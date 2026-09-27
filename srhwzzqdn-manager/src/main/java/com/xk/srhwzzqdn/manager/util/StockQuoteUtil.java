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
        String url = getQuoteUrl() + "?secid=" + secid +
                "&fields=f43,f44,f45,f46,f47,f48,f57,f58,f168,f169,f170,f171";
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
                    return sb.toString();
                }
            } catch (Exception e) {
                logger.error("解析实时报价失败", e);
            }
        }
        return fetchRealtimeQuoteTx(stockCode);
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
                        for (int i = 0; i < klines.size(); i++) {
                            sb.append(klines.getString(i)).append("\n");
                        }
                        return sb.toString();
                    }
                }
            } catch (Exception e) {
                logger.error("解析K线数据失败", e);
            }
        }
        return fetchKLineDataTx(stockCode, count);
    }

    /**
     * 腾讯日K线兜底（东财失败/空时）：bar=[日期,开,收,高,低,量手]（可能混入分红对象，只取前6列）；
     * 振幅=(高-低)/前收×100，首根前收以开盘近似；腾讯无历史成交额列，文本标注说明
     */
    private static String fetchKLineDataTx(String stockCode, int count) {
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
            BigDecimal prevClose = null;
            for (int i = 0; i < days.size(); i++) {
                JSONArray a = days.getJSONArray(i);
                BigDecimal open = new BigDecimal(a.getString(1));
                BigDecimal close = new BigDecimal(a.getString(2));
                BigDecimal high = new BigDecimal(a.getString(3));
                BigDecimal low = new BigDecimal(a.getString(4));
                BigDecimal prev = prevClose != null ? prevClose : open;
                BigDecimal amp = prev.signum() == 0 ? BigDecimal.ZERO
                        : high.subtract(low).multiply(new BigDecimal("100")).divide(prev, 2, RoundingMode.HALF_UP);
                sb.append(a.getString(0)).append(",").append(open.toPlainString()).append(",")
                        .append(close.toPlainString()).append(",").append(high.toPlainString()).append(",")
                        .append(low.toPlainString()).append(",").append(a.size() > 5 ? a.getString(5) : "0").append(",")
                        .append(amp.toPlainString()).append("\n");
                prevClose = close;
            }
            return sb.toString();
        } catch (Exception e) {
            logger.warn("腾讯K线兜底失败 code={}", stockCode);
            return null;
        }
    }

    /**
     * 获取最近N天资金流向并格式化为文本
     */
    private static String fetchMoneyFlow(String stockCode, int days) {
        String secid = buildSecId(stockCode);
        String url = getFlowUrl() + "?secid=" + secid + "&lmt=" + days;
        String body = httpGet(url);
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
     * 获取股票完整实时行情文本（报价+K线+资金流向）
     * 用于注入 AI 预测提示词
     * 三个请求相互独立 → 并行执行（原串行最坏叠加3×(3次重试+退避)的白等；并行后总耗时≈最慢块）
     * 任一接口失败不影响其他数据，返回的文本始终非 null
     */
    public static String getRealtimeMarketData(String stockCode) {
        ExecutorService ex = Executors.newFixedThreadPool(3);
        Future<String> quoteF = ex.submit(() -> fetchRealtimeQuote(stockCode));
        Future<String> klineF = ex.submit(() -> fetchKLineData(stockCode, 30));
        Future<String> flowF = ex.submit(() -> fetchMoneyFlow(stockCode, 5));
        ex.shutdown();
        String quote = joinQuietly(quoteF);
        String kline = joinQuietly(klineF);
        String flow = joinQuietly(flowF);
        StringBuilder sb = new StringBuilder();
        if (quote != null) sb.append(quote);
        else sb.append("【当日实时行情】获取失败\n");
        if (kline != null) sb.append(kline);
        else sb.append("【最近日K线】获取失败\n");
        if (flow != null) sb.append(flow);
        else sb.append("【资金流向】获取失败\n");
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