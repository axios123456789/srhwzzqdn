package com.xk.srhwzzqdn.manager.assetControlArea.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.xk.srhwzzqdn.manager.assetControlArea.mapper.StockAssetMapper;
import com.xk.srhwzzqdn.manager.assetControlArea.service.StockAssetService;
import com.xk.srhwzzqdn.model.dto.assetControl.StockQueryDto;
import com.xk.srhwzzqdn.model.entity.assetControl.*;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class StockAssetServiceImpl implements StockAssetService {

    private static final Logger logger = LoggerFactory.getLogger(StockAssetServiceImpl.class);

    @Autowired
    private StockAssetMapper stockAssetMapper;

    @Autowired
    private com.xk.srhwzzqdn.manager.util.AiCommonUtil aiCommonUtil;

    private static final String QUOTE_URL = "http://push2.eastmoney.com/api/qt/stock/get";
    private static final String KLINE_URL = "http://push2his.eastmoney.com/api/qt/stock/kline/get";
    private static final String FLOW_URL = "http://push2.eastmoney.com/api/qt/stock/fflow/daykline/get";
    private static final String FINANCE_URL = "https://datacenter.eastmoney.com/securities/api/data/get";
    private static final String ANNOUNCE_URL = "https://np-anotice-stock.eastmoney.com/api/security/ann";
    private static final String NEWS_SEARCH_URL = "https://search-api-web.eastmoney.com/search/jsonp";

    // ===== AI 分析结果缓存：以数据指纹为 key，输入数据未变时直接复用上次分析结果，避免重复等待 AI 生成 =====
    // 指纹覆盖分析所用的全部输入（行情估值/K线/财务/资金/消息/筹码/板块/大盘实时），任一数据变化则指纹变化，
    // 缓存命中返回的结果与实时重新分析完全一致，属于纯性能优化
    private static final long AI_ANALYSIS_CACHE_TTL_MS = 10 * 60 * 1000L; // 指纹未变时结果复用有效期
    private static final int AI_ANALYSIS_CACHE_MAX = 32;                  // 缓存条目上限，防内存膨胀
    private final Map<String, AiAnalysisCacheEntry> aiAnalysisCache = new ConcurrentHashMap<>();

    private static class AiAnalysisCacheEntry {
        final Map<String, Object> result;
        final long expireAt;

        AiAnalysisCacheEntry(Map<String, Object> result, long expireAt) {
            this.result = result;
            this.expireAt = expireAt;
        }
    }

    private static String buildSecId(String stockCode) {
        if (stockCode == null || stockCode.isEmpty()) return "";
        String code = stockCode.trim();
        String market = code.startsWith("6") ? "1" : "0";
        return market + "." + code;
    }

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
            logger.error("HTTP请求失败: {}", url, e);
        }
        return null;
    }

    private static BigDecimal div100(Object val) {
        if (val == null) return null;
        try {
            return new BigDecimal(val.toString()).divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP);
        } catch (Exception e) {
            return null;
        }
    }

    private static BigDecimal div1e8(Object val) {
        if (val == null) return null;
        try {
            return new BigDecimal(val.toString()).divide(new BigDecimal("100000000"), 4, RoundingMode.HALF_UP);
        } catch (Exception e) {
            return null;
        }
    }

    private static BigDecimal div1e4(Object val) {
        if (val == null) return null;
        try {
            return new BigDecimal(val.toString()).divide(new BigDecimal("10000"), 4, RoundingMode.HALF_UP);
        } catch (Exception e) {
            return null;
        }
    }

    private static BigDecimal toDecimal(Object val) {
        if (val == null) return null;
        try {
            return new BigDecimal(val.toString());
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 分批插入K线：全量历史日K可达数千根，单条SQL过长易超限，按500根一批插入
     */
    private void insertKlineInBatches(List<StockKline> list) {
        if (list == null || list.isEmpty()) return;
        int batchSize = 500;
        for (int i = 0; i < list.size(); i += batchSize) {
            stockAssetMapper.batchAddStockKline(list.subList(i, Math.min(i + batchSize, list.size())));
        }
    }

    @Override
    public String getStockAllDataByCode(String stockCode) throws Exception {
        if (stockAssetMapper.isExistByCode(stockCode) > 0) {
            return "数据已存在";
        }

        String secid = buildSecId(stockCode);
        if (secid.isEmpty()) {
            return "未找到：股票代码为空";
        }

        StockBasic stockBasic = fetchStockBasic(secid, stockCode);
        if (stockBasic == null || stockBasic.getStockName() == null) {
            return "未找到：无此股票代码";
        }

        stockBasic.setStockCode(stockCode);
        stockBasic.setCreateBy("system");
        stockAssetMapper.addStockBasic(stockBasic);

        // 日K 300根 / 周K 250根 / 月K 300根（月K覆盖全部历史）
        for (int[] kt : new int[][]{{1, 300}, {2, 250}, {3, 300}}) {
            List<StockKline> klineList = fetchKlineData(secid, stockCode, kt[0], kt[1]);
            if (!klineList.isEmpty()) {
                stockAssetMapper.batchAddStockKline(klineList);
            }
        }

        // 财务指标（全部历史报告期）
        List<StockFinance> financeList = fetchFinanceData(stockCode);
        if (!financeList.isEmpty()) {
            stockAssetMapper.batchAddStockFinance(financeList);
        }

        // 股东人数历史（筹码结构，insert ignore按期去重）
        List<StockHolderNum> holderList = fetchHolderNumData(stockCode);
        if (!holderList.isEmpty()) {
            stockAssetMapper.batchAddStockHolderNum(holderList);
        }

        List<StockCapitalFlow> flowList = fetchCapitalFlowData(secid, stockCode, 30);
        if (!flowList.isEmpty()) {
            stockAssetMapper.batchAddStockCapitalFlow(flowList);
        }

        return "成功获取股票数据：" + stockBasic.getStockName() + "（" + stockCode + "），含行情/K线/财务/股东人数/资金流";
    }

    private StockBasic fetchStockBasic(String secid, String stockCode) {
        String fields = "f43,f44,f45,f46,f47,f48,f50,f57,f58,f59,f60,f84,f85,f116,f117,f126,f127,f128,f129,f162,f163,f164,f165,f167,f168,f169,f170,f171,f173,f184,f186,f187,f188,f189,f277,f292";
        String url = QUOTE_URL + "?secid=" + secid + "&fields=" + fields;
        String body = httpGet(url);
        if (body == null) return null;
        try {
            JSONObject json = JSON.parseObject(body);
            JSONObject d = json.getJSONObject("data");
            if (d == null) return null;

            StockBasic stock = new StockBasic();
            stock.setStockCode(d.getString("f57"));
            stock.setStockName(d.getString("f58"));
            stock.setMarket(stockCode.startsWith("6") ? 1 : 0);
            // f127=所属行业、f128=所属板块、f129=所属概念
            stock.setIndustry(d.getString("f127"));
            stock.setSector(d.getString("f128"));
            stock.setConceptSectors(d.getString("f129"));
            // 注意：f169=涨跌额、f170=涨跌幅；f162=PE(动)、f163=PE(TTM)、f164=PE(静)；
            // f277 是总股本而非PE，f292 非市销率，f189 是上市日期而非外盘，f186=毛利率、f187=净利率（非委比/委差），均不能错用
            stock.setLastPrice(div100(d.get("f43")));
            stock.setChangePct(div100(d.get("f170")));
            stock.setChangeAmount(div100(d.get("f169")));
            stock.setAmplitude(div100(d.get("f171")));
            stock.setTurnoverRate(div100(d.get("f168")));
            stock.setTotalMarketCap(div1e8(d.get("f116")));
            stock.setCircMarketCap(div1e8(d.get("f117")));
            stock.setTotalShares(div1e4(d.get("f84")));
            stock.setCircShares(div1e4(d.get("f85")));
            stock.setPeStatic(div100(d.get("f164")));
            stock.setPeDynamic(div100(d.get("f162")));
            stock.setPeTtm(div100(d.get("f163")));
            stock.setPbRatio(div100(d.get("f167")));
            stock.setVolume(d.getLong("f47"));
            stock.setTurnover(div1e8(d.get("f48")));
            stock.setVolumeRatio(div100(d.get("f50")));
            // f126=股息率(%)、f165=市销率PS(TTM)（已实测验证：工行3.94/新天然气2.98/海油3.67均与官网吻合）
            stock.setDividendYield(div100(d.get("f126")));
            stock.setPsRatio(div100(d.get("f165")));
            // f189 为上市日期（如 20010827）
            String listDateStr = d.getString("f189");
            if (listDateStr != null && listDateStr.length() == 8) {
                try {
                    stock.setListDate(new SimpleDateFormat("yyyyMMdd").parse(listDateStr));
                } catch (Exception ignored) {
                }
            }
            return stock;
        } catch (Exception e) {
            logger.error("解析股票基本数据失败", e);
            return null;
        }
    }

    private List<StockKline> fetchKlineData(String secid, String stockCode, int klineType, int count) {
        String url = KLINE_URL + "?secid=" + secid +
                "&klt=" + (klineType == 1 ? 101 : klineType == 2 ? 102 : 103) +
                "&fqt=1&end=20500101&lmt=" + count +
                "&fields1=f1,f2,f3,f4,f5,f6&fields2=f51,f52,f53,f54,f55,f56,f57,f58,f59,f60,f61";
        String body = httpGet(url);
        if (body == null) return Collections.emptyList();
        try {
            JSONObject json = JSON.parseObject(body);
            JSONObject d = json.getJSONObject("data");
            if (d == null) return Collections.emptyList();
            JSONArray klines = d.getJSONArray("klines");
            if (klines == null || klines.isEmpty()) return Collections.emptyList();

            List<StockKline> list = new ArrayList<>();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            for (int i = 0; i < klines.size(); i++) {
                String[] parts = klines.getString(i).split(",");
                if (parts.length < 8) continue;
                StockKline k = new StockKline();
                k.setStockCode(stockCode);
                k.setTradeDate(sdf.parse(parts[0]));
                k.setKlineType(klineType);
                k.setOpenPrice(new BigDecimal(parts[1]));
                k.setClosePrice(new BigDecimal(parts[2]));
                k.setHighPrice(new BigDecimal(parts[3]));
                k.setLowPrice(new BigDecimal(parts[4]));
                k.setVolume(Long.parseLong(parts[5]));
                k.setTurnover(new BigDecimal(parts[6]));
                k.setAmplitude(new BigDecimal(parts[7]));
                if (parts.length > 8) k.setChangePct(new BigDecimal(parts[8]));
                if (parts.length > 9) k.setChangeAmount(new BigDecimal(parts[9]));
                if (parts.length > 10) k.setTurnoverRate(new BigDecimal(parts[10]));
                list.add(k);
            }
            return list;
        } catch (Exception e) {
            logger.error("解析K线数据失败", e);
            return Collections.emptyList();
        }
    }

    private List<StockCapitalFlow> fetchCapitalFlowData(String secid, String stockCode, int days) {
        // 必须带 klt 与 fields 参数，否则接口返回 data:null
        String url = FLOW_URL + "?secid=" + secid + "&lmt=" + days +
                "&klt=1&fields1=f1,f2,f3,f7&fields2=f51,f52,f53,f54,f55,f56,f57,f58,f59,f60,f61,f62,f63,f64,f65";
        String body = httpGet(url);
        if (body == null) return Collections.emptyList();
        try {
            JSONObject json = JSON.parseObject(body);
            JSONObject d = json.getJSONObject("data");
            if (d == null) return Collections.emptyList();
            JSONArray klines = d.getJSONArray("klines");
            if (klines == null || klines.isEmpty()) return Collections.emptyList();

            List<StockCapitalFlow> list = new ArrayList<>();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            for (int i = 0; i < klines.size(); i++) {
                String[] parts = klines.getString(i).split(",");
                if (parts.length < 5) continue;
                StockCapitalFlow f = new StockCapitalFlow();
                f.setStockCode(stockCode);
                f.setTradeDate(sdf.parse(parts[0]));
                f.setMainNetInflow(div1e4(new BigDecimal(parts[1])));
                f.setSmallNet(div1e4(new BigDecimal(parts[2])));
                f.setMediumNet(div1e4(new BigDecimal(parts[3])));
                f.setLargeNet(div1e4(new BigDecimal(parts[4])));
                if (parts.length > 5) f.setSuperLargeNet(div1e4(new BigDecimal(parts[5])));
                list.add(f);
            }
            return list;
        } catch (Exception e) {
            logger.error("解析资金流向数据失败", e);
            return Collections.emptyList();
        }
    }

    /**
     * 获取全部历史报告期的主要财务指标（东方财富F10接口，ps=100实测可一次取回全部历史，如工行85期2003年至今）
     * 金额统一换算为亿元，比率为原值
     */
    private List<StockFinance> fetchFinanceData(String stockCode) {
        String secucode = stockCode + (stockCode.startsWith("6") ? ".SH" : ".SZ");
        String url = FINANCE_URL +
                "?type=RPT_F10_FINANCE_MAINFINADATA&sty=APP_F10_MAINFINADATA" +
                "&filter=(SECUCODE%3D%22" + secucode + "%22)" +
                "&p=1&ps=100&sr=-1&st=REPORT_DATE&source=HSF10&client=PC";
        String body = httpGet(url);
        if (body == null) return Collections.emptyList();
        try {
            JSONObject json = JSON.parseObject(body);
            JSONObject result = json.getJSONObject("result");
            if (result == null) return Collections.emptyList();
            JSONArray rows = result.getJSONArray("data");
            if (rows == null || rows.isEmpty()) return Collections.emptyList();

            List<StockFinance> list = new ArrayList<>();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            for (int i = 0; i < rows.size(); i++) {
                JSONObject row = rows.getJSONObject(i);
                StockFinance f = new StockFinance();
                f.setStockCode(stockCode);
                String rd = row.getString("REPORT_DATE");
                if (rd != null && rd.length() >= 10) {
                    try {
                        f.setReportDate(sdf.parse(rd.substring(0, 10)));
                    } catch (Exception ignored) {
                    }
                }
                f.setRevenue(div1e8(row.get("TOTALOPERATEREVE")));
                f.setRevenueYoy(toDecimal(row.get("TOTALOPERATEREVETZ")));
                f.setNetProfit(div1e8(row.get("PARENTNETPROFIT")));
                f.setNetProfitYoy(toDecimal(row.get("PARENTNETPROFITTZ")));
                f.setDeductNetProfit(div1e8(row.get("KCFJCXSYJLR")));
                f.setGrossMargin(toDecimal(row.get("XSMLL")));
                f.setNetMargin(toDecimal(row.get("XSJLL")));
                f.setRoe(toDecimal(row.get("ROEJQ")));
                f.setRoa(toDecimal(row.get("ZZCJLL")));
                f.setRoic(toDecimal(row.get("ROIC")));
                f.setDebtRatio(toDecimal(row.get("ZCFZL")));
                f.setCurrentRatio(toDecimal(row.get("LD")));
                f.setQuickRatio(toDecimal(row.get("SD")));
                f.setEps(toDecimal(row.get("EPSJB")));
                f.setBps(toDecimal(row.get("BPS")));
                f.setCashflowPerShare(toDecimal(row.get("MGJYXJJE")));
                f.setRevenueGrowth1y(f.getRevenueYoy());
                f.setProfitGrowth1y(f.getNetProfitYoy());
                list.add(f);
            }
            return list;
        } catch (Exception e) {
            logger.error("解析财务数据失败", e);
            return Collections.emptyList();
        }
    }

    /**
     * 抓取股东人数历史（东财 RPT_F10_EH_HOLDERNUM，全量历史各期）
     * 股东人数下降=筹码集中（主力吸筹）；上升=筹码分散（散户接盘）
     */
    private List<StockHolderNum> fetchHolderNumData(String stockCode) {
        String secucode = stockCode + (stockCode.startsWith("6") ? ".SH" : ".SZ");
        String url = "https://datacenter-web.eastmoney.com/api/data/v1/get" +
                "?reportName=RPT_F10_EH_HOLDERNUM&columns=ALL" +
                "&filter=(SECUCODE%3D%22" + secucode + "%22)" +
                "&pageNumber=1&pageSize=100&sortTypes=-1&sortColumns=END_DATE&source=WEB&client=WEB";
        String body = httpGet(url);
        if (body == null) return Collections.emptyList();
        try {
            JSONObject json = JSON.parseObject(body);
            JSONObject result = json.getJSONObject("result");
            if (result == null) return Collections.emptyList();
            JSONArray rows = result.getJSONArray("data");
            if (rows == null || rows.isEmpty()) return Collections.emptyList();

            List<StockHolderNum> list = new ArrayList<>();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            for (int i = 0; i < rows.size(); i++) {
                JSONObject row = rows.getJSONObject(i);
                StockHolderNum h = new StockHolderNum();
                h.setStockCode(stockCode);
                // RPT_F10_EH_HOLDERNUM：HOLDER_TOTAL_NUM=股东户数，TOTAL_NUM_RATIO=较上期变化率%，
                // AVG_HOLD_AMT=户均持股市值(元)，AVG_FREE_SHARES=户均持股数，
                // HOLDER_TOTAL_NUMCHANGE=较上期变化户数，NOTICE_DATE=公告日期
                h.setStockName(row.getString("SECURITY_NAME_ABBR"));
                h.setHolderNum(row.getLong("HOLDER_TOTAL_NUM"));
                h.setHolderNumRatio(toDecimal(row.get("TOTAL_NUM_RATIO")));
                h.setAvgMarketCap(toDecimal(row.get("AVG_HOLD_AMT")));
                h.setAvgHoldNum(toDecimal(row.get("AVG_FREE_SHARES")));
                h.setHolderNumChange(row.getLong("HOLDER_TOTAL_NUMCHANGE"));
                String nd = row.getString("NOTICE_DATE");
                if (nd != null && nd.length() >= 10) {
                    try {
                        h.setNoticeDate(sdf.parse(nd.substring(0, 10)));
                    } catch (Exception ignored) {
                    }
                }
                String ed = row.getString("END_DATE");
                if (ed != null && ed.length() >= 10) {
                    try {
                        h.setEndDate(sdf.parse(ed.substring(0, 10)));
                    } catch (Exception ignored) {
                    }
                }
                if (h.getEndDate() != null && h.getHolderNum() != null) {
                    list.add(h);
                }
            }
            return list;
        } catch (Exception e) {
            logger.error("解析股东人数数据失败: {}", stockCode, e);
            return Collections.emptyList();
        }
    }

    /**
     * 抓取个股新闻（东财搜索接口，按股票名称、按时间排序，取最近2页共40条）
     */
    private List<StockNews> fetchNewsList(String stockCode, String stockName) {
        if (stockName == null || stockName.isEmpty()) return Collections.emptyList();
        List<StockNews> list = new ArrayList<>();
        for (int page = 1; page <= 2; page++) {
            String param = "{\"uid\":\"\",\"keyword\":\"" + stockName + "\",\"type\":[\"cmsArticleWebOld\"]," +
                    "\"client\":\"web\",\"clientType\":\"web\",\"clientVersion\":\"curr\"," +
                    "\"param\":{\"cmsArticleWebOld\":{\"searchScope\":\"default\",\"sort\":\"time\"," +
                    "\"pageIndex\":" + page + ",\"pageSize\":20,\"preTag\":\"\",\"postTag\":\"\"}}}";
            try {
                String url = NEWS_SEARCH_URL + "?cb=jQuery&param=" + URLEncoder.encode(param, "UTF-8");
                String body = httpGet(url);
                if (body == null) continue;
                // 去掉 jsonp 包裹：jQuery({...})
                int start = body.indexOf('(');
                int end = body.lastIndexOf(')');
                if (start < 0 || end <= start) continue;
                JSONObject json = JSON.parseObject(body.substring(start + 1, end));
                JSONObject result = json.getJSONObject("result");
                if (result == null) continue;
                JSONArray articles = result.getJSONArray("cmsArticleWebOld");
                if (articles == null || articles.isEmpty()) continue;
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                for (int i = 0; i < articles.size(); i++) {
                    JSONObject a = articles.getJSONObject(i);
                    StockNews n = new StockNews();
                    n.setStockCode(stockCode);
                    n.setTitle(a.getString("title"));
                    n.setSummary(a.getString("content"));
                    n.setSource(a.getString("mediaName"));
                    n.setNewsUrl(a.getString("url"));
                    String date = a.getString("date");
                    if (date != null && date.length() >= 19) {
                        try {
                            n.setPublishTime(sdf.parse(date.substring(0, 19)));
                        } catch (Exception ignored) {
                        }
                    }
                    n.setNewsType(1);
                    if (n.getTitle() != null && !n.getTitle().isEmpty()) list.add(n);
                }
                Thread.sleep(150);
            } catch (Exception e) {
                logger.error("抓取个股新闻失败: {}", stockCode, e);
            }
        }
        return list;
    }

    /**
     * 抓取个股公告列表（东财公告接口，取最近20条）
     */
    private List<StockNews> fetchAnnouncementList(String stockCode) {
        List<StockNews> list = new ArrayList<>();
        String url = ANNOUNCE_URL + "?sr=-1&page_size=20&page_index=1&ann_type=A&client_source=web" +
                "&stock_list=" + stockCode + "&f_node=0&s_node=0";
        String body = httpGet(url);
        if (body == null) return list;
        try {
            JSONObject json = JSON.parseObject(body);
            JSONObject data = json.getJSONObject("data");
            if (data == null) return list;
            JSONArray arr = data.getJSONArray("list");
            if (arr == null || arr.isEmpty()) return list;
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            for (int i = 0; i < arr.size(); i++) {
                JSONObject a = arr.getJSONObject(i);
                StockNews n = new StockNews();
                n.setStockCode(stockCode);
                n.setTitle(a.getString("title"));
                n.setSource("公司公告");
                String artCode = a.getString("art_code");
                if (artCode != null) {
                    n.setNewsUrl("https://data.eastmoney.com/notices/detail/" + stockCode + "/" + artCode + ".html");
                }
                // notice_date 如 2026-08-15 00:00:00，为公告归属日期
                String nd = a.getString("notice_date");
                if (nd != null && nd.length() >= 19) {
                    try {
                        n.setPublishTime(sdf.parse(nd.substring(0, 19)));
                    } catch (Exception ignored) {
                    }
                }
                n.setNewsType(2);
                if (n.getTitle() != null && !n.getTitle().isEmpty()) list.add(n);
            }
        } catch (Exception e) {
            logger.error("抓取个股公告失败: {}", stockCode, e);
        }
        return list;
    }

    /**
     * 批量刷新所有股票的最新实时数据：
     * 更新实时行情（部分字段更新，不覆盖行业/公司信息等），
     * 重建三种周期K线与资金流向，补全缺失的财务数据，增量补全消息面
     */
    @Override
    public String refreshAllStockRealtime() {
        List<StockBasic> all = stockAssetMapper.getAllStockBasic();
        if (all == null || all.isEmpty()) {
            return "暂无股票，请先通过【数据获取】添加股票";
        }
        int ok = 0, fail = 0;
        for (StockBasic item : all) {
            if (refreshSingleStock(item.getStockCode(), "system")) {
                ok++;
            } else {
                fail++;
            }
            try {
                Thread.sleep(200); // 限速，避免触发接口风控
            } catch (InterruptedException ignored) {
            }
        }
        return "实时数据刷新完成：共" + all.size() + "只，成功" + ok + "只，失败" + fail + "只";
    }

    /**
     * 刷新单只股票全部实时数据：行情估值/K线(三周期)/资金流/财务(按报告期刷新)/消息(增量去重)
     *
     * @return true=成功 false=失败
     */
    private boolean refreshSingleStock(String code, String updateBy) {
        try {
            String secid = buildSecId(code);
            StockBasic quote = fetchStockBasic(secid, code);
            if (quote == null || quote.getLastPrice() == null) {
                return false;
            }
            quote.setStockCode(code);
            quote.setUpdateBy(updateBy);
            stockAssetMapper.updateStockRealtime(quote);

            // 重建K线（先抓全部周期且都成功，才删旧插新，避免接口限流失败导致旧K线丢失；请求间200ms限流）
            boolean klineAllOk = true;
            List<List<StockKline>> klineBatches = new ArrayList<>();
            for (int[] kt : new int[][]{{1, 10000}, {2, 2000}, {3, 600}}) {
                List<StockKline> ks = fetchKlineData(secid, code, kt[0], kt[1]);
                if (ks == null || ks.isEmpty()) {
                    klineAllOk = false;
                    break;
                }
                klineBatches.add(ks);
                Thread.sleep(200);   // 反爬限流
            }
            if (klineAllOk) {
                stockAssetMapper.deleteStockKlineByCode(code);
                for (List<StockKline> ks : klineBatches) {
                    insertKlineInBatches(ks);
                }
            } else {
                logger.warn("股票{}K线抓取不完整（可能限流），保留旧K线数据", code);
            }

            // 重建资金流向（先抓成功再删旧，避免限流失败导致旧资金流丢失）
            List<StockCapitalFlow> fs = fetchCapitalFlowData(secid, code, 30);
            if (fs != null && !fs.isEmpty()) {
                stockAssetMapper.deleteStockCapitalFlowByCode(code);
                stockAssetMapper.batchAddStockCapitalFlow(fs);
            } else {
                logger.warn("股票{}资金流抓取失败（可能限流），保留旧资金流数据", code);
            }
            Thread.sleep(200);   // 反爬限流

            // 财务数据：删旧插新，新报告期自动入库、已有报告期随最新披露修正
            List<StockFinance> fins = fetchFinanceData(code);
            if (!fins.isEmpty()) {
                stockAssetMapper.deleteStockFinanceByCode(code);
                stockAssetMapper.batchAddStockFinance(fins);
            }

            // 增量补全股东人数历史（唯一索引去重，新披露期数自动入库）
            List<StockHolderNum> holders = fetchHolderNumData(code);
            if (!holders.isEmpty()) {
                stockAssetMapper.batchAddStockHolderNum(holders);
            }

            // 增量补全消息面（新闻+公告，唯一索引去重，历史保留）
            StockBasic basic = stockAssetMapper.getStockBasicByCode(code);
            List<StockNews> news = fetchNewsList(code, basic != null ? basic.getStockName() : "");
            news.addAll(fetchAnnouncementList(code));
            if (!news.isEmpty()) {
                stockAssetMapper.batchAddStockNews(news);
            }
            return true;
        } catch (Exception e) {
            logger.error("刷新股票实时数据失败: {}", code, e);
            return false;
        }
    }

    @Override
    public String refreshStockRealtime(String stockCode) {
        StockBasic basic = stockAssetMapper.getStockBasicByCode(stockCode);
        if (basic == null) {
            return "未找到股票：" + stockCode;
        }
        boolean ok = refreshSingleStock(stockCode, "admin");
        return ok ? "股票 " + basic.getStockName() + "（" + stockCode + "）实时数据刷新成功：行情估值/K线/资金流/财务/消息已更新"
                : "股票 " + basic.getStockName() + "（" + stockCode + "）实时数据刷新失败，请查看后端日志";
    }

    @Override
    public StockBasic getStockBasicByCode(String stockCode) {
        return stockAssetMapper.getStockBasicByCode(stockCode);
    }

    @Override
    public PageInfo<StockBasic> getStockListByCondition(Integer current, Integer limit, StockQueryDto dto) {
        PageHelper.startPage(current, limit);
        List<StockBasic> list = stockAssetMapper.getStockListByCondition(dto);
        return new PageInfo<>(list);
    }

    @Override
    public List<StockKline> getStockKline(String stockCode, Integer klineType, Integer limit) {
        return stockAssetMapper.getStockKline(stockCode, klineType, limit);
    }

    @Override
    public List<StockFinance> getStockFinance(String stockCode, Integer limit) {
        return stockAssetMapper.getStockFinance(stockCode, limit);
    }

    @Override
    public List<StockCapitalFlow> getStockCapitalFlow(String stockCode, Integer limit) {
        return stockAssetMapper.getStockCapitalFlow(stockCode, limit);
    }

    @Override
    public List<StockNews> getStockNews(String stockCode, Integer limit) {
        return stockAssetMapper.getStockNews(stockCode, limit);
    }

    @Override
    public String refreshStockNews(String stockCode) {
        StockBasic basic = stockAssetMapper.getStockBasicByCode(stockCode);
        if (basic == null) {
            return "未找到股票：" + stockCode;
        }
        List<StockNews> news = fetchNewsList(stockCode, basic.getStockName());
        news.addAll(fetchAnnouncementList(stockCode));
        if (!news.isEmpty()) {
            stockAssetMapper.batchAddStockNews(news);
        }
        return "消息刷新完成：本次抓取" + news.size() + "条（重复自动去重）";
    }

    @Override
    public List<StockHolderNum> getStockHolderNum(String stockCode, Integer limit) {
        return stockAssetMapper.getStockHolderNum(stockCode, limit == null ? 60 : limit);
    }

    /**
     * AI 综合分析：聚合行情估值/K线位置/技术指标/板块/资金/财务/消息等数据，
     * 先做规则化评分（确定性），再调用公共 AI 接口生成综合分析报告
     */
    @Override
    public Map<String, Object> analyzeStock(String stockCode) {
        StockBasic basic = stockAssetMapper.getStockBasicByCode(stockCode);
        if (basic == null) {
            throw new RuntimeException("未找到股票：" + stockCode);
        }
        // ===== 数据源保障：数据库优先；缺失的维度自动调接口补抓并入库，避免评分因缺数据而失真 =====
        String secid = buildSecId(stockCode);
        List<StockKline> daily = stockAssetMapper.getStockKline(stockCode, 1, 300);
        if (daily.isEmpty()) {
            List<StockKline> fetched = fetchKlineData(secid, stockCode, 1, 300);
            if (fetched != null && !fetched.isEmpty()) {
                stockAssetMapper.batchAddStockKline(fetched);
                daily = stockAssetMapper.getStockKline(stockCode, 1, 300);
            }
        }
        Collections.reverse(daily); // 转为时间升序
        if (daily.isEmpty()) {
            throw new RuntimeException("该股票无K线数据，请先获取/刷新数据");
        }
        List<StockFinance> finances = stockAssetMapper.getStockFinance(stockCode, 12);
        if (finances.isEmpty()) {
            finances = fetchFinanceData(stockCode);
            if (!finances.isEmpty()) {
                stockAssetMapper.batchAddStockFinance(finances);
                finances = stockAssetMapper.getStockFinance(stockCode, 12);
            }
        }
        List<StockCapitalFlow> flows = stockAssetMapper.getStockCapitalFlow(stockCode, 15);
        if (flows.size() < 15) {
            // 资金流不足15天（如曾因接口限流只存下零星几天），重抓30天完整窗口再入库
            List<StockCapitalFlow> fetched = fetchCapitalFlowData(secid, stockCode, 30);
            if (fetched != null && fetched.size() > flows.size()) {
                stockAssetMapper.deleteStockCapitalFlowByCode(stockCode);
                stockAssetMapper.batchAddStockCapitalFlow(fetched);
                flows = stockAssetMapper.getStockCapitalFlow(stockCode, 15);
            }
        }
        List<StockNews> newsList = stockAssetMapper.getStockNews(stockCode, 15);
        if (newsList.isEmpty()) {
            List<StockNews> fetched = fetchNewsList(stockCode, basic.getStockName() == null ? "" : basic.getStockName());
            fetched.addAll(fetchAnnouncementList(stockCode));
            if (!fetched.isEmpty()) {
                stockAssetMapper.batchAddStockNews(fetched);
                newsList = stockAssetMapper.getStockNews(stockCode, 15);
            }
        }

        // ===== 技术指标计算 =====
        List<Double> closes = new ArrayList<>();
        List<Double> highs = new ArrayList<>();
        List<Double> lows = new ArrayList<>();
        for (StockKline k : daily) {
            closes.add(k.getClosePrice() == null ? 0d : k.getClosePrice().doubleValue());
            highs.add(k.getHighPrice() == null ? 0d : k.getHighPrice().doubleValue());
            lows.add(k.getLowPrice() == null ? 0d : k.getLowPrice().doubleValue());
        }
        double ma5 = avgLast(closes, 5), ma10 = avgLast(closes, 10), ma20 = avgLast(closes, 20), ma60 = avgLast(closes, 60);
        double[] macd = calcMacd(closes); // [dif, dea, hist]
        double[] kdj = calcKdj(highs, lows, closes, 9); // [k, d, j]
        double rsi14 = calcRsi(closes, 14);
        double lastClose = closes.get(closes.size() - 1);

        // 股价位置：近250日（约一年）区间
        int posN = Math.min(250, closes.size());
        double yearHigh = highs.stream().skip(closes.size() - posN).mapToDouble(Double::doubleValue).max().orElse(0);
        double yearLow = lows.stream().skip(closes.size() - posN).mapToDouble(Double::doubleValue).min().orElse(0);
        double positionPct = yearHigh > yearLow ? (lastClose - yearLow) / (yearHigh - yearLow) * 100 : 50;
        double drawdownFromHigh = yearHigh > 0 ? (lastClose - yearHigh) / yearHigh * 100 : 0;

        // ===== 板块与大盘实时环境（提前到数据阶段抓取，取值与用法不变，仅为缓存指纹构建做准备）=====
        JSONObject sectorInfo = fetchSectorInfo(basic.getIndustry());
        JSONObject marketInfo = fetchMarketInfo();

        // ===== 评分前置数据（筹码、前一日指标、位置分档）=====
        List<StockHolderNum> holders = stockAssetMapper.getStockHolderNum(stockCode, 8);
        if (holders.isEmpty()) {
            holders = fetchHolderNumData(stockCode);
            if (!holders.isEmpty()) {
                stockAssetMapper.batchAddStockHolderNum(holders);
                holders = stockAssetMapper.getStockHolderNum(stockCode, 8);
            }
        }
        // ===== AI 分析结果缓存查询：数据指纹未变（行情/K线/财务/资金/消息/筹码/板块/大盘与上次一致）时直接复用上次报告 =====
        String aiFingerprint = buildAiAnalysisFingerprint(stockCode, basic, daily, finances, flows, newsList, holders, sectorInfo, marketInfo);
        AiAnalysisCacheEntry cachedEntry = aiAnalysisCache.get(aiFingerprint);
        if (cachedEntry != null && System.currentTimeMillis() < cachedEntry.expireAt) {
            logger.info("AI分析命中缓存 | stock={} | 跳过重复调用AI接口，直接返回上次结果", stockCode);
            return cachedEntry.result;
        }
        evictAiAnalysisCache();

        StockKline lastK = daily.get(daily.size() - 1);
        StockKline prevK = daily.size() > 1 ? daily.get(daily.size() - 2) : null;
        double[] macdPrev = closes.size() > 1 ? calcMacd(closes.subList(0, closes.size() - 1)) : macd;
        double[] kdjPrev = closes.size() > 1
                ? calcKdj(highs.subList(0, highs.size() - 1), lows.subList(0, lows.size() - 1), closes.subList(0, closes.size() - 1), 9)
                : kdj;
        double chg20 = closes.size() > 20 ? (lastClose / closes.get(closes.size() - 21) - 1) * 100 : 0;
        String posBand = positionPct >= 80 ? "高位" : positionPct >= 55 ? "中高位" : positionPct >= 30 ? "中低位" : "低位";
        SimpleDateFormat daySdf = new SimpleDateFormat("yyyy-MM-dd");

        // ===== 规则评分（每项加减分记录明细，展示得分构成，供AI解读与排查）=====
        List<String> techDetail = new ArrayList<>();
        List<String> fundDetail = new ArrayList<>();
        List<String> flowDetail = new ArrayList<>();
        List<String> newsDetail = new ArrayList<>();

        // ---------- 技术面 0-100（趋势20+排列15+斜率5+MACD15+KDJ10+RSI8+量价16+换手4+突破8，按位置修正）----------
        double tech = 0;
        // 1) 趋势：现价与各均线关系（20分）
        if (lastClose > ma5) { tech += 5; techDetail.add("+5 现价站上MA5(" + String.format("%.2f", ma5) + ")"); }
        if (lastClose > ma10) { tech += 5; techDetail.add("+5 现价站上MA10(" + String.format("%.2f", ma10) + ")"); }
        if (lastClose > ma20) { tech += 5; techDetail.add("+5 现价站上MA20(" + String.format("%.2f", ma20) + ")"); }
        if (lastClose > ma60) { tech += 5; techDetail.add("+5 现价站上MA60(" + String.format("%.2f", ma60) + ")"); }
        // 2) 均线排列（15分）
        boolean bullAll = ma5 > ma10 && ma10 > ma20 && ma20 > ma60;
        boolean bullShort = !bullAll && ma5 > ma10 && ma10 > ma20;
        boolean bearAll = ma5 < ma10 && ma10 < ma20 && ma20 < ma60;
        if (bullAll) { tech += 15; techDetail.add("+15 均线多头排列(MA5>MA10>MA20>MA60)"); }
        else if (bullShort) { tech += 10; techDetail.add("+10 短中期均线多头(MA5>MA10>MA20，MA60未确认)"); }
        else if (bearAll) { tech -= 12; techDetail.add("-12 均线空头排列(MA5<MA10<MA20<MA60)"); }
        // 3) MA20斜率（±5分）：与10日前的MA20对比
        boolean ma20Rising = false, ma20Falling = false;
        if (closes.size() > 30) {
            double ma20Prev10 = avgLast(closes.subList(0, closes.size() - 10), 20);
            ma20Rising = ma20 > ma20Prev10 * 1.002;
            ma20Falling = ma20 < ma20Prev10 * 0.998;
            if (ma20Rising) { tech += 5; techDetail.add("+5 MA20上行(中期趋势向好)"); }
            else if (ma20Falling) { tech -= 3; techDetail.add("-3 MA20下行(中期趋势偏弱)"); }
        }
        // 4) MACD动量（15分）
        if (macd[0] > macd[1]) { tech += 6; techDetail.add("+6 MACD金叉运行中(DIF>DEA)"); }
        if (macd[2] > 0) {
            if (macd[2] > macdPrev[2]) { tech += 6; techDetail.add("+6 MACD红柱放大(上涨动能增强)"); }
            else { tech += 2; techDetail.add("+2 MACD红柱缩小(上涨动能衰减)"); }
        }
        if (macd[0] > 0) { tech += 3; techDetail.add("+3 MACD零轴上方(多头市场)"); }
        else if (macd[0] > macdPrev[0]) { tech += 2; techDetail.add("+2 DIF零轴下方上拐(反弹信号)"); }
        // 5) KDJ（10分）
        boolean kdjGolden = kdj[0] > kdj[1] && kdjPrev[0] <= kdjPrev[1];
        if (kdj[0] > kdj[1]) { tech += 4; techDetail.add("+4 KDJ的K在D上方"); }
        if (kdjGolden) { tech += 4; techDetail.add("+4 KDJ刚形成金叉"); }
        if (kdj[0] > 85 || kdj[2] > 100) { tech -= 8; techDetail.add("-8 KDJ超买(K>85或J>100)"); }
        if (kdj[0] < 20 || kdj[2] < 0) {
            if (positionPct < 40) { tech += 6; techDetail.add("+6 KDJ超卖且股价处中低位(反弹弹性大)"); }
            else { tech += 2; techDetail.add("+2 KDJ超卖"); }
        }
        // 6) RSI（8分）
        if (rsi14 >= 45 && rsi14 <= 70) { tech += 8; techDetail.add("+8 RSI14=" + String.format("%.1f", rsi14) + "强势区(45~70)"); }
        else if ((rsi14 >= 40 && rsi14 < 45) || (rsi14 > 70 && rsi14 <= 75)) { tech += 3; techDetail.add("+3 RSI14=" + String.format("%.1f", rsi14) + "中性偏强"); }
        else if (rsi14 > 75) { tech -= 8; techDetail.add("-8 RSI14=" + String.format("%.1f", rsi14) + "超买"); }
        else if (rsi14 < 25) { tech += 4; techDetail.add("+4 RSI14=" + String.format("%.1f", rsi14) + "超卖"); }
        // 7) 量价配合（16分）
        double volRatio = (prevK != null && prevK.getVolume() != null && prevK.getVolume() > 0 && lastK.getVolume() != null)
                ? lastK.getVolume() / (double) prevK.getVolume() : 1;
        double todayChg = lastK.getChangePct() == null ? 0 : lastK.getChangePct().doubleValue();
        boolean volUp = volRatio >= 1.3, volDown = volRatio <= 0.7;
        if (todayChg > 0 && volUp) { tech += 8; techDetail.add("+8 放量上涨(量为昨日" + String.format("%.2f", volRatio) + "倍，量价健康)"); }
        else if (todayChg > 0 && volDown) { tech -= 3; techDetail.add("-3 缩量上涨(量为昨日" + String.format("%.2f", volRatio) + "倍，追高存疑)"); }
        else if (todayChg < 0 && volUp) { tech -= 10; techDetail.add("-10 放量下跌(量为昨日" + String.format("%.2f", volRatio) + "倍，出货嫌疑)"); }
        else if (todayChg < 0 && volDown) { tech -= 2; techDetail.add("-2 缩量回调(量为昨日" + String.format("%.2f", volRatio) + "倍，或为洗盘)"); }
        // 8) 换手率健康度（4分）
        double turnoverRateToday = lastK.getTurnoverRate() == null ? 0 : lastK.getTurnoverRate().doubleValue();
        if (turnoverRateToday >= 1 && turnoverRateToday <= 7) { tech += 4; techDetail.add("+4 换手率" + String.format("%.2f%%", turnoverRateToday) + "适中"); }
        else if (turnoverRateToday > 15 && positionPct >= 70) { tech -= 5; techDetail.add("-5 换手率" + String.format("%.2f%%", turnoverRateToday) + "高位过度换手"); }
        else if (turnoverRateToday > 0 && turnoverRateToday < 0.5) { tech -= 3; techDetail.add("-3 换手率" + String.format("%.2f%%", turnoverRateToday) + "流动性不足"); }
        // 9) 突破与位置修正（+8/-8/×0.9/+5）
        boolean newHigh = positionPct >= 99 || lastClose >= yearHigh * 0.995;
        if (newHigh && todayChg > 0 && volUp) { tech += 8; techDetail.add("+8 放量创年内新高(突破有效)"); }
        else if (newHigh && !volUp) { tech -= 3; techDetail.add("-3 缩量触及年内新高(假突破风险)"); }
        if (positionPct >= 80) { tech *= 0.9; techDetail.add("×0.9 股价处高位(" + String.format("%.0f%%", positionPct) + "位置)，技术信号统一打折防追高"); }
        if (positionPct <= 20 && rsi14 < 35) { tech += 5; techDetail.add("+5 低位超跌(RSI<35，反弹弹性大)"); }
        tech = Math.max(-100, Math.min(100, tech));
        techDetail.add("=> 技术面总分 " + Math.round(tech));

        // ---------- 基本面 0-100（成长35+质量30+估值25+披露时效10，结合股价位置匹配；起步0分，数据缺失项不计分并标注）----------
        double fund = 0;
        fundDetail.add("基础分 0（各分项靠财务数据挣分，数据缺失项不计分）");
        long daysSinceReport = -1;
        StockFinance latestFin = finances.isEmpty() ? null : finances.get(0);
        if (latestFin != null && latestFin.getReportDate() != null) {
            daysSinceReport = (System.currentTimeMillis() - latestFin.getReportDate().getTime()) / 86400000L;
        }
        Double npYoy = latestFin == null ? null : toDouble(latestFin.getNetProfitYoy());
        Double revYoy = latestFin == null ? null : toDouble(latestFin.getRevenueYoy());
        Double roe = latestFin == null ? null : toDouble(latestFin.getRoe());
        Double gross = latestFin == null ? null : toDouble(latestFin.getGrossMargin());
        int growPeriods = 0, declinePeriods = 0;
        if (latestFin != null) {
            // 1) 成长性（35分）
            if (npYoy != null) {
                if (npYoy > 30) { fund += 18; fundDetail.add("+18 净利润同比" + String.format("%+.1f%%", npYoy) + "（高增长）"); }
                else if (npYoy > 10) { fund += 13; fundDetail.add("+13 净利润同比" + String.format("%+.1f%%", npYoy) + "（稳健增长）"); }
                else if (npYoy >= 0) { fund += 7; fundDetail.add("+7 净利润同比" + String.format("%+.1f%%", npYoy) + "（微增）"); }
                else if (npYoy > -15) { fund -= 8; fundDetail.add("-8 净利润同比" + String.format("%+.1f%%", npYoy) + "（小幅下滑）"); }
                else { fund -= 16; fundDetail.add("-16 净利润同比" + String.format("%+.1f%%", npYoy) + "（大幅下滑）"); }
            } else fundDetail.add("0 净利润同比缺失，未计分");
            for (StockFinance f : finances) {   // 最新在前，统计从最新期起的连续同向段
                Double y = toDouble(f.getNetProfitYoy());
                if (y == null || y == 0) break;
                if (y > 0) {
                    if (declinePeriods > 0) break;
                    growPeriods++;
                } else {
                    if (growPeriods > 0) break;
                    declinePeriods++;
                }
            }
            if (growPeriods >= 3) { fund += 8; fundDetail.add("+8 连续" + growPeriods + "期净利润正增长"); }
            else if (growPeriods >= 2) { fund += 5; fundDetail.add("+5 连续" + growPeriods + "期净利润正增长"); }
            if (declinePeriods >= 2) { fund -= 10; fundDetail.add("-10 连续" + declinePeriods + "期净利润下滑"); }
            if (revYoy != null) {
                if (revYoy > 20) { fund += 8; fundDetail.add("+8 营收同比" + String.format("%+.1f%%", revYoy) + "（放量增长）"); }
                else if (revYoy >= 0) { fund += 4; fundDetail.add("+4 营收同比" + String.format("%+.1f%%", revYoy)); }
                else { fund -= 7; fundDetail.add("-7 营收同比" + String.format("%+.1f%%", revYoy) + "（收缩）"); }
            } else fundDetail.add("0 营收同比缺失，未计分");
            // 利润含金量：扣非净利润/净利润
            Double np = toDouble(latestFin.getNetProfit());
            Double deductNp = toDouble(latestFin.getDeductNetProfit());
            if (np != null && np > 0 && deductNp != null) {
                if (deductNp < 0) { fund -= 6; fundDetail.add("-6 扣非净利润为负（主业实际亏损）"); }
                else if (deductNp / np >= 0.8) { fund += 4; fundDetail.add("+4 扣非/净利=" + String.format("%.0f%%", deductNp / np * 100) + "（利润含金量高）"); }
            }
            // 2) 盈利质量（30分）
            if (roe != null) {
                if (roe > 15) { fund += 10; fundDetail.add("+10 ROE" + String.format("%.2f%%", roe) + "（>15% 优秀）"); }
                else if (roe > 10) { fund += 7; fundDetail.add("+7 ROE" + String.format("%.2f%%", roe) + "（10%~15% 良好）"); }
                else if (roe > 5) { fund += 4; fundDetail.add("+4 ROE" + String.format("%.2f%%", roe) + "（5%~10% 一般）"); }
                else if (roe <= 0) { fund -= 10; fundDetail.add("-10 ROE" + String.format("%.2f%%", roe) + "（为负）"); }
            } else fundDetail.add("0 ROE缺失，未计分");
            StockFinance prevFin = finances.size() > 1 ? finances.get(1) : null;
            if (roe != null && prevFin != null && toDouble(prevFin.getRoe()) != null && roe > toDouble(prevFin.getRoe())) {
                fund += 3; fundDetail.add("+3 ROE较上期提升");
            }
            Double netMargin = toDouble(latestFin.getNetMargin());
            if (netMargin != null && netMargin > 15) { fund += 5; fundDetail.add("+5 净利率" + String.format("%.2f%%", netMargin) + "（>15%）"); }
            if (gross != null) {
                if (gross > 40) { fund += 5; fundDetail.add("+5 毛利率" + String.format("%.2f%%", gross) + "（>40% 高毛利）"); }
                else if (gross > 25) { fund += 3; fundDetail.add("+3 毛利率" + String.format("%.2f%%", gross)); }
                else if (gross < 15) { fund -= 2; fundDetail.add("-2 毛利率" + String.format("%.2f%%", gross) + "（<15%）"); }
            } else fundDetail.add("0 毛利率缺失（部分行业如银行无毛利概念），未计分");
            Double debt = toDouble(latestFin.getDebtRatio());
            if (debt != null) {
                if (debt > 70) { fund -= 6; fundDetail.add("-6 负债率" + String.format("%.2f%%", debt) + "（>70% 高杠杆）"); }
                else if (debt > 60) { fund -= 3; fundDetail.add("-3 负债率" + String.format("%.2f%%", debt)); }
                else if (debt < 40) { fund += 5; fundDetail.add("+5 负债率" + String.format("%.2f%%", debt) + "（<40% 低杠杆）"); }
            } else fundDetail.add("0 负债率缺失，未计分");
            // 现金流质量：每股经营现金流/每股收益=净现比（F10接口无经营现金流总额字段，用每股口径等价计算）
            Double ocfps = toDouble(latestFin.getCashflowPerShare());
            Double eps = toDouble(latestFin.getEps());
            if (ocfps != null && ocfps > 0 && eps != null && eps > 0) {
                if (ocfps / eps >= 0.5) { fund += 5; fundDetail.add("+5 净现比" + String.format("%.2f", ocfps / eps) + "（现金流覆盖净利，盈利含金量高）"); }
                else fundDetail.add("0 净现比" + String.format("%.2f", ocfps / eps) + "（<0.5 现金流偏弱），未计分");
            } else fundDetail.add("0 每股现金流或EPS缺失，未计分");
            // 3) 估值（25分，与股价位置匹配）
            BigDecimal pe = basic.getPeTtm();
            BigDecimal pb = basic.getPbRatio();
            if (pe != null) {
                double p = pe.doubleValue();
                if (p <= 0) { fund -= 8; fundDetail.add("-8 PE(TTM)为负（亏损状态）"); }
                else if (p < 15) { fund += 10; fundDetail.add("+10 PE(TTM)" + String.format("%.2f", p) + "（<15 低估）"); }
                else if (p < 25) { fund += 7; fundDetail.add("+7 PE(TTM)" + String.format("%.2f", p) + "（15~25 合理）"); }
                else if (p < 40) { fund += 3; fundDetail.add("+3 PE(TTM)" + String.format("%.2f", p) + "（25~40 偏高）"); }
                else if (p <= 80) { fund -= 3; fundDetail.add("-3 PE(TTM)" + String.format("%.2f", p) + "（40~80 高估）"); }
                else { fund -= 6; fundDetail.add("-6 PE(TTM)" + String.format("%.2f", p) + "（>80 严重高估）"); }
            } else fundDetail.add("0 PE(TTM)缺失，未计分");
            if (pb != null) {
                double p = pb.doubleValue();
                if (p > 0 && p < 1.5) { fund += 3; fundDetail.add("+3 PB" + String.format("%.2f", p) + "（<1.5 破净附近）"); }
                else if (p > 8) { fund -= 3; fundDetail.add("-3 PB" + String.format("%.2f", p) + "（>8 溢价过高）"); }
            }
            if (positionPct < 30 && npYoy != null && npYoy > 0) { fund += 6; fundDetail.add("+6 股价低位+业绩增长（估值与位置匹配，存在双击潜力）"); }
            if (positionPct > 75 && pe != null && pe.doubleValue() > 45) { fund -= 8; fundDetail.add("-8 股价高位+PE>45（双杀风险）"); }
            // 4) 披露时效（10分）：距最新报告期越久，基本面指引越弱
            if (daysSinceReport >= 0) {
                if (daysSinceReport <= 50) { fund += 10; fundDetail.add("+10 最新报告期距今" + daysSinceReport + "天（披露新鲜，指引性强）"); }
                else if (daysSinceReport > 110) { fund *= 0.9; fundDetail.add("×0.9 最新报告期距今" + daysSinceReport + "天（数据陈旧，全项打折）"); }
                else fundDetail.add("0 最新报告期距今" + daysSinceReport + "天（时效一般），未计分");
            }
        } else {
            fundDetail.add("无财务数据（补抓失败），基本面按0分处理");
        }
        fund = Math.max(-100, Math.min(100, fund));
        fundDetail.add("=> 基本面总分 " + Math.round(fund));

        // ---------- 资金筹码面 0-100（主力动向55+股东户数筹码40+位置修正，吸筹/出货共振判定）----------
        // 注意：资金强度一律用"净流入/成交额"无量纲比例，避免金额绝对值单位失衡导致分数爆表/归零
        double flowMain = 0;
        double sum3 = 0, sum5 = 0, sum10 = 0, sup5 = 0, flowRatio5 = 0;
        int streak = 0;
        boolean inDir = false;
        if (!flows.isEmpty()) {
            List<StockCapitalFlow> asc = new ArrayList<>(flows);
            Collections.reverse(asc); // 升序（旧→新）
            int n = asc.size();
            for (int i = 0; i < n; i++) {
                double m = toDouble(asc.get(i).getMainNetInflow()) == null ? 0 : toDouble(asc.get(i).getMainNetInflow()).doubleValue();
                if (i >= n - 3) sum3 += m;
                if (i >= n - 5) {
                    sum5 += m;
                    sup5 += toDouble(asc.get(i).getSuperLargeNet()) == null ? 0 : toDouble(asc.get(i).getSuperLargeNet()).doubleValue();
                }
                if (i >= n - 10) sum10 += m;
            }
            if (sum3 > 0) { flowMain += 10; flowDetail.add("+10 近3日主力净流入 " + String.format("%.0f万", sum3)); }
            else { flowMain -= 4; flowDetail.add("-4 近3日主力净流出 " + String.format("%.0f万", -sum3)); }
            if (sum5 > 0) { flowMain += 10; flowDetail.add("+10 近5日主力净流入 " + String.format("%.0f万", sum5)); }
            else { flowMain -= 8; flowDetail.add("-8 近5日主力净流出 " + String.format("%.0f万", -sum5)); }
            if (sum10 > 0) { flowMain += 6; flowDetail.add("+6 近10日主力净流入 " + String.format("%.0f万", sum10)); }
            // 主力强度：近5日主力净流入/近5日成交额（无量纲核心指标）
            double amount5 = 0;
            for (int i = Math.max(0, daily.size() - 5); i < daily.size(); i++) {
                if (daily.get(i).getTurnover() != null) amount5 += daily.get(i).getTurnover().doubleValue();
            }
            if (amount5 > 0) {
                flowRatio5 = sum5 * 10000 / amount5 * 100;   // sum5为万元，amount5为元
                String rStr = String.format("%.2f%%", flowRatio5);
                if (flowRatio5 > 5) { flowMain += 8; flowDetail.add("+8 近5日主力净流入占成交额" + rStr + "（强势吸筹级别）"); }
                else if (flowRatio5 > 2) { flowMain += 5; flowDetail.add("+5 近5日主力净流入占成交额" + rStr + "（明显流入）"); }
                else if (flowRatio5 > 0) { flowMain += 2; flowDetail.add("+2 近5日主力净流入占成交额" + rStr + "（温和流入）"); }
                else if (flowRatio5 > -2) { flowMain -= 2; flowDetail.add("-2 近5日主力净流入占成交额" + rStr + "（轻度流出）"); }
                else if (flowRatio5 > -5) { flowMain -= 5; flowDetail.add("-5 近5日主力净流入占成交额" + rStr + "（明显流出）"); }
                else { flowMain -= 10; flowDetail.add("-10 近5日主力净流入占成交额" + rStr + "（强力出货级别）"); }
            }
            if (sup5 > 0) { flowMain += 4; flowDetail.add("+4 近5日超大单净流入 " + String.format("%.0f万", sup5) + "（机构级别资金）"); }
            else if (sup5 < 0) { flowMain -= 4; flowDetail.add("-4 近5日超大单净流出 " + String.format("%.0f万", -sup5) + "（机构减持动作）"); }
            double lastMain = toDouble(asc.get(n - 1).getMainNetInflow()) == null ? 0 : toDouble(asc.get(n - 1).getMainNetInflow()).doubleValue();
            inDir = lastMain >= 0;
            for (int i = n - 1; i >= 0; i--) {
                double m = toDouble(asc.get(i).getMainNetInflow()) == null ? 0 : toDouble(asc.get(i).getMainNetInflow()).doubleValue();
                if (inDir ? m >= 0 : m < 0) streak++;
                else break;
            }
            if (inDir) {
                if (streak >= 5) { flowMain += 8; flowDetail.add("+8 主力资金连续" + streak + "日净流入（持续性吸筹）"); }
                else if (streak >= 3) { flowMain += 5; flowDetail.add("+5 主力资金连续" + streak + "日净流入"); }
            } else {
                if (streak >= 5) { flowMain -= 12; flowDetail.add("-12 主力资金连续" + streak + "日净流出（持续减仓）"); }
                else if (streak >= 3) { flowMain -= 8; flowDetail.add("-8 主力资金连续" + streak + "日净流出"); }
            }
        } else {
            flowDetail.add("资金流数据缺失（补抓失败），主力动向未计分");
        }
        // 股东户数筹码（40分）
        double chipScore = 0;
        StockHolderNum h0 = holders.isEmpty() ? null : holders.get(0);
        int downStreak = 0, upStreak = 0;
        for (StockHolderNum h : holders) {
            Double r = toDouble(h.getHolderNumRatio());
            if (r == null || r == 0) break;
            if (r < 0) {
                if (upStreak > 0) break;
                downStreak++;
            } else {
                if (downStreak > 0) break;
                upStreak++;
            }
        }
        if (h0 != null) {
            Double r0 = toDouble(h0.getHolderNumRatio());
            if (r0 != null) {
                if (r0 <= -5) { chipScore += 15; flowDetail.add("+15 最新股东户数环比" + String.format("%.2f%%", r0) + "（大幅集中，筹码向主力转移）"); }
                else if (r0 <= -2) { chipScore += 10; flowDetail.add("+10 最新股东户数环比" + String.format("%.2f%%", r0) + "（明显集中）"); }
                else if (r0 < 0) { chipScore += 6; flowDetail.add("+6 最新股东户数环比" + String.format("%.2f%%", r0) + "（小幅集中）"); }
                else if (r0 >= 10) { chipScore -= 14; flowDetail.add("-14 最新股东户数环比" + String.format("%+.2f%%", r0) + "（大幅分散，散户接盘迹象）"); }
                else if (r0 >= 5) { chipScore -= 8; flowDetail.add("-8 最新股东户数环比" + String.format("%+.2f%%", r0) + "（明显分散）"); }
            } else flowDetail.add("0 最新股东户数变化率缺失，未计分");
            if (downStreak >= 3) { chipScore += 8; flowDetail.add("+8 股东户数连续" + downStreak + "期下降（筹码持续集中）"); }
            else if (downStreak >= 2) { chipScore += 4; flowDetail.add("+4 股东户数连续" + downStreak + "期下降"); }
            if (upStreak >= 3) { chipScore -= 8; flowDetail.add("-8 股东户数连续" + upStreak + "期上升（筹码持续分散）"); }
            else if (upStreak >= 2) { chipScore -= 4; flowDetail.add("-4 股东户数连续" + upStreak + "期上升"); }
            // 共振判定：户数变化方向 × 主力资金方向
            if (downStreak >= 2 && sum5 > 0) { chipScore += 10; flowDetail.add("+10 吸筹共振（户数连续集中+主力资金净流入，双重印证）"); }
            if (upStreak >= 2 && sum5 < 0) { chipScore -= 10; flowDetail.add("-10 出货共振（户数连续分散+主力资金净流出，双重印证）"); }
        } else {
            flowDetail.add("股东户数数据缺失（补抓失败），筹码部分未计分");
        }
        // 资金筹码位置修正
        if (positionPct < 30 && sum5 > 0) { chipScore += 6; flowDetail.add("+6 低位吸筹（股价处" + String.format("%.0f%%", positionPct) + "位置，吸筹可信度高）"); }
        if (positionPct >= 80 && sum5 > 0 && todayChg > 5) { chipScore -= 5; flowDetail.add("-5 高位放量流入警惕对倒出货（股价处" + String.format("%.0f%%", positionPct) + "位置）"); }
        boolean noFlowChipData = flows.isEmpty() && holders.isEmpty();
        double flowScore = noFlowChipData ? 50 : Math.max(-100, Math.min(100, flowMain + chipScore));
        flowDetail.add("=> 资金筹码总分 " + Math.round(flowScore));

        // ---------- 消息面 0-100（时效25+公告20+关键词影响30+热度10，情绪方向由AI判断）----------
        double newsScore = 30;
        newsDetail.add("基础分 30（中性起点）");
        int goodKwCnt = 0, badKwCnt = 0;
        List<String> goodTitles = new ArrayList<>(), badTitles = new ArrayList<>();
        if (!newsList.isEmpty()) {
            Date latestNews = newsList.get(0).getPublishTime();
            long daysSinceNews = latestNews == null ? 999 :
                    (System.currentTimeMillis() - latestNews.getTime()) / 86400000L;
            if (daysSinceNews <= 3) { newsScore += 25; newsDetail.add("+25 最新消息距今" + daysSinceNews + "天（时效性高）"); }
            else if (daysSinceNews <= 7) { newsScore += 12; newsDetail.add("+12 最新消息距今" + daysSinceNews + "天（时效一般）"); }
            else newsDetail.add("0 最新消息距今" + daysSinceNews + "天（时效性弱），未计分");
            long annIn7d = newsList.stream().filter(n -> n.getNewsType() != null && n.getNewsType() == 2)
                    .filter(n -> n.getPublishTime() != null)
                    .filter(n -> (System.currentTimeMillis() - n.getPublishTime().getTime()) / 86400000L <= 7).count();
            if (annIn7d >= 2) { newsScore += 20; newsDetail.add("+20 近7天公告" + annIn7d + "条（公告密度高）"); }
            else if (annIn7d >= 1) { newsScore += 10; newsDetail.add("+10 近7天公告1条"); }
            else newsDetail.add("0 近7天无公告，未计分");
            // 近14天标题关键词影响（确定性规则，实质影响交由AI结合位置判断）
            String[] goodKw = {"增持", "回购", "中标", "预增", "扭亏", "分红", "派息", "业绩增长", "签订", "突破", "净利增"};
            String[] badKw = {"减持", "质押", "立案", "调查", "违规", "诉讼", "预亏", "下滑", "减值", "问询", "退市", "冻结", "仲裁"};
            int hot3 = 0;
            for (StockNews nw : newsList) {
                if (nw.getPublishTime() == null) continue;
                long d = (System.currentTimeMillis() - nw.getPublishTime().getTime()) / 86400000L;
                if (d > 14) break;   // 列表按时间倒序
                if (d <= 3) hot3++;
                String t = nw.getTitle() == null ? "" : nw.getTitle();
                boolean counted = false;
                for (String k : goodKw) {
                    if (t.contains(k)) {
                        goodKwCnt++;
                        counted = true;
                        if (goodTitles.size() < 3) goodTitles.add("《" + t + "》");
                        break;
                    }
                }
                if (!counted) {
                    for (String k : badKw) {
                        if (t.contains(k)) {
                            badKwCnt++;
                            if (badTitles.size() < 3) badTitles.add("《" + t + "》");
                            break;
                        }
                    }
                }
            }
            int goodPts = Math.min(18, goodKwCnt * 6), badPts = Math.min(24, badKwCnt * 8);
            newsScore += goodPts - badPts;
            if (goodKwCnt > 0) newsDetail.add("+" + goodPts + " 近14天利好关键词" + goodKwCnt + "条（如" + String.join("、", goodTitles) + "）");
            if (badKwCnt > 0) newsDetail.add("-" + badPts + " 近14天利空关键词" + badKwCnt + "条（如" + String.join("、", badTitles) + "）");
            if (goodKwCnt == 0 && badKwCnt == 0) newsDetail.add("0 近14天无利好/利空关键词命中，未计分");
            if (hot3 >= 5) { newsScore += 10; newsDetail.add("+10 近3天消息" + hot3 + "条（关注度升温，方向由AI结合内容判断）"); }
        } else {
            newsDetail.add("消息数据缺失（补抓失败），仅保留基础分30");
        }
        newsScore = Math.max(-100, Math.min(100, newsScore));
        newsDetail.add("=> 消息面总分 " + Math.round(newsScore));

        // ---------- 大盘与板块环境（实时抓取，对综合分做环境修正）----------
        double shPct = marketInfo.get("shChangePct") == null ? 0 : toDouble(new BigDecimal(marketInfo.get("shChangePct").toString()));
        Integer sRank = sectorInfo.getInteger("rank");
        Integer sTotal = sectorInfo.getInteger("total");
        boolean sectorStrong = sRank != null && sTotal != null && sTotal > 0 && sRank <= sTotal / 3;
        boolean sectorWeak = sRank != null && sTotal != null && sTotal > 0 && sRank > sTotal / 2;
        double envAdj = 0;
        if (shPct >= 1) envAdj += sectorStrong ? 3 : 1.5;
        if (shPct <= -1.5) envAdj -= sectorWeak ? 3 : 1.5;

        // 四维分允许负值，按权重如实计入综合分（负分拉低综合分，体现偏空程度），综合分区间[-100,100]
        double composite = Math.max(-100, Math.min(100,
                Math.round(tech * 0.35 + fund * 0.30 + flowScore * 0.20 + newsScore * 0.15 + envAdj)));
        String valueLevel;
        if (composite >= 80) valueLevel = "A（投资价值较高）";
        else if (composite >= 70) valueLevel = "B（有一定投资价值）";
        else if (composite >= 60) valueLevel = "C（中性观察）";
        else if (composite >= 50) valueLevel = "D（偏弱谨慎）";
        else if (composite >= 0) valueLevel = "E（风险较大，宜回避）";
        else valueLevel = "F（强烈看空，坚决回避）";

        // ===== 状态判定词（确定性结论，供AI引用，保证分析有理有据）=====
        JSONObject st = new JSONObject();
        st.put("posBand", posBand);
        st.put("chg20", chg20);
        String aboveMa = (lastClose > ma5 ? "MA5 " : "") + (lastClose > ma10 ? "MA10 " : "")
                + (lastClose > ma20 ? "MA20 " : "") + (lastClose > ma60 ? "MA60" : "");
        st.put("aboveMa", aboveMa.trim().isEmpty() ? "现价位于全部主要均线下方（全面承压）" : "现价站上 " + aboveMa.trim());
        st.put("maLayout", bullAll ? "多头排列（MA5>MA10>MA20>MA60，短中期趋势向上）"
                : bearAll ? "空头排列（MA5<MA10<MA20<MA60，短中期趋势向下）"
                : bullShort ? "短中期多头（MA5>MA10>MA20，长期均线MA60未确认）" : "均线纠缠，方向未明");
        st.put("ma20Slope", ma20Rising ? "MA20上行（中期趋势向好）" : ma20Falling ? "MA20下行（中期趋势偏弱）" : "MA20走平（趋势盘整）");
        st.put("macdState", (macd[0] > macd[1] ? "DIF在DEA上方（金叉运行中）" : "DIF在DEA下方（死叉运行中）")
                + (macd[0] > 0 ? "，零轴上方（多头市场）" : "，零轴下方（空头市场）")
                + (macd[2] > 0 ? (macd[2] > macdPrev[2] ? "，红柱放大（上涨动能增强）" : "，红柱缩小（上涨动能衰减）")
                : (macd[2] < macdPrev[2] ? "，绿柱放大（下跌动能增强）" : "，绿柱缩小（下跌动能衰竭）")));
        st.put("kdjState", String.format("K=%.1f，D=%.1f，J=%.1f", kdj[0], kdj[1], kdj[2])
                + (kdjGolden ? "，刚形成金叉" : kdj[0] > kdj[1] ? "，K在D上方" : "，K在D下方")
                + (kdj[0] > 85 || kdj[2] > 100 ? "，进入超买区" : (kdj[0] < 20 || kdj[2] < 0) ? "，进入超卖区" : ""));
        st.put("rsiState", String.format("RSI14=%.1f（%s）", rsi14,
                rsi14 > 75 ? "超买" : rsi14 >= 45 ? "强势区" : rsi14 >= 30 ? "中性偏弱" : "超卖"));
        st.put("volPrice", (todayChg > 0 && volUp ? "放量上涨" : todayChg > 0 && volDown ? "缩量上涨" : todayChg < 0 && volUp ? "放量下跌"
                : todayChg < 0 && volDown ? "缩量回调" : "量能平稳")
                + String.format("（量为昨日的%.2f倍，当日换手率%.2f%%，当日涨跌%.2f%%）", volRatio, turnoverRateToday, todayChg));
        st.put("financeFresh", latestFin == null || latestFin.getReportDate() == null ? "无财务数据"
                : String.format("最近报告期 %s，距今 %d 天%s", daySdf.format(latestFin.getReportDate()), daysSinceReport,
                daysSinceReport <= 50 ? "（披露新鲜，基本面指引强）" : daysSinceReport > 110 ? "（数据已陈旧，注意下期披露窗口，基本面指引减弱）" : "（时效正常）"));
        st.put("profitTrend", latestFin == null ? "无财务数据"
                : String.format("最新净利润同比 %s%%、营收同比 %s%%；自最新期起连续 %d 期正增长 / %d 期下滑",
                npYoy == null ? "-" : npYoy, revYoy == null ? "-" : revYoy, growPeriods, declinePeriods));
        st.put("reactions", buildReactionDesc(daily, finances, daySdf));
        st.put("flowStreak", flows.isEmpty() ? "无资金流数据"
                : String.format("主力资金%s%d日；近3日合计 %.0f 万，近5日 %.0f 万，近10日 %.0f 万；近5日超大单净 %s %.0f 万",
                inDir ? "连续净流入 " : "连续净流出 ", streak, sum3, sum5, sum10,
                sup5 >= 0 ? "流入" : "流出", Math.abs(sup5)));
        st.put("chipState", h0 == null ? "无股东户数数据"
                : String.format("最新期（%s）股东户数 %s 户，环比 %s%%；近3期趋势：%s",
                h0.getEndDate() == null ? "-" : daySdf.format(h0.getEndDate()),
                h0.getHolderNum() == null ? "-" : h0.getHolderNum(),
                h0.getHolderNumRatio() == null ? "-" : h0.getHolderNumRatio(),
                downStreak >= 2 ? "连续下降（筹码趋向集中）" : upStreak >= 2 ? "连续上升（筹码趋向分散）" : "交替波动"));
        st.put("resonance", h0 == null || flows.isEmpty() ? ""
                : downStreak >= 2 && sum5 > 0 ? String.format("【吸筹共振】筹码集中与主力净流入同时出现（%s区域），主力吸筹特征明显", posBand)
                : upStreak >= 2 && sum5 < 0 ? String.format("【出货共振】筹码分散与主力净流出同时出现（%s区域），散户接盘/主力派发特征明显", posBand) : "");
        st.put("newsFlags", String.format("近14天消息：利好关键词 %d 条，利空关键词 %d 条%s",
                goodKwCnt, badKwCnt, goodKwCnt + badKwCnt == 0 ? "（无显著利好利空关键词）" : "（实质影响请AI结合具体标题判断）"));
        Object shPoint = marketInfo.get("shPoint");
        st.put("marketDesc", String.format("上证指数%s（%s%%），深成指 %s%%，创业板指 %s%%；沪市上涨 %s 家 / 下跌 %s 家",
                shPoint == null ? "-" : shPoint.toString(),
                marketInfo.get("shChangePct") == null ? "-" : marketInfo.get("shChangePct").toString(),
                marketInfo.get("szChangePct") == null ? "-" : marketInfo.get("szChangePct").toString(),
                marketInfo.get("cybChangePct") == null ? "-" : marketInfo.get("cybChangePct").toString(),
                marketInfo.get("upCount") == null ? "-" : marketInfo.get("upCount").toString(),
                marketInfo.get("downCount") == null ? "-" : marketInfo.get("downCount").toString()));
        double secPct = sectorInfo.get("changePct") == null ? 0 : toDouble(new BigDecimal(sectorInfo.get("changePct").toString()));
        st.put("sectorRel", String.format("今日个股 %.2f%% vs 行业板块 %.2f%%（%s）", todayChg, secPct,
                todayChg >= secPct ? "跑赢板块" : "跑输板块"));

        Map<String, Object> score = new HashMap<>();
        score.put("composite", composite);
        score.put("tech", Math.round(tech));
        score.put("fund", Math.round(fund));
        score.put("flow", Math.round(flowScore));
        score.put("news", Math.round(newsScore));
        score.put("valueLevel", valueLevel);
        score.put("envDesc", String.format("大盘环境修正 %+d 分（上证 %.1f%%，板块%s）",
                Math.round(envAdj), shPct, sectorStrong ? "强势" : sectorWeak ? "偏弱" : "中性"));
        score.put("positionDesc", String.format("现价 %.2f 处于近一年区间 %.0f%% 位置（%s，区间 %.2f~%.2f），距年内高点回撤 %.1f%%",
                lastClose, positionPct, posBand, yearLow, yearHigh, drawdownFromHigh));
        // 评分明细（每维度得分如何得出的逐条说明，前端可直接展示，AI需在对应章节解读）
        score.put("techDetail", techDetail);
        score.put("fundDetail", fundDetail);
        score.put("flowDetail", flowDetail);
        score.put("newsDetail", newsDetail);

        // ===== AI 综合分析 =====
        String aiAnalysis = callAiAnalysis(basic, daily, ma5, ma10, ma20, ma60, macd, kdj, rsi14,
                positionPct, yearLow, yearHigh, drawdownFromHigh, sectorInfo, flows, finances, newsList, holders, score,
                marketInfo, st);

        Map<String, Object> result = new HashMap<>();
        result.put("ruleScore", score);
        result.put("aiAnalysis", aiAnalysis);
        result.put("sectorInfo", sectorInfo);
        result.put("marketInfo", marketInfo);

        // 写入缓存：指纹未变期间复用该结果，数据任一变化后指纹不同自然重新分析
        aiAnalysisCache.put(aiFingerprint, new AiAnalysisCacheEntry(result, System.currentTimeMillis() + AI_ANALYSIS_CACHE_TTL_MS));
        return result;
    }

    /**
     * 构建 AI 分析缓存指纹：覆盖分析所用的全部输入数据（行情估值/K线/财务/资金/消息/筹码/板块/大盘实时），
     * 任一数据变化则指纹变化，确保缓存复用结果与实时重新分析完全一致
     */
    private String buildAiAnalysisFingerprint(String stockCode, StockBasic basic, List<StockKline> daily,
                                              List<StockFinance> finances, List<StockCapitalFlow> flows,
                                              List<StockNews> newsList, List<StockHolderNum> holders,
                                              JSONObject sectorInfo, JSONObject marketInfo) {
        SimpleDateFormat daySdf = new SimpleDateFormat("yyyy-MM-dd");
        StringBuilder fp = new StringBuilder(stockCode);
        // 行情估值（随刷新变化）
        fp.append("|p=").append(basic.getLastPrice()).append(",c=").append(basic.getChangePct())
                .append(",t=").append(basic.getTurnoverRate()).append(",vr=").append(basic.getVolumeRatio())
                .append(",cap=").append(basic.getTotalMarketCap()).append(",pe=").append(basic.getPeTtm())
                .append(",pb=").append(basic.getPbRatio());
        // K线：条数+首尾日期+最新收盘+收盘合计（中间任一行变动都会改变合计）
        double closeSum = 0;
        for (StockKline k : daily) closeSum += k.getClosePrice() == null ? 0 : k.getClosePrice().doubleValue();
        StockKline kFirst = daily.get(0), kLast = daily.get(daily.size() - 1);
        fp.append("|k=").append(daily.size())
                .append(",f=").append(kFirst.getTradeDate() == null ? "-" : daySdf.format(kFirst.getTradeDate()))
                .append(",l=").append(kLast.getTradeDate() == null ? "-" : daySdf.format(kLast.getTradeDate()))
                .append(",lc=").append(kLast.getClosePrice()).append(",s=").append(Math.round(closeSum * 100));
        // 财务：期数+最新报告期+营收/净利
        StockFinance f0 = finances.isEmpty() ? null : finances.get(0);
        fp.append("|fin=").append(finances.size()).append(",")
                .append(f0 == null || f0.getReportDate() == null ? "-" : daySdf.format(f0.getReportDate())).append(",")
                .append(f0 == null ? "-" : f0.getRevenue()).append(",").append(f0 == null ? "-" : f0.getNetProfit());
        // 资金流：条数+最新交易日+主力净流入
        StockCapitalFlow fl0 = flows.isEmpty() ? null : flows.get(0);
        fp.append("|flow=").append(flows.size()).append(",")
                .append(fl0 == null || fl0.getTradeDate() == null ? "-" : daySdf.format(fl0.getTradeDate())).append(",")
                .append(fl0 == null ? "-" : fl0.getMainNetInflow());
        // 消息：条数+最新发布时间+标题集合（标题参与关键词扫描与AI解读，须纳入指纹）
        StringBuilder titles = new StringBuilder();
        for (StockNews n : newsList) titles.append(n.getTitle() == null ? "" : n.getTitle()).append(",");
        StockNews n0 = newsList.isEmpty() ? null : newsList.get(0);
        fp.append("|news=").append(newsList.size()).append(",")
                .append(n0 == null || n0.getPublishTime() == null ? "-" : n0.getPublishTime().getTime()).append(",")
                .append(titles.hashCode());
        // 筹码：条数+最新期股东户数
        StockHolderNum h0 = holders.isEmpty() ? null : holders.get(0);
        fp.append("|hold=").append(holders.size()).append(",")
                .append(h0 == null || h0.getEndDate() == null ? "-" : daySdf.format(h0.getEndDate())).append(",")
                .append(h0 == null ? "-" : h0.getHolderNum());
        // 板块与大盘实时（交易时段会变化，收盘后恒定，缓存命中主要发生在收盘后/数据未变时）
        fp.append("|sec=").append(sectorInfo == null ? "-" : sectorInfo.getString("name")).append(",")
                .append(sectorInfo == null ? "-" : sectorInfo.get("changePct")).append(",")
                .append(sectorInfo == null ? "-" : sectorInfo.get("rank"));
        fp.append("|mkt=").append(marketInfo.get("shPoint")).append(",").append(marketInfo.get("shChangePct"))
                .append(",").append(marketInfo.get("upCount")).append(",").append(marketInfo.get("downCount"));
        return fp.toString();
    }

    /** 缓存容量控制：超限时先清理已过期条目，仍超限则移除最早过期的一条 */
    private void evictAiAnalysisCache() {
        if (aiAnalysisCache.size() < AI_ANALYSIS_CACHE_MAX) return;
        long now = System.currentTimeMillis();
        aiAnalysisCache.values().removeIf(e -> now >= e.expireAt);
        if (aiAnalysisCache.size() >= AI_ANALYSIS_CACHE_MAX) {
            aiAnalysisCache.entrySet().stream()
                    .min(Comparator.comparingLong(e -> e.getValue().expireAt))
                    .ifPresent(e -> aiAnalysisCache.remove(e.getKey()));
        }
    }

    private Double toDouble(BigDecimal v) {
        return v == null ? null : v.doubleValue();
    }

    private double avgLast(List<Double> list, int n) {
        int from = Math.max(0, list.size() - n);
        return list.subList(from, list.size()).stream().mapToDouble(Double::doubleValue).average().orElse(0);
    }

    private double[] calcMacd(List<Double> closes) {
        double k12 = 2.0 / 13, k26 = 2.0 / 27, k9 = 2.0 / 10;
        double ema12 = closes.get(0), ema26 = closes.get(0), dea = 0;
        double dif = 0, hist = 0;
        for (double c : closes) {
            ema12 = ema12 * (1 - k12) + c * k12;
            ema26 = ema26 * (1 - k26) + c * k26;
            dif = ema12 - ema26;
            dea = dea * (1 - k9) + dif * k9;
            hist = (dif - dea) * 2;
        }
        return new double[]{dif, dea, hist};
    }

    private double[] calcKdj(List<Double> highs, List<Double> lows, List<Double> closes, int n) {
        double k = 50, d = 50, j = 50;
        for (int i = 0; i < closes.size(); i++) {
            int from = Math.max(0, i - n + 1);
            double hn = highs.subList(from, i + 1).stream().mapToDouble(Double::doubleValue).max().orElse(0);
            double ln = lows.subList(from, i + 1).stream().mapToDouble(Double::doubleValue).min().orElse(0);
            double rsv = hn > ln ? (closes.get(i) - ln) / (hn - ln) * 100 : 50;
            k = 2.0 / 3 * k + 1.0 / 3 * rsv;
            d = 2.0 / 3 * d + 1.0 / 3 * k;
            j = 3 * k - 2 * d;
        }
        return new double[]{k, d, j};
    }

    private double calcRsi(List<Double> closes, int n) {
        double gain = 0, loss = 0;
        int from = Math.max(1, closes.size() - n);
        for (int i = from; i < closes.size(); i++) {
            double chg = closes.get(i) - closes.get(i - 1);
            if (chg > 0) gain += chg; else loss -= chg;
        }
        if (gain + loss == 0) return 50;
        return gain / (gain + loss) * 100;
    }

    /**
     * 获取大盘实时行情与市场情绪（上证/深成指/创业板指涨跌幅 + 沪深两市涨跌家数，东财实时接口）
     */
    private JSONObject fetchMarketInfo() {
        JSONObject info = new JSONObject();
        info.put("shPoint", null);
        info.put("shChangePct", null);
        info.put("szChangePct", null);
        info.put("cybChangePct", null);
        info.put("upCount", null);
        info.put("downCount", null);
        // 1.000001=上证指数(沪市家数)，0.399001=深证成指，0.399006=创业板指，0.399106=深证综指(深市家数)
        String url = "http://push2.eastmoney.com/api/qt/ulist.np/get?fltt=2&secids=1.000001,0.399001,0.399006,0.399106&fields=f2,f3,f12,f14,f104,f105";
        String body = httpGet(url);
        if (body == null) return info;
        try {
            JSONObject json = JSON.parseObject(body);
            JSONObject data = json.getJSONObject("data");
            JSONArray diff = data == null ? null : data.getJSONArray("diff");
            if (diff == null || diff.isEmpty()) return info;
            long up = 0, down = 0;
            boolean hasCount = false;
            for (int i = 0; i < diff.size(); i++) {
                JSONObject row = diff.getJSONObject(i);
                String code = row.getString("f12");
                if ("000001".equals(code)) {
                    info.put("shPoint", row.get("f2"));
                    info.put("shChangePct", row.get("f3"));
                } else if ("399001".equals(code)) {
                    info.put("szChangePct", row.get("f3"));
                } else if ("399006".equals(code)) {
                    info.put("cybChangePct", row.get("f3"));
                }
                // 沪市(000001)与深市(399106)的涨跌家数合并=全市场情绪
                if ("000001".equals(code) || "399106".equals(code)) {
                    try {
                        up += row.getLongValue("f104");
                        down += row.getLongValue("f105");
                        hasCount = true;
                    } catch (Exception ignored) {
                    }
                }
            }
            if (hasCount) {
                info.put("upCount", up);
                info.put("downCount", down);
            }
        } catch (Exception e) {
            logger.error("获取大盘行情失败", e);
        }
        return info;
    }

    /**
     * 历史基本面披露后的股价反应对照（近似披露日=报告期后45天，取披露后第1与第5个交易日收盘对比，
     * 用于验证市场对历次业绩的认可度；受300日K线覆盖范围限制，最多对照最近4期）
     */
    // 将评分明细逐行写入prompt，让AI能引用并解释每个维度的得分构成
    private void appendScoreDetail(StringBuilder sb, String title, Object detailObj) {
        if (detailObj instanceof List) {
            sb.append("· ").append(title).append("：\n");
            for (Object o : (List<?>) detailObj) {
                sb.append("  ").append(o.toString()).append("\n");
            }
        }
    }

    private String buildReactionDesc(List<StockKline> dailyAsc, List<StockFinance> financesDesc, SimpleDateFormat sdf) {
        if (dailyAsc == null || dailyAsc.isEmpty() || financesDesc == null || financesDesc.isEmpty()) {
            return "（K线或财务数据不足，无法对照）\n";
        }
        StringBuilder sb = new StringBuilder();
        int matched = 0;
        long window45 = 45L * 86400000L;
        for (StockFinance f : financesDesc) {
            if (matched >= 4) break;
            if (f.getReportDate() == null) continue;
            Date discDate = new Date(f.getReportDate().getTime() + window45);
            int idx = -1;
            for (int i = 0; i < dailyAsc.size(); i++) {
                if (dailyAsc.get(i).getTradeDate() != null && !dailyAsc.get(i).getTradeDate().before(discDate)) {
                    idx = i;
                    break;
                }
            }
            if (idx < 0 || idx + 4 >= dailyAsc.size()) continue;   // 超出K线覆盖范围
            double c1 = dailyAsc.get(idx).getClosePrice().doubleValue();
            double c5 = dailyAsc.get(idx + 4).getClosePrice().doubleValue();
            if (c1 <= 0) continue;
            double chg = (c5 - c1) / c1 * 100;
            Double yoy = toDouble(f.getNetProfitYoy());
            String verdict;
            if (yoy == null) verdict = "业绩趋势不明，反应中性";
            else if (yoy > 0 && chg > 0) verdict = "业绩增+披露后上涨=市场认可";
            else if (yoy > 0 && chg < -5) verdict = "业绩增+披露后大跌=利好兑现或不及预期";
            else if (yoy < 0 && chg < 0) verdict = "业绩降+披露后下跌=利空发酵";
            else if (yoy < 0 && chg > 0) verdict = "业绩降+披露后上涨=利空出尽或预期反转";
            else verdict = "中性反应";
            sb.append("报告期 ").append(sdf.format(f.getReportDate()))
                    .append("：净利润同比 ").append(f.getNetProfitYoy() == null ? "-" : f.getNetProfitYoy()).append("%")
                    .append("，披露后5个交易日股价 ").append(String.format("%+.1f%%", chg))
                    .append("（").append(verdict).append("）\n");
            matched++;
        }
        return sb.length() == 0 ? "（历史披露窗口超出K线覆盖范围，无法对照）\n" : sb.toString();
    }

    /**
     * 获取所属行业板块当日表现与排名（东财行业板块行情）
     */
    private JSONObject fetchSectorInfo(String industry) {
        JSONObject info = new JSONObject();
        info.put("name", industry);
        info.put("changePct", null);
        info.put("rank", null);
        info.put("total", null);
        if (industry == null || industry.isEmpty()) return info;
        String url = "http://push2.eastmoney.com/api/qt/clist/get?pn=1&pz=500&po=1&np=1&fltt=2&invt=2&fid=f3&fs=m:90+t:2&fields=f3,f14";
        String body = httpGet(url);
        if (body == null) return info;
        try {
            JSONObject json = JSON.parseObject(body);
            JSONObject data = json.getJSONObject("data");
            if (data == null) return info;
            JSONArray diff = data.getJSONArray("diff");
            if (diff == null || diff.isEmpty()) return info;
            info.put("total", diff.size());
            // 先精确匹配，再去除Ⅱ/Ⅰ后缀前缀匹配
            String target = industry;
            for (int round = 0; round < 2; round++) {
                for (int i = 0; i < diff.size(); i++) {
                    JSONObject s = diff.getJSONObject(i);
                    String name = s.getString("f14");
                    if (name == null) continue;
                    boolean match = round == 0 ? name.equals(target)
                            : (target.replace("Ⅱ", "").replace("Ⅰ", "").length() >= 2
                            && (name.startsWith(target.replace("Ⅱ", "").replace("Ⅰ", ""))
                            || target.startsWith(name.replace("Ⅱ", "").replace("Ⅰ", ""))));
                    if (match) {
                        info.put("name", name);
                        info.put("changePct", s.get("f3"));
                        info.put("rank", i + 1);
                        return info;
                    }
                }
            }
        } catch (Exception e) {
            logger.error("获取板块行情失败: {}", industry, e);
        }
        return info;
    }

    private String callAiAnalysis(StockBasic basic, List<StockKline> daily,
                                  double ma5, double ma10, double ma20, double ma60,
                                  double[] macd, double[] kdj, double rsi14,
                                  double positionPct, double yearLow, double yearHigh, double drawdown,
                                  JSONObject sectorInfo, List<StockCapitalFlow> flows,
                                  List<StockFinance> finances, List<StockNews> newsList,
                                  List<StockHolderNum> holders, Map<String, Object> score,
                                  JSONObject marketInfo, JSONObject states) {
        SimpleDateFormat daySdf = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat fullSdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        double lastPrice = basic.getLastPrice() == null ? 0 : basic.getLastPrice().doubleValue();
        StringBuilder sb = new StringBuilder();
        sb.append("请基于以下数据对该股票进行专业综合分析。所有判断必须结合股价位置进行修正。\n\n");

        // 1) 股价位置（核心约束，置于最前）
        sb.append("【股价位置——核心约束（以下所有章节的分析与评分必须先引用此位置再下结论）】\n");
        sb.append("现价 ").append(lastPrice)
                .append("，处于近一年区间 ").append(String.format("%.0f%%", positionPct))
                .append(" 位置（").append(states.getString("posBand")).append("区域，区间 ")
                .append(String.format("%.2f~%.2f", yearLow, yearHigh))
                .append("），距年内高点回撤 ").append(String.format("%.1f%%", drawdown))
                .append("，现价较年内低点已上涨 ").append(String.format("%.1f%%", yearLow > 0 ? (lastPrice - yearLow) / yearLow * 100 : 0))
                .append("，近20日涨跌 ").append(String.format("%+.1f%%", states.getDoubleValue("chg20"))).append("\n");
        sb.append("位置解读规则：低位（<30%）利空钝化、利好弹性大，吸筹信号可信度高；中位（30%~70%）信号正常解读；")
                .append("高位（>80%）利好易兑现、技术信号打折，需防冲高回落与估值双杀。\n\n");

        // 2) 公司概况与最新行情估值
        sb.append("【公司概况与最新行情估值】\n");
        sb.append("代码/名称：").append(basic.getStockCode()).append(" ").append(basic.getStockName())
                .append("，所属行业：").append(basic.getIndustry()).append("，板块：").append(basic.getSector())
                .append("，概念：").append(basic.getConceptSectors()).append("\n");
        sb.append("最新价：").append(basic.getLastPrice()).append("，当日涨跌幅：").append(basic.getChangePct()).append("%")
                .append("，换手率：").append(basic.getTurnoverRate()).append("%，量比：").append(basic.getVolumeRatio()).append("\n");
        sb.append("总市值：").append(basic.getTotalMarketCap()).append("亿，市盈率TTM：").append(basic.getPeTtm())
                .append("，市净率：").append(basic.getPbRatio()).append("\n");

        // 3) 技术指标与系统判定
        sb.append("\n【技术指标与系统判定（判定词为确定性结论，请直接引用并解释其含义）】\n");
        sb.append("均线：MA5=").append(String.format("%.2f", ma5)).append("，MA10=").append(String.format("%.2f", ma10))
                .append("，MA20=").append(String.format("%.2f", ma20)).append("，MA60=").append(String.format("%.2f", ma60)).append("\n");
        sb.append("均线系统判定：").append(states.getString("maLayout"))
                .append("；").append(states.getString("aboveMa"))
                .append("；").append(states.getString("ma20Slope")).append("\n");
        sb.append("MACD：").append(String.format("DIF=%.3f，DEA=%.3f，HIST=%.3f；判定：", macd[0], macd[1], macd[2]))
                .append(states.getString("macdState")).append("\n");
        sb.append("KDJ：").append(states.getString("kdjState")).append("\n");
        sb.append("RSI：").append(states.getString("rsiState")).append("\n");
        sb.append("量价配合：").append(states.getString("volPrice")).append("\n");

        // 4) 板块与大盘实时环境
        sb.append("\n【板块与大盘实时环境（分析时刻实时抓取）】\n");
        sb.append("大盘：").append(states.getString("marketDesc")).append("\n");
        if (sectorInfo != null) {
            sb.append("行业板块：「").append(sectorInfo.getString("name")).append("」今日涨跌幅 ")
                    .append(sectorInfo.get("changePct")).append("%");
            if (sectorInfo.get("rank") != null) {
                sb.append("，在 ").append(sectorInfo.get("total")).append(" 个行业板块中排名第 ").append(sectorInfo.get("rank")).append(" 名");
            }
            sb.append("\n个股相对强弱：").append(states.getString("sectorRel")).append("\n");
        }

        // 5) 基本面（含披露时效与历史披露后股价反应）
        if (!finances.isEmpty()) {
            sb.append("\n【基本面-财务指标（").append(finances.size()).append("期，最新在前）】\n");
            for (StockFinance f : finances) {
                sb.append(f.getReportDate() == null ? "" : daySdf.format(f.getReportDate()))
                        .append("：营收").append(f.getRevenue()).append("亿(同比").append(f.getRevenueYoy()).append("%)")
                        .append("，净利润").append(f.getNetProfit()).append("亿(同比").append(f.getNetProfitYoy()).append("%)")
                        .append("，扣非").append(f.getDeductNetProfit()).append("亿")
                        .append("，ROE ").append(f.getRoe()).append("%，毛利率").append(f.getGrossMargin())
                        .append("%，净利率").append(f.getNetMargin()).append("%，负债率").append(f.getDebtRatio())
                        .append("%，经营现金流").append(f.getOperatingCashFlow()).append("亿\n");
            }
            sb.append("披露时效：").append(states.getString("financeFresh")).append("\n");
            sb.append("业绩趋势：").append(states.getString("profitTrend")).append("\n");
            sb.append("历史披露后股价反应（近似披露日=报告期后45天，验证市场对业绩的认可度）：\n").append(states.getString("reactions"));
        }

        // 6) 资金面
        if (!flows.isEmpty()) {
            sb.append("\n【资金面-主力资金近 ").append(flows.size()).append(" 日流向（万元，最新在前）】\n");
            for (StockCapitalFlow fl : flows) {
                sb.append(fl.getTradeDate() == null ? "" : daySdf.format(fl.getTradeDate()))
                        .append("：主力").append(fl.getMainNetInflow()).append("万")
                        .append("，超大单").append(fl.getSuperLargeNet()).append("万\n");
            }
            sb.append("系统判定：").append(states.getString("flowStreak")).append("\n");
        }

        // 7) 筹码结构（股东户数）
        if (holders != null && !holders.isEmpty()) {
            sb.append("\n【筹码结构-股东户数（户数下降=筹码集中=主力吸筹方向；上升=筹码分散=散户接盘方向）】\n");
            for (StockHolderNum h : holders) {
                sb.append(h.getEndDate() == null ? "" : daySdf.format(h.getEndDate()))
                        .append("：股东户数 ").append(h.getHolderNum())
                        .append("，较上期变化率 ").append(h.getHolderNumRatio() == null ? "-" : h.getHolderNumRatio()).append("%")
                        .append("，户均持股市值 ").append(h.getAvgMarketCap() == null ? "-" : h.getAvgMarketCap()).append("元\n");
            }
            sb.append("系统判定：").append(states.getString("chipState")).append("\n");
            if (states.getString("resonance") != null && !states.getString("resonance").isEmpty()) {
                sb.append("资金-筹码共振：").append(states.getString("resonance")).append("\n");
            }
        }

        // 8) 消息面
        if (!newsList.isEmpty()) {
            sb.append("\n【消息面-最新消息（注意结合时间判断时效性，当前时间：").append(fullSdf.format(new Date())).append("）】\n");
            for (StockNews n : newsList) {
                sb.append(n.getPublishTime() == null ? "" : fullSdf.format(n.getPublishTime()))
                        .append(" [").append(n.getNewsType() != null && n.getNewsType() == 2 ? "公告" : "新闻").append("]")
                        .append(n.getTitle()).append("\n");
            }
            sb.append("关键词扫描：").append(states.getString("newsFlags")).append("\n");
        }

        // 9) 系统规则评分
        sb.append("\n【系统规则评分（供参考，可修正；注意股价位置已参与修正）】\n");
        sb.append("评分语义：四维分与综合分均为正表示偏多、为负表示偏空，负值已按权重如实计入综合分，负得越多看空越强。\n");
        sb.append("技术面 ").append(score.get("tech")).append(" 分、基本面 ").append(score.get("fund"))
                .append(" 分、资金筹码面 ").append(score.get("flow")).append(" 分、消息面 ").append(score.get("news"))
                .append(" 分，加权综合 ").append(score.get("composite")).append(" 分（权重 35/30/20/15，")
                .append(score.get("envDesc")).append("），等级：").append(score.get("valueLevel")).append("\n");
        sb.append("股价位置：").append(score.get("positionDesc")).append("\n");
        // 评分明细：每维度得分如何得出的逐条加减分记录，AI必须引用并解释，让用户看得懂分是怎么来的
        appendScoreDetail(sb, "技术面评分构成", score.get("techDetail"));
        appendScoreDetail(sb, "基本面评分构成", score.get("fundDetail"));
        appendScoreDetail(sb, "资金筹码面评分构成", score.get("flowDetail"));
        appendScoreDetail(sb, "消息面评分构成", score.get("newsDetail"));

        // 10) 输出要求
        sb.append("\n请输出 Markdown 格式分析报告，必须包含以下章节（顺序固定）：\n");
        sb.append("## 综合结论（200字以内：第一句必须点明股价位置，随后给出明确评级[买入/增持/观望/减持/卖出]与首要逻辑）\n");
        sb.append("## 技术面详解（先列出技术面评分构成中每条加减分项并解释其含义，让读者明白技术分怎么来的；")
                .append("再逐项解读均线系统→MACD→KDJ→RSI→量价，结合股价位置判断当前趋势阶段[启动/加速/赶顶/阴跌/震荡]与买卖时机）\n");
        sb.append("## 基本面详解（先列出基本面评分构成逐条解释；再说明财务披露时效；逐项分析成长性/盈利质量/现金流/估值；")
                .append("结合历史披露后股价反应说明市场对业绩的认可度；低位业绩增=双击潜力，高位业绩增=预期兑现风险）\n");
        sb.append("## 资金面与筹码详解（先列出资金筹码评分构成逐条解释；再分析主力资金连续性→超大单动向→股东户数趋势→吸筹/出货判定；")
                .append("必须结合位置：低位吸筹可信度高，高位放量流入警惕对倒出货）\n");
        sb.append("## 消息面详解（先列出消息面评分构成逐条解释；再按时效分类，逐条标注利好/利空及实质影响；高位利好需评估兑现风险）\n");
        sb.append("## 板块与大盘环境（板块强度与排名、个股相对板块强弱、大盘与市场情绪对个股中短期的影响）\n");
        sb.append("## 股价位置综合评估（汇总当前位置下技术/基本面/资金/消息四类信号的可信度修正结论，明确该位置的操作基调）\n");
        sb.append("## 投资价值评估（AI综合评分0-100与理由；与系统规则评分对比，说明差异原因）\n");
        sb.append("## 风险点（逐条列出，标注发生概率与影响程度）\n");
        sb.append("## 操作建议（①评级 ②建议仓位区间[结合位置：低位可积极、中位稳健、高位防守] ③短线与中线视角 "
                + "④参考支撑位与压力位[基于年内低点/高点/MA20/MA60给出具体价位] ⑤止损参考位）\n");
        sb.append("要求：数据驱动、有理有据、观点明确、不空话套话；每个结论必须能对应到上文具体数据；若数据缺失如实说明。");

        String systemPrompt = "你是一位资深证券投资分析师，擅长A股个股综合分析。分析必须客观严谨、数据驱动、有理有据："
                + "每个结论都要先引用具体数据再解释含义；所有判断必须结合股价位置（近一年区间位置、距高点回撤）进行修正——"
                + "同样的信号在不同位置含义完全不同。明确指出机会与风险，不构成绝对投资建议但要有明确倾向性判断。用简体中文回答。";
        String ai = aiCommonUtil.callWithSystem(systemPrompt, sb.toString());
        if (ai == null || ai.trim().isEmpty()) {
            StringBuilder fallback = new StringBuilder();
            fallback.append("AI 分析暂时不可用（请检查 ai.common 配置），以下为规则评分结果：\n\n")
                    .append("- 综合评分：").append(score.get("composite")).append("（").append(score.get("valueLevel")).append("）\n")
                    .append("- 技术面：").append(score.get("tech")).append(" / 基本面：").append(score.get("fund"))
                    .append(" / 资金筹码面：").append(score.get("flow")).append(" / 消息面：").append(score.get("news")).append("\n")
                    .append("- ").append(score.get("positionDesc")).append("\n");
            appendScoreDetail(fallback, "技术面评分构成", score.get("techDetail"));
            appendScoreDetail(fallback, "基本面评分构成", score.get("fundDetail"));
            appendScoreDetail(fallback, "资金筹码面评分构成", score.get("flowDetail"));
            appendScoreDetail(fallback, "消息面评分构成", score.get("newsDetail"));
            return fallback.toString();
        }
        return ai;
    }

    @Override
    public void updateStockBasic(StockBasic stockBasic) {
        stockBasic.setUpdateBy("system");
        stockAssetMapper.updateStockBasic(stockBasic);
    }

    @Override
    public void deleteStockDataByCode(String stockCode) {
        stockAssetMapper.deleteStockKlineByCode(stockCode);
        stockAssetMapper.deleteStockFinanceByCode(stockCode);
        stockAssetMapper.deleteStockCapitalFlowByCode(stockCode);
        stockAssetMapper.deleteStockNewsByCode(stockCode);
        stockAssetMapper.deleteStockHolderNumByCode(stockCode);
        stockAssetMapper.deleteStockBasicByCode(stockCode);
    }

    @Override
    public List<StockBasic> getAllStockBasic() {
        return stockAssetMapper.getAllStockBasic();
    }
}
