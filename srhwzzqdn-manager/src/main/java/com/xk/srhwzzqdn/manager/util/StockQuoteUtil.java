package com.xk.srhwzzqdn.manager.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * 股票实时行情数据获取工具
 * 基于东方财富 push2/push2his 接口获取实时报价、日K线、资金流向
 * 用于 AI 预测时注入当天实时数据，解决 AI 模型知识时效性问题
 */
public class StockQuoteUtil {
    private static final Logger logger = LoggerFactory.getLogger(StockQuoteUtil.class);

    private static final String QUOTE_URL_FALLBACK = "http://push2.eastmoney.com/api/qt/stock/get";
    private static final String KLINE_URL_FALLBACK = "http://push2his.eastmoney.com/api/qt/stock/kline/get";
    private static final String FLOW_URL_FALLBACK = "http://push2.eastmoney.com/api/qt/stock/fflow/daykline/get";
    /** push2his 资金流日K兜底（与 push2 主源不同 host 家族，push2 黑洞时常存活；fields2 口径与主源一致） */
    private static final String FFLOW_DAY_URL_FALLBACK =
            "https://push2his.eastmoney.com/api/qt/stock/fflow/daykline/get?lmt=%d&klt=101&fields1=f1,f2,f3,f7&fields2=f51,f52,f53,f54,f55&secid=%s";
    private static final String CLIST_URL_FALLBACK = "http://push2.eastmoney.com/api/qt/clist/get";
    private static final String TOPIC_POOL_URL_FALLBACK = "http://push2ex.eastmoney.com/getTopic";

    // ==================== 请求头 ====================
    // 合规约束：固定单一UA/Referer正常访问公开数据接口，不做UA轮换伪装、不规避访问控制；
    // 访问频率由全局熔断器（StockDataFetcher分组）+单次尝试+批量200ms间隔约束，克制调用
    private static final String FIXED_UA =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36";
    private static void applyBrowserHeaders(HttpGet request) {
        String host = request.getURI().getHost();
        boolean isSina = host != null && host.contains("sina");
        request.setHeader("User-Agent", FIXED_UA);
        request.setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
        request.setHeader("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8");
        request.setHeader("Connection", "keep-alive");
        request.setHeader("Cache-Control", "max-age=0");
        // 新浪接口2021年起强制要求sina站内Referer（接口访问要求，非伪装）；其余固定东财行情页来源
        request.setHeader("Referer", isSina ? "https://finance.sina.com.cn/" : "https://quote.eastmoney.com/");
    }

    private static String getQuoteUrl() {
        return InterfaceConfigUtil.getUrl("util_quote_url", QUOTE_URL_FALLBACK);
    }
    private static String getKlineUrl() {
        return InterfaceConfigUtil.getUrl("util_kline_url", KLINE_URL_FALLBACK);
    }
    private static String getFlowUrl() {
        return InterfaceConfigUtil.getUrl("util_flow_url", FLOW_URL_FALLBACK);
    }
    /** push2his 资金流日K兜底（完整模板口径：lmt/fields 固定，代码仅替换 secid；独立配置键防与 util_flow_url 口径互踩） */
    private static String getFflowDayUrlTpl() {
        return InterfaceConfigUtil.getUrl("util_fflow_day_url", FFLOW_DAY_URL_FALLBACK);
    }
    private static String getClistUrl() {
        return InterfaceConfigUtil.getUrl("util_clist_url", CLIST_URL_FALLBACK);
    }
    private static String getTopicPoolUrl() {
        return InterfaceConfigUtil.getUrl("util_topic_pool_url", TOPIC_POOL_URL_FALLBACK);
    }
    /** 腾讯行情host（纯host口径，代码拼接/q=前缀；独立配置键，避免与资产区tx键口径互相干扰） */
    private static String getTxQuoteUrl() {
        return InterfaceConfigUtil.getUrl("util_tx_quote_url", "https://qt.gtimg.cn");
    }
    /** 腾讯日K线（前复权完整路径） */
    private static String getTxKlineUrl() {
        return InterfaceConfigUtil.getUrl("util_tx_kline_url", "https://ifzq.gtimg.cn/appstock/app/fqkline/get");
    }

    /**
     * 构建腾讯行情代码前缀：沪市(6开头)→sh，北交所(8/4开头)→bj，其余→sz
     */
    private static String txCode(String stockCode) {
        String code = stockCode == null ? "" : stockCode.trim();
        String pfx = code.startsWith("6") ? "sh" : (code.startsWith("8") || code.startsWith("4")) ? "bj" : "sz";
        return pfx + code;
    }

    /**
     * 腾讯行情字段数组（GBK解码，~分隔）：v_sz000001="51~平安银行~000001~..." 取引号内载荷
     * 字段口径2026-09-26实测：[3]最新价 [4]昨收 [5]今开 [31]涨跌额 [32]涨跌幅% [33]最高 [34]最低
     * [35]最新价/成交量/成交额(元) [36]成交量手 [37]成交额(万) [38]换手率% [43]振幅%
     */
    private static String[] fetchTxQuoteFields(String txSymbol) {
        String body = httpGet(getTxQuoteUrl() + "/q=" + txSymbol, "GBK");
        if (body == null) return null;
        String[] parts = body.split("\"");
        if (parts.length < 2) return null;
        return parts[1].split("~");
    }

    /**
     * 根据股票代码构建东方财富 secid
     * 沪市(6开头)→1.code，深市(0/3开头)→0.code
     */
    private static String buildSecId(String stockCode) {
        if (stockCode == null || stockCode.isEmpty()) return "";
        String code = stockCode.trim();
        String market = code.startsWith("6") ? "1" : "0";
        return market + "." + code;
    }

    /**
     * HTTP GET 请求（浏览器级请求头伪装 + 单次尝试 + 5s/8s超时）
     * 单次不重试：HttpClient默认对NoHttpResponseException还会静默内部重试3次（单次调用最坏4×超时），
     * 既拖垮race线程池（源任务排队超时被cancel=静默失败），又会在拉黑期反复戳接口延长封禁；
     * 故禁用自动重试且外层仅1次尝试，失败直接激活该组熔断（拉黑期内0请求）
     * 接入全局统一熔断器（StockDataFetcher）：按URL特征映射接口组
     */
    private static String httpGet(String url) {
        return httpGet(url, "UTF-8");
    }

    /**
     * HTTP GET 请求（浏览器级请求头 + 单次尝试 + 5s/8s超时 + 指定响应字符集）
     * 腾讯行情URL不匹配任何熔断组（groupOf返回null），单次尝试不重试不熔断，与合规模型一致
     */
    private static String httpGet(String url, String charset) {
        String group = groupOf(url);
        if (group != null && StockDataFetcher.blocked(group)) {
            logger.info("行情工具接口熔断期内跳过 | 组={} | 剩余{}秒 | url={}",
                    group, StockDataFetcher.remainMs(group) / 1000, url);
            return null;
        }
        try (CloseableHttpClient client = HttpClientBuilder.create().disableAutomaticRetries().build()) {
            HttpGet request = new HttpGet(url);
            applyBrowserHeaders(request);
            request.setConfig(RequestConfig.custom()
                    .setConnectTimeout(5000)
                    .setSocketTimeout(8000)
                    .build());
            try (CloseableHttpResponse response = client.execute(request)) {
                if (response.getStatusLine().getStatusCode() == 200) {
                    if (group != null) StockDataFetcher.markSuccess(group);
                    return EntityUtils.toString(response.getEntity(), charset);
                }
                logger.warn("行情接口非200 | 状态={} | url={}", response.getStatusLine().getStatusCode(), url);
            }
        } catch (Exception e) {
            logger.warn("行情接口请求失败 | url={} | 原因={}", url, e.getMessage());
        }
        // 失败 → 激活该接口组熔断（K线类5分钟，其余3分钟），期间该组请求直接跳过
        if (group != null) {
            StockDataFetcher.markFail(group, "kline".equals(group) || "datacenter".equals(group)
                    ? 5 * 60 * 1000L : 3 * 60 * 1000L);
        }
        return null;
    }

    /** 按URL特征映射熔断接口组（与全局各组语义对齐；非东财域名（腾讯兜底等）返回null不参与熔断，
     *  防止腾讯fqkline含"kline"字样被误映射进GROUP_KLINE导致兜底在东财熔断期内被误跳过） */
    private static String groupOf(String url) {
        if (url == null) return null;
        if (!url.contains("eastmoney.com")) return null;
        if (url.contains("fflow")) return StockDataFetcher.GROUP_FLOW;
        if (url.contains("kline") || url.contains("push2his")) return StockDataFetcher.GROUP_KLINE;
        if (url.contains("getTopic")) return StockDataFetcher.GROUP_POOL;
        if (url.contains("clist")) return StockDataFetcher.GROUP_CLIST;
        if (url.contains("push2") || url.contains("datacenter")) return StockDataFetcher.GROUP_QUOTE;
        return null;
    }

    /**
     * 数值除以100保留2位小数（东方财富字段精度处理）
     */
    private static String div100(Object val) {
        if (val == null) return "-";
        try {
            return new BigDecimal(val.toString()).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP).toPlainString();
        } catch (Exception e) {
            return val.toString();
        }
    }

    /**
     * 获取实时报价并格式化为文本
     * 接口优先串行：东财报价（失败/空→腾讯单票行情兜底），输出文本格式保持一致
     * 返回 null 表示双源均失败
     */
    private static String fetchRealtimeQuote(String stockCode) {
        String secid = buildSecId(stockCode);
        // f9=市盈率(动态,×100) f23=市净率(×100) f100=行业名 f116=总市值(元) f117=流通市值(元)——基本面字段0增量请求顺带获取
        String url = getQuoteUrl() + "?secid=" + secid +
                "&fields=f43,f44,f45,f46,f47,f48,f57,f58,f9,f23,f100,f116,f117,f168,f169,f170,f171";
        String body = httpGet(url);
        if (body != null) {
            try {
                JSONObject json = JSON.parseObject(body);
                JSONObject d = json.getJSONObject("data");
                if (d != null) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("【当日实时行情】\n");
                    sb.append("股票名称：").append(d.getString("f58")).append("\n");
                    sb.append("股票代码：").append(d.getString("f57")).append("\n");
                    sb.append("最新价：").append(div100(d.get("f43"))).append(" 元\n");
                    sb.append("涨跌额：").append(div100(d.get("f169"))).append(" 元\n");
                    sb.append("涨跌幅：").append(div100(d.get("f170"))).append("%\n");
                    sb.append("今开：").append(div100(d.get("f46"))).append(" 元\n");
                    sb.append("最高：").append(div100(d.get("f44"))).append(" 元\n");
                    sb.append("最低：").append(div100(d.get("f45"))).append(" 元\n");
                    sb.append("振幅：").append(div100(d.get("f171"))).append("%\n");
                    sb.append("换手率：").append(div100(d.get("f168"))).append("%\n");
                    sb.append("成交量：").append(d.get("f47")).append(" 手\n");
                    sb.append("成交额：").append(d.get("f48")).append(" 元\n");
                    sb.append("行业：").append(strOrDash(d.getString("f100"))).append("\n");
                    sb.append("市盈率(动态)：").append(div100(d.get("f9"))).append("\n");
                    sb.append("市净率：").append(div100(d.get("f23"))).append("\n");
                    sb.append("总市值：").append(divE8(d.get("f116"))).append(" 亿元\n");
                    sb.append("流通市值：").append(divE8(d.get("f117"))).append(" 亿元\n");
                    return sb.toString();
                }
            } catch (Exception e) {
                logger.error("解析实时报价失败", e);
            }
        }
        return fetchRealtimeQuoteTx(stockCode);
    }

    /** 字符串空值转"-"（行业等文本字段） */
    private static String strOrDash(String s) {
        return (s == null || s.isEmpty() || "-".equals(s)) ? "-" : s;
    }

    /** 数值÷1e8保留2位小数（市值类字段元→亿） */
    private static String divE8(Object val) {
        if (val == null || "-".equals(val)) return "-";
        try {
            return new BigDecimal(val.toString()).divide(new BigDecimal("100000000"), 2, RoundingMode.HALF_UP).toPlainString();
        } catch (Exception e) {
            return val.toString();
        }
    }

    /**
     * 腾讯单票实时行情兜底（东财失败/空时；字段口径见fetchTxQuoteFields注释）
     */
    private static String fetchRealtimeQuoteTx(String stockCode) {
        try {
            String[] f = fetchTxQuoteFields(txCode(stockCode));
            if (f == null || f.length < 44 || f[3] == null || f[3].isEmpty()) return null;
            // 成交额取[35]第三段精确元值（[37]为四舍五入到万的近似值）
            String amountYuan = "0";
            String[] amt = f[35].split("/");
            if (amt.length > 2) amountYuan = amt[2];
            StringBuilder sb = new StringBuilder();
            sb.append("【当日实时行情】\n");
            sb.append("股票名称：").append(f[1]).append("\n");
            sb.append("股票代码：").append(f[2]).append("\n");
            sb.append("最新价：").append(f[3]).append(" 元\n");
            sb.append("涨跌额：").append(f[31]).append(" 元\n");
            sb.append("涨跌幅：").append(f[32]).append("%\n");
            sb.append("今开：").append(f[5]).append(" 元\n");
            sb.append("最高：").append(f[33]).append(" 元\n");
            sb.append("最低：").append(f[34]).append(" 元\n");
            sb.append("振幅：").append(f[43]).append("%\n");
            sb.append("换手率：").append(f[38]).append("%\n");
            sb.append("成交量：").append(f[36]).append(" 手\n");
            sb.append("成交额：").append(amountYuan).append(" 元\n");
            // 腾讯无PE/PB/行业字段，仅补市值（[44]流通市值(亿) [45]总市值(亿)），字段缺失标注不提供
            sb.append("行业：-\n");
            sb.append("市盈率(动态)：-\n");
            sb.append("市净率：-\n");
            if (f.length > 45 && f[45] != null && !f[45].isEmpty()) sb.append("总市值：").append(f[45]).append(" 亿元\n");
            else sb.append("总市值：-\n");
            if (f.length > 44 && f[44] != null && !f[44].isEmpty()) sb.append("流通市值：").append(f[44]).append(" 亿元\n");
            else sb.append("流通市值：-\n");
            return sb.toString();
        } catch (Exception e) {
            logger.warn("腾讯实时报价兜底失败 code={}", stockCode);
            return null;
        }
    }

    /**
     * 获取最近N根日K线并格式化为文本
     * 接口优先串行：东财K线（失败/空→腾讯日K兜底，前复权口径一致）
     */
    private static String fetchKLineData(String stockCode, int count) {
        KlineData kd = fetchKLineDataBoth(stockCode, count);
        return kd == null ? null : kd.text;
    }

    /** K线双输出：text=AI注入文本（与原格式完全一致），bars=[开,收,高,低]供技术指标计算（0增量请求） */
    private static class KlineData {
        final String text;
        final List<double[]> bars;
        KlineData(String text, List<double[]> bars) { this.text = text; this.bars = bars; }
    }

    private static KlineData fetchKLineDataBoth(String stockCode, int count) {
        String secid = buildSecId(stockCode);
        String url = getKlineUrl() + "?secid=" + secid +
                "&klt=101&fqt=1&end=20500101&lmt=" + count +
                "&fields1=f1,f2,f3,f4,f5,f6&fields2=f51,f52,f53,f54,f55,f56,f57,f58";
        String body = httpGet(url);
        if (body != null) {
            try {
                JSONObject json = JSON.parseObject(body);
                JSONObject d = json.getJSONObject("data");
                if (d != null) {
                    JSONArray klines = d.getJSONArray("klines");
                    if (klines != null && !klines.isEmpty()) {
                        StringBuilder sb = new StringBuilder();
                        sb.append("【最近").append(klines.size()).append("日K线（日期,开盘,收盘,最高,最低,成交量手,成交额元,振幅%）】\n");
                        List<double[]> bars = new ArrayList<>(klines.size());
                        for (int i = 0; i < klines.size(); i++) {
                            String row = klines.getString(i);
                            sb.append(row).append("\n");
                            try {
                                String[] p = row.split(",");
                                // fields2=f51日期,f52开,f53收,f54高,f55低 → [1]开[2]收[3]高[4]低
                                bars.add(new double[]{Double.parseDouble(p[1]), Double.parseDouble(p[2]),
                                        Double.parseDouble(p[3]), Double.parseDouble(p[4])});
                            } catch (Exception ignore) { /* 单行解析失败不影响文本输出 */ }
                        }
                        return new KlineData(sb.toString(), bars);
                    }
                }
            } catch (Exception e) {
                logger.error("解析K线数据失败", e);
            }
        }
        return fetchKLineDataTxBoth(stockCode, count);
    }

    /**
     * 腾讯日K线兜底（东财失败/空时）：bar=[日期,开,收,高,低,量手]（可能混入分红对象，只取前6列）；
     * 振幅=(高-低)/前收×100，首根前收以开盘近似；腾讯无历史成交额列，文本标注说明
     */
    private static KlineData fetchKLineDataTxBoth(String stockCode, int count) {
        try {
            String tx = txCode(stockCode);
            String body = httpGet(getTxKlineUrl() + "?param=" + tx + ",day,,," + count + ",qfq");
            if (body == null) return null;
            JSONObject node = JSON.parseObject(body).getJSONObject("data").getJSONObject(tx);
            if (node == null) return null;
            JSONArray days = node.getJSONArray("qfqday");
            if (days == null) days = node.getJSONArray("day");
            if (days == null || days.isEmpty()) return null;
            StringBuilder sb = new StringBuilder();
            sb.append("【最近").append(days.size()).append("日K线（日期,开盘,收盘,最高,最低,成交量手,振幅%）】\n");
            List<double[]> bars = new ArrayList<>(days.size());
            BigDecimal prevClose = null;
            for (int i = 0; i < days.size(); i++) {
                JSONArray a = days.getJSONArray(i);
                double open = Double.parseDouble(a.getString(1));
                double close = Double.parseDouble(a.getString(2));
                double high = Double.parseDouble(a.getString(3));
                double low = Double.parseDouble(a.getString(4));
                bars.add(new double[]{open, close, high, low});
                BigDecimal prev = prevClose != null ? prevClose : new BigDecimal(a.getString(1));
                BigDecimal amp = prev.signum() == 0 ? BigDecimal.ZERO
                        : new BigDecimal(a.getString(3)).subtract(new BigDecimal(a.getString(4)))
                                .multiply(new BigDecimal("100")).divide(prev, 2, RoundingMode.HALF_UP);
                sb.append(a.getString(0)).append(",").append(a.getString(1)).append(",")
                        .append(a.getString(2)).append(",").append(a.getString(3)).append(",")
                        .append(a.getString(4)).append(",").append(a.size() > 5 ? a.getString(5) : "0").append(",")
                        .append(amp.toPlainString()).append("\n");
                prevClose = new BigDecimal(a.getString(2));
            }
            return new KlineData(sb.toString(), bars);
        } catch (Exception e) {
            logger.warn("腾讯K线兜底失败 code={}", stockCode);
            return null;
        }
    }

    /**
     * 技术指标文本（纯 Java 计算，0 增量请求）：MA5/10/20、MACD(12,26,9)、KDJ(9,3,3)
     * 仅基于已拉取的日K bars，样本不足时标注实际样本数
     */
    private static String formatTechIndicators(List<double[]> bars) {
        if (bars == null || bars.isEmpty()) return null;
        StringBuilder sb = new StringBuilder();
        sb.append("【技术指标（基于近").append(bars.size()).append("日收盘价计算）】\n");
        // 均线：收盘价简单均值（收盘=bars[i][1]）
        appendMa(sb, "MA5", bars, 5);
        appendMa(sb, "MA10", bars, 10);
        appendMa(sb, "MA20", bars, 20);
        // MACD：EMA12/EMA26→DIF，DEA=EMA9(DIF)，柱=2×(DIF-DEA)
        if (bars.size() >= 9) {
            double ema12 = bars.get(0)[1], ema26 = bars.get(0)[1], dea = 0;
            double dif = 0, lastDif = 0, lastDea = 0;
            for (int i = 0; i < bars.size(); i++) {
                double c = bars.get(i)[1];
                ema12 = i == 0 ? c : ema12 * 11 / 13 + c * 2 / 13;
                ema26 = i == 0 ? c : ema26 * 25 / 27 + c * 2 / 27;
                dif = ema12 - ema26;
                dea = i == 0 ? dif : dea * 8 / 10 + dif * 2 / 10;
                lastDif = dif;
                lastDea = dea;
            }
            sb.append("MACD：DIF=").append(r2(lastDif)).append("，DEA=").append(r2(lastDea))
                    .append("，柱=").append(r2(2 * (lastDif - lastDea))).append("\n");
        } else {
            sb.append("MACD：样本不足\n");
        }
        // KDJ(9,3,3)：RSV=(C-Ln)/(Hn-Ln)×100，K=2/3K'+1/3RSV，D=2/3D'+1/3K，J=3K-2D
        if (bars.size() >= 9) {
            double k = 50, dj = 50;
            for (int i = 0; i < bars.size(); i++) {
                int from = Math.max(0, i - 8);
                double hh = -Double.MAX_VALUE, ll = Double.MAX_VALUE;
                for (int j = from; j <= i; j++) {
                    hh = Math.max(hh, bars.get(j)[2]);
                    ll = Math.min(ll, bars.get(j)[3]);
                }
                double rsv = hh == ll ? 50 : (bars.get(i)[1] - ll) / (hh - ll) * 100;
                k = k * 2 / 3 + rsv / 3;
                dj = dj * 2 / 3 + k / 3;
            }
            sb.append("KDJ：K=").append(r2(k)).append("，D=").append(r2(dj)).append("，J=").append(r2(3 * k - 2 * dj)).append("\n");
        } else {
            sb.append("KDJ：样本不足\n");
        }
        return sb.toString();
    }

    /** N日简单均线输出（样本不足标注） */
    private static void appendMa(StringBuilder sb, String name, List<double[]> bars, int n) {
        if (bars.size() < n) {
            sb.append(name).append("：样本不足\n");
            return;
        }
        double sum = 0;
        for (int i = bars.size() - n; i < bars.size(); i++) sum += bars.get(i)[1];
        sb.append(name).append("：").append(r2(sum / n)).append("\n");
    }

    /** 保留2位小数字符串 */
    private static String r2(double v) {
        return new BigDecimal(v).setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    /**
     * 获取最近N天资金流向并格式化为文本
     * 接口优先串行：push2 主源（失败/空→push2his fflow daykline 兜底，fields2 口径一致，GROUP_FLOW 熔断共享）
     */
    private static String fetchMoneyFlow(String stockCode, int days) {
        String secid = buildSecId(stockCode);
        String url = getFlowUrl() + "?secid=" + secid + "&lmt=" + days;
        String body = httpGet(url);
        String result = parseMoneyFlowText(body);
        if (result != null) return result;
        return fetchMoneyFlowHis(stockCode, days);
    }

    /** 解析资金流响应为文本（主源/兜底共用；失败返回null由调用方走兜底） */
    private static String parseMoneyFlowText(String body) {
        if (body == null) return null;
        try {
            JSONObject json = JSON.parseObject(body);
            JSONObject d = json.getJSONObject("data");
            if (d == null) return null;
            JSONArray klines = d.getJSONArray("klines");
            if (klines == null || klines.isEmpty()) return null;
            StringBuilder sb = new StringBuilder();
            sb.append("【最近").append(klines.size()).append("日资金流向（日期,主力净流入元,小单净流入,中单净流入,大单净流入）】\n");
            for (int i = 0; i < klines.size(); i++) {
                sb.append(klines.getString(i)).append("\n");
            }
            return sb.toString();
        } catch (Exception e) {
            logger.error("解析资金流向失败", e);
            return null;
        }
    }

    /**
     * push2his fflow daykline 资金流兜底（与 push2 主源不同 host 家族，push2 黑洞时常存活；
     * URL 含 fflow→GROUP_FLOW 与主源共享熔断；fields2=f51,f52,f53,f54,f55 与主源输出格式一致）
     */
    private static String fetchMoneyFlowHis(String stockCode, int days) {
        try {
            String url = String.format(getFflowDayUrlTpl(), days, buildSecId(stockCode));
            String body = httpGet(url);
            String result = parseMoneyFlowText(body);
            if (result != null) logger.info("资金流push2his兜底成功 code={}", stockCode);
            return result;
        } catch (Exception e) {
            logger.warn("push2his资金流兜底失败 code={} 原因={}", stockCode, e.getMessage());
            return null;
        }
    }

    /**
     * 获取股票完整实时行情文本（报价含基本面 + K线 + 技术指标 + 资金流向 + 大盘指数）
     * 用于注入 AI 预测提示词
     * 四块并行执行（指数块内部复用东财→腾讯兜底链）；任一接口失败不影响其他数据，返回的文本始终非 null
     * 技术指标基于已拉取的K线纯 Java 计算，0 增量请求；资金流向为 push2→push2his 兜底链
     */
    public static String getRealtimeMarketData(String stockCode) {
        ExecutorService ex = Executors.newFixedThreadPool(4);
        Future<String> quoteF = ex.submit(() -> fetchRealtimeQuote(stockCode));
        Future<KlineData> klineF = ex.submit(() -> fetchKLineDataBoth(stockCode, 30));
        Future<String> flowF = ex.submit(() -> fetchMoneyFlow(stockCode, 5));
        Future<String> marketF = ex.submit(StockQuoteUtil::fetchMarketBrief);
        ex.shutdown();
        String quote = joinQuietly(quoteF);
        KlineData kline = joinKlineQuietly(klineF);
        String flow = joinQuietly(flowF);
        String market = joinQuietly(marketF);
        StringBuilder sb = new StringBuilder();
        if (quote != null) sb.append(quote);
        else sb.append("【当日实时行情】获取失败\n");
        if (kline != null) sb.append(kline.text);
        else sb.append("【最近日K线】获取失败\n");
        String tech = kline == null ? null : formatTechIndicators(kline.bars);
        if (tech != null) sb.append(tech);
        else sb.append("【技术指标】K线获取失败，无法计算\n");
        if (flow != null) sb.append(flow);
        else sb.append("【资金流向】获取失败\n");
        if (market != null) sb.append(market);
        else sb.append("【大盘指数】获取失败\n");
        return sb.toString();
    }

    /** K线双输出并行块收取：超时/异常返回null */
    private static KlineData joinKlineQuietly(Future<KlineData> f) {
        try {
            return f.get(45, java.util.concurrent.TimeUnit.SECONDS);
        } catch (Exception e) {
            f.cancel(true);
            return null;
        }
    }

    /**
     * 大盘指数简报（三大指数涨跌幅+两市成交额，复用 fetchIndexChangePct/fetchIndexAmount 的东财→腾讯兜底链）
     */
    private static String fetchMarketBrief() {
        Double sh = fetchIndexChangePct("1.000001");
        Double sz = fetchIndexChangePct("0.399001");
        Double cyb = fetchIndexChangePct("0.399006");
        if (sh == null && sz == null && cyb == null) return null;
        StringBuilder sb = new StringBuilder();
        sb.append("【大盘指数】\n");
        sb.append("上证指数涨跌幅：").append(sh == null ? "-" : r2(sh)).append("%\n");
        sb.append("深证成指涨跌幅：").append(sz == null ? "-" : r2(sz)).append("%\n");
        sb.append("创业板指涨跌幅：").append(cyb == null ? "-" : r2(cyb)).append("%\n");
        return sb.toString();
    }

    /** 并行块收取：超时/异常返回null（该块降级不影响其余块） */
    private static String joinQuietly(Future<String> f) {
        try {
            return f.get(45, java.util.concurrent.TimeUnit.SECONDS);
        } catch (Exception e) {
            f.cancel(true);
            return null;
        }
    }

    /**
     * 获取单个指数涨跌幅（÷100）
     * 接口优先串行：东财报价（失败/空→腾讯指数行情兜底[32]涨跌幅%）
     */
    private static Double fetchIndexChangePct(String secid) {
        String url = getQuoteUrl() + "?secid=" + secid + "&fields=f169";
        String body = httpGet(url);
        if (body != null) {
            try {
                JSONObject json = JSON.parseObject(body);
                JSONObject d = json.getJSONObject("data");
                if (d != null) {
                    return new BigDecimal(d.get("f169").toString())
                            .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP).doubleValue();
                }
            } catch (Exception e) {
                logger.warn("解析指数涨跌幅失败: {}", secid);
            }
        }
        try {
            String[] f = fetchTxQuoteFields(secid.replace("1.", "sh").replace("0.", "sz"));
            if (f != null && f.length > 32 && f[32] != null && !f[32].isEmpty()) {
                return new BigDecimal(f[32]).setScale(2, RoundingMode.HALF_UP).doubleValue();
            }
        } catch (Exception e) {
            logger.warn("腾讯指数涨跌幅兜底失败: {}", secid);
        }
        return null;
    }

    /**
     * 获取指数成交额（元→亿元）
     * 接口优先串行：东财报价（失败/空→腾讯指数行情兜底[37]成交额万→亿元）
     */
    private static Double fetchIndexAmount(String secid) {
        String url = getQuoteUrl() + "?secid=" + secid + "&fields=f48";
        String body = httpGet(url);
        if (body != null) {
            try {
                JSONObject json = JSON.parseObject(body);
                JSONObject d = json.getJSONObject("data");
                if (d != null) {
                    return new BigDecimal(d.get("f48").toString())
                            .divide(new BigDecimal("100000000"), 2, RoundingMode.HALF_UP).doubleValue();
                }
            } catch (Exception e) {
                logger.warn("解析指数成交额失败: {}", secid);
            }
        }
        try {
            String[] f = fetchTxQuoteFields(secid.replace("1.", "sh").replace("0.", "sz"));
            if (f != null && f.length > 37 && f[37] != null && !f[37].isEmpty()) {
                return new BigDecimal(f[37]).divide(new BigDecimal("10000"), 2, RoundingMode.HALF_UP).doubleValue();
            }
        } catch (Exception e) {
            logger.warn("腾讯指数成交额兜底失败: {}", secid);
        }
        return null;
    }

    /**
     * 获取A股大盘实时概览（3大指数+涨跌家数统计）
     * 用于每日复盘表单自动填充
     * 返回 JSONObject：shChangePct/szChangePct/cybChangePct/totalAmount/riseCount/fallCount/limitUpCount/limitDownCount
     */
    public static JSONObject getMarketOverview() {
        JSONObject result = new JSONObject();
        // 3大指数涨跌幅
        result.put("shChangePct", fetchIndexChangePct("1.000001"));
        result.put("szChangePct", fetchIndexChangePct("0.399001"));
        result.put("cybChangePct", fetchIndexChangePct("0.399006"));
        // 两市成交额（沪市+深市，亿元）
        Double shAmount = fetchIndexAmount("1.000001");
        Double szAmount = fetchIndexAmount("0.399001");
        if (shAmount != null && szAmount != null) {
            result.put("totalAmount", new BigDecimal(shAmount + szAmount).setScale(2, RoundingMode.HALF_UP).doubleValue());
        } else {
            result.put("totalAmount", null);
        }
        // 涨跌家数统计（实时）
        result.putAll(fetchRiseFallCountRealtime());
        // 涨停/跌停家数（涨停池/跌停池接口，数据更准确）
        String todayYyyymmdd = LocalDate.now().toString().replace("-", "");
        result.put("limitUpCount", fetchTopicPoolCount("ZTPool", todayYyyymmdd));
        result.put("limitDownCount", fetchTopicPoolCount("DTPool", todayYyyymmdd));
        return result;
    }

    /**
     * 实时涨跌家数统计
     * clist 接口 fltt=2 时 f3 已是百分比数值（如 5.23），无需再除以100
     */
    private static JSONObject fetchRiseFallCountRealtime() {
        JSONObject stat = new JSONObject();
        int riseCount = 0, fallCount = 0;
        String clistUrl = getClistUrl() + "?pn=1&pz=6000&po=1&np=1&fltt=2&invt=2" +
                "&fs=m:0+t:6,m:0+t:80,m:1+t:2,m:1+t:23&fields=f3";
        String body = httpGet(clistUrl);
        if (body != null) {
            try {
                JSONObject json = JSON.parseObject(body);
                JSONObject d = json.getJSONObject("data");
                if (d != null) {
                    JSONArray diff = d.getJSONArray("diff");
                    if (diff != null) {
                        for (int i = 0; i < diff.size(); i++) {
                            JSONObject item = diff.getJSONObject(i);
                            double p = Double.parseDouble(item.get("f3").toString());
                            if (p > 0) riseCount++;
                            else if (p < 0) fallCount++;
                        }
                    }
                }
            } catch (Exception e) {
                logger.error("解析涨跌家数统计失败", e);
            }
        }
        stat.put("riseCount", riseCount);
        stat.put("fallCount", fallCount);
        return stat;
    }

    /**
     * 获取指定日期涨停/跌停家数（东方财富涨停池/跌停池接口，支持历史日期）
     * @param poolType ZTPool=涨停池，DTPool=跌停池
     * @param yyyymmdd 日期（yyyyMMdd）
     * @return 家数，获取失败返回 null
     */
    private static Integer fetchTopicPoolCount(String poolType, String yyyymmdd) {
        String url = getTopicPoolUrl() + poolType +
                "?ut=7eea3edcaed734bea9cbfc24409ed989&dpt=wz.ztzt&Pageindex=0&pagesize=1&sort=fbt%3Aasc&date=" + yyyymmdd;
        String body = httpGet(url);
        if (body == null) return null;
        try {
            JSONObject json = JSON.parseObject(body);
            JSONObject d = json.getJSONObject("data");
            if (d == null) return null;
            return d.getInteger("tc");
        } catch (Exception e) {
            logger.error("解析涨停/跌停池失败: {}", poolType, e);
            return null;
        }
    }

    /**
     * 按日期获取A股大盘概览（每日复盘用）
     * 指数涨跌幅/成交额：东方财富日K线历史接口，定位复盘日期或其之前最近交易日
     * 涨停/跌停家数：涨停池/跌停池接口（支持历史日期）
     * 上涨/下跌家数：仅复盘日为当天时可实时统计，历史日期无公开数据源返回 null（前端不覆盖）
     * 返回额外字段 actualDate：实际数据日期（复盘日为非交易日时为最近一个交易日）
     */
    public static JSONObject getMarketOverviewByDate(String dateStr) {
        JSONObject result = new JSONObject();
        LocalDate targetDate;
        try {
            targetDate = LocalDate.parse(dateStr);
        } catch (Exception e) {
            targetDate = LocalDate.now();
        }
        // 未来日期按当天处理
        if (targetDate.isAfter(LocalDate.now())) {
            targetDate = LocalDate.now();
        }
        String target = targetDate.toString();
        // 3大指数按日期取K线涨跌幅与成交额（首个定位成功的指数确定实际交易日）
        fetchIndexDataByDate(result, "1.000001", target, "shChangePct", "shAmount");
        fetchIndexDataByDate(result, "0.399001", target, "szChangePct", "szAmount");
        fetchIndexDataByDate(result, "0.399006", target, "cybChangePct", null);
        // 两市成交额合计（亿元）
        Double shAmount = result.getDouble("shAmount");
        Double szAmount = result.getDouble("szAmount");
        if (shAmount != null && szAmount != null) {
            result.put("totalAmount", new BigDecimal(shAmount + szAmount).setScale(2, RoundingMode.HALF_UP).doubleValue());
        } else {
            result.put("totalAmount", null);
        }
        result.remove("shAmount");
        result.remove("szAmount");
        // 涨停/跌停家数（按目标日期）
        String yyyymmdd = target.replace("-", "");
        result.put("limitUpCount", fetchTopicPoolCount("ZTPool", yyyymmdd));
        result.put("limitDownCount", fetchTopicPoolCount("DTPool", yyyymmdd));
        // 上涨/下跌家数：仅当天可实时统计
        if (target.equals(LocalDate.now().toString())) {
            result.putAll(fetchRiseFallCountRealtime());
        } else {
            result.put("riseCount", null);
            result.put("fallCount", null);
        }
        // 实际交易日兜底（所有指数K线定位失败时）
        if (!result.containsKey("actualDate")) {
            result.put("actualDate", target);
        }
        return result;
    }

    /**
     * 从日K线定位目标日期（或其之前最近交易日），填充指数涨跌幅(f59)与成交额(f57)
     * 首次定位成功时将实际交易日写入 result.actualDate
     * klines 每根格式：[0]日期 [1]开盘 [2]收盘 [3]最高 [4]最低 [5]成交量 [6]成交额 [7]振幅 [8]涨跌幅
     */
    private static void fetchIndexDataByDate(JSONObject result, String secid, String target, String pctKey, String amountKey) {
        String url = getKlineUrl() + "?secid=" + secid +
                "&klt=101&fqt=1&end=20500101&lmt=80" +
                "&fields1=f1,f2,f3,f4,f5,f6&fields2=f51,f52,f53,f54,f55,f56,f57,f58,f59";
        String body = httpGet(url);
        if (body != null) {
            try {
                JSONObject json = JSON.parseObject(body);
                JSONObject d = json.getJSONObject("data");
                if (d != null) {
                    JSONArray klines = d.getJSONArray("klines");
                    if (klines != null && !klines.isEmpty()) {
                        // 定位日期 <= target 的最后一根K线
                        int idx = -1;
                        for (int i = 0; i < klines.size(); i++) {
                            String kDate = klines.getString(i).split(",")[0];
                            if (kDate.compareTo(target) <= 0) idx = i;
                            else break;
                        }
                        if (idx >= 0) {
                            String[] cur = klines.getString(idx).split(",");
                            if (!result.containsKey("actualDate")) {
                                result.put("actualDate", cur[0]);
                            }
                            result.put(pctKey, new BigDecimal(cur[8]).setScale(2, RoundingMode.HALF_UP).doubleValue());
                            if (amountKey != null) {
                                result.put(amountKey, new BigDecimal(cur[6])
                                        .divide(new BigDecimal("100000000"), 2, RoundingMode.HALF_UP).doubleValue());
                            }
                            return;
                        }
                    }
                }
            } catch (Exception e) {
                logger.error("按日期解析指数K线失败: {}", secid, e);
            }
        }
        fetchIndexDataByDateTx(result, secid, target, pctKey, amountKey);
    }

    /**
     * 腾讯指数日K兜底（东财失败/空时）：bar=[日期,开,收,高,低,量]，涨跌幅=收/前收-1；
     * 腾讯无历史成交额列→amountKey不填充（前端对null不覆盖，保持现有降级语义）
     */
    private static void fetchIndexDataByDateTx(JSONObject result, String secid, String target, String pctKey, String amountKey) {
        try {
            String[] p = secid.split("\\.");
            String sym = ("1".equals(p[0]) ? "sh" : "sz") + p[1];
            String body = httpGet(getTxKlineUrl() + "?param=" + sym + ",day,,,80,qfq");
            if (body == null) return;
            JSONObject node = JSON.parseObject(body).getJSONObject("data").getJSONObject(sym);
            if (node == null) return;
            JSONArray days = node.getJSONArray("qfqday");
            if (days == null) days = node.getJSONArray("day");
            if (days == null || days.isEmpty()) return;
            int idx = -1;
            for (int i = 0; i < days.size(); i++) {
                String d0 = days.getJSONArray(i).getString(0);
                if (d0.compareTo(target) <= 0) idx = i;
                else break;
            }
            if (idx < 0) return;
            JSONArray bar = days.getJSONArray(idx);
            BigDecimal close = new BigDecimal(bar.getString(2));
            BigDecimal prev = idx > 0 ? new BigDecimal(days.getJSONArray(idx - 1).getString(2))
                    : new BigDecimal(bar.getString(1));
            if (!result.containsKey("actualDate")) {
                result.put("actualDate", bar.getString(0));
            }
            result.put(pctKey, close.subtract(prev).multiply(new BigDecimal("100"))
                    .divide(prev, 2, RoundingMode.HALF_UP).doubleValue());
        } catch (Exception e) {
            logger.warn("腾讯指数日K兜底失败: {}", secid);
        }
    }
}