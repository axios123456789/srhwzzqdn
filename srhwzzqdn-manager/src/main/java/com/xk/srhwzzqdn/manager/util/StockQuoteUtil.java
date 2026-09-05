package com.xk.srhwzzqdn.manager.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

/**
 * 股票实时行情数据获取工具
 * 基于东方财富 push2/push2his 接口获取实时报价、日K线、资金流向
 * 用于 AI 预测时注入当天实时数据，解决 AI 模型知识时效性问题
 */
public class StockQuoteUtil {
    private static final Logger logger = LoggerFactory.getLogger(StockQuoteUtil.class);

    private static final String QUOTE_URL = "http://push2.eastmoney.com/api/qt/stock/get";
    private static final String KLINE_URL = "http://push2his.eastmoney.com/api/qt/stock/kline/get";
    private static final String FLOW_URL = "http://push2.eastmoney.com/api/qt/stock/fflow/daykline/get";

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
     * HTTP GET 请求
     */
    private static String httpGet(String url) {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(url);
            request.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)");
            try (CloseableHttpResponse response = client.execute(request)) {
                if (response.getStatusLine().getStatusCode() == 200) {
                    return EntityUtils.toString(response.getEntity(), "UTF-8");
                }
            }
        } catch (Exception e) {
            logger.error("行情接口请求失败: {}", url, e);
        }
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
     * 返回 null 表示获取失败
     */
    private static String fetchRealtimeQuote(String stockCode) {
        String secid = buildSecId(stockCode);
        String url = QUOTE_URL + "?secid=" + secid +
                "&fields=f43,f44,f45,f46,f47,f48,f57,f58,f168,f169,f170,f171";
        String body = httpGet(url);
        if (body == null) return null;
        try {
            JSONObject json = JSON.parseObject(body);
            JSONObject d = json.getJSONObject("data");
            if (d == null) return null;
            StringBuilder sb = new StringBuilder();
            sb.append("【当日实时行情】\n");
            sb.append("股票名称：").append(d.getString("f58")).append("\n");
            sb.append("股票代码：").append(d.getString("f57")).append("\n");
            sb.append("最新价：").append(div100(d.get("f43"))).append(" 元\n");
            sb.append("涨跌额：").append(div100(d.get("f170"))).append(" 元\n");
            sb.append("涨跌幅：").append(div100(d.get("f169"))).append("%\n");
            sb.append("今开：").append(div100(d.get("f46"))).append(" 元\n");
            sb.append("最高：").append(div100(d.get("f44"))).append(" 元\n");
            sb.append("最低：").append(div100(d.get("f45"))).append(" 元\n");
            sb.append("振幅：").append(div100(d.get("f171"))).append("%\n");
            sb.append("换手率：").append(div100(d.get("f168"))).append("%\n");
            sb.append("成交量：").append(d.get("f47")).append(" 手\n");
            sb.append("成交额：").append(d.get("f48")).append(" 元\n");
            return sb.toString();
        } catch (Exception e) {
            logger.error("解析实时报价失败", e);
            return null;
        }
    }

    /**
     * 获取最近N根日K线并格式化为文本
     */
    private static String fetchKLineData(String stockCode, int count) {
        String secid = buildSecId(stockCode);
        String url = KLINE_URL + "?secid=" + secid +
                "&klt=101&fqt=1&end=20500101&lmt=" + count +
                "&fields1=f1,f2,f3,f4,f5,f6&fields2=f51,f52,f53,f54,f55,f56,f57,f58";
        String body = httpGet(url);
        if (body == null) return null;
        try {
            JSONObject json = JSON.parseObject(body);
            JSONObject d = json.getJSONObject("data");
            if (d == null) return null;
            JSONArray klines = d.getJSONArray("klines");
            if (klines == null || klines.isEmpty()) return null;
            StringBuilder sb = new StringBuilder();
            sb.append("【最近").append(klines.size()).append("日K线（日期,开盘,收盘,最高,最低,成交量手,成交额元,振幅%）】\n");
            for (int i = 0; i < klines.size(); i++) {
                sb.append(klines.getString(i)).append("\n");
            }
            return sb.toString();
        } catch (Exception e) {
            logger.error("解析K线数据失败", e);
            return null;
        }
    }

    /**
     * 获取最近N天资金流向并格式化为文本
     */
    private static String fetchMoneyFlow(String stockCode, int days) {
        String secid = buildSecId(stockCode);
        String url = FLOW_URL + "?secid=" + secid + "&lmt=" + days;
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
     * 任一接口失败不影响其他数据，返回的文本始终非 null
     */
    public static String getRealtimeMarketData(String stockCode) {
        StringBuilder sb = new StringBuilder();
        String quote = fetchRealtimeQuote(stockCode);
        String kline = fetchKLineData(stockCode, 30);
        String flow = fetchMoneyFlow(stockCode, 5);
        if (quote != null) sb.append(quote);
        else sb.append("【当日实时行情】获取失败\n");
        if (kline != null) sb.append(kline);
        else sb.append("【最近日K线】获取失败\n");
        if (flow != null) sb.append(flow);
        else sb.append("【资金流向】获取失败\n");
        return sb.toString();
    }

    /**
     * 获取单个指数涨跌幅（÷100）
     */
    private static Double fetchIndexChangePct(String secid) {
        String url = QUOTE_URL + "?secid=" + secid + "&fields=f169";
        String body = httpGet(url);
        if (body == null) return null;
        try {
            JSONObject json = JSON.parseObject(body);
            JSONObject d = json.getJSONObject("data");
            if (d == null) return null;
            return new BigDecimal(d.get("f169").toString())
                    .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP).doubleValue();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 获取指数成交额（元→亿元）
     */
    private static Double fetchIndexAmount(String secid) {
        String url = QUOTE_URL + "?secid=" + secid + "&fields=f48";
        String body = httpGet(url);
        if (body == null) return null;
        try {
            JSONObject json = JSON.parseObject(body);
            JSONObject d = json.getJSONObject("data");
            if (d == null) return null;
            return new BigDecimal(d.get("f48").toString())
                    .divide(new BigDecimal("100000000"), 2, RoundingMode.HALF_UP).doubleValue();
        } catch (Exception e) {
            return null;
        }
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
        String clistUrl = "http://push2.eastmoney.com/api/qt/clist/get?pn=1&pz=6000&po=1&np=1&fltt=2&invt=2" +
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
        String url = "http://push2ex.eastmoney.com/getTopic" + poolType +
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
        String url = KLINE_URL + "?secid=" + secid +
                "&klt=101&fqt=1&end=20500101&lmt=80" +
                "&fields1=f1,f2,f3,f4,f5,f6&fields2=f51,f52,f53,f54,f55,f56,f57,f58,f59";
        String body = httpGet(url);
        if (body == null) return;
        try {
            JSONObject json = JSON.parseObject(body);
            JSONObject d = json.getJSONObject("data");
            if (d == null) return;
            JSONArray klines = d.getJSONArray("klines");
            if (klines == null || klines.isEmpty()) return;
            // 定位日期 <= target 的最后一根K线
            int idx = -1;
            for (int i = 0; i < klines.size(); i++) {
                String kDate = klines.getString(i).split(",")[0];
                if (kDate.compareTo(target) <= 0) idx = i;
                else break;
            }
            if (idx < 0) return;
            String[] cur = klines.getString(idx).split(",");
            if (!result.containsKey("actualDate")) {
                result.put("actualDate", cur[0]);
            }
            result.put(pctKey, new BigDecimal(cur[8]).setScale(2, RoundingMode.HALF_UP).doubleValue());
            if (amountKey != null) {
                result.put(amountKey, new BigDecimal(cur[6])
                        .divide(new BigDecimal("100000000"), 2, RoundingMode.HALF_UP).doubleValue());
            }
        } catch (Exception e) {
            logger.error("按日期解析指数K线失败: {}", secid, e);
        }
    }
}