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
        // 涨跌家数统计：通过 clist 接口获取所有A股涨跌幅
        int riseCount = 0, fallCount = 0, limitUpCount = 0, limitDownCount = 0;
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
                            BigDecimal pct = new BigDecimal(item.get("f3").toString())
                                    .divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP);
                            double p = pct.doubleValue();
                            if (p > 0) riseCount++;
                            else if (p < 0) fallCount++;
                            if (p >= 9.9) limitUpCount++;
                            else if (p <= -9.9) limitDownCount++;
                        }
                    }
                }
            } catch (Exception e) {
                logger.error("解析涨跌家数统计失败", e);
            }
        }
        result.put("riseCount", riseCount);
        result.put("fallCount", fallCount);
        result.put("limitUpCount", limitUpCount);
        result.put("limitDownCount", limitDownCount);
        return result;
    }
}