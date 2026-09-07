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

            // 重建K线（删旧插新，全量历史保证三种周期完整）
            stockAssetMapper.deleteStockKlineByCode(code);
            for (int[] kt : new int[][]{{1, 10000}, {2, 2000}, {3, 600}}) {
                List<StockKline> ks = fetchKlineData(secid, code, kt[0], kt[1]);
                insertKlineInBatches(ks);
            }

            // 重建资金流向
            stockAssetMapper.deleteStockCapitalFlowByCode(code);
            List<StockCapitalFlow> fs = fetchCapitalFlowData(secid, code, 30);
            if (!fs.isEmpty()) {
                stockAssetMapper.batchAddStockCapitalFlow(fs);
            }

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
        List<StockKline> daily = stockAssetMapper.getStockKline(stockCode, 1, 300);
        Collections.reverse(daily); // 转为时间升序
        if (daily.isEmpty()) {
            throw new RuntimeException("该股票无K线数据，请先获取/刷新数据");
        }
        List<StockFinance> finances = stockAssetMapper.getStockFinance(stockCode, 6);
        List<StockCapitalFlow> flows = stockAssetMapper.getStockCapitalFlow(stockCode, 15);
        List<StockNews> newsList = stockAssetMapper.getStockNews(stockCode, 15);

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

        // ===== 板块情况 =====
        JSONObject sectorInfo = fetchSectorInfo(basic.getIndustry());

        // ===== 规则评分 =====
        // 技术面 0-100
        double tech = 0;
        if (lastClose > ma20) tech += 10;
        if (ma5 > ma10 && ma10 > ma20) tech += 10;
        if (macd[0] > macd[1]) tech += 10;
        if (macd[2] > 0) tech += 5;
        if (rsi14 >= 40 && rsi14 <= 70) tech += 10;
        else if (rsi14 > 80) tech -= 10;
        else if (rsi14 < 30) tech += 5;
        StockKline lastK = daily.get(daily.size() - 1);
        StockKline prevK = daily.size() > 1 ? daily.get(daily.size() - 2) : null;
        if (prevK != null && lastK.getClosePrice().compareTo(prevK.getClosePrice()) > 0
                && prevK.getVolume() != null && lastK.getVolume() != null
                && lastK.getVolume() > prevK.getVolume()) tech += 5;
        tech = Math.max(0, Math.min(100, tech * 2)); // 满分50→100

        // 基本面 0-100
        double fund = 50;
        if (!finances.isEmpty()) {
            StockFinance f = finances.get(0);
            fund = 0;
            Double npYoy = toDouble(f.getNetProfitYoy());
            Double revYoy = toDouble(f.getRevenueYoy());
            Double roe = toDouble(f.getRoe());
            Double gross = toDouble(f.getGrossMargin());
            if (npYoy != null) {
                if (npYoy > 20) fund += 15; else if (npYoy > 0) fund += 10; else fund -= 10;
            }
            if (revYoy != null) {
                if (revYoy > 20) fund += 10; else if (revYoy > 0) fund += 5; else fund -= 5;
            }
            if (roe != null) {
                if (roe > 10) fund += 10; else if (roe > 0) fund += 5; else fund -= 5;
            }
            if (gross != null && gross > 30) fund += 5;
            BigDecimal pe = basic.getPeTtm();
            if (pe != null) {
                if (pe.doubleValue() > 0 && pe.doubleValue() < 30) fund += 10;
                else if (pe.doubleValue() < 0) fund -= 5;
            }
            fund = Math.max(0, Math.min(100, fund));
        }

        // 资金面 0-100
        double flowScore = 50;
        if (!flows.isEmpty()) {
            List<StockCapitalFlow> asc = new ArrayList<>(flows);
            Collections.reverse(asc); // 升序
            double sum5 = asc.stream().skip(Math.max(0, asc.size() - 5))
                    .mapToDouble(x -> toDouble(x.getMainNetInflow()) == null ? 0 : x.getMainNetInflow().doubleValue()).sum();
            double sum10 = asc.stream().skip(Math.max(0, asc.size() - 10))
                    .mapToDouble(x -> toDouble(x.getMainNetInflow()) == null ? 0 : x.getMainNetInflow().doubleValue()).sum();
            flowScore = 0;
            if (sum5 > 0) flowScore += 15;
            else flowScore -= 10;
            if (sum10 > 0) flowScore += 10;
            else flowScore -= 5;
            StockCapitalFlow today = asc.get(asc.size() - 1);
            if (today.getMainNetInflow() != null && today.getMainNetInflow().doubleValue() > 0) flowScore += 5;
            flowScore = Math.max(0, Math.min(100, flowScore * 4)); // 满分30→100
        }

        // 消息面活跃度 0-100（情绪由AI判断）
        double newsScore = 40;
        if (!newsList.isEmpty()) {
            Date latest = newsList.get(0).getPublishTime();
            long daysSince = latest == null ? 999 :
                    (System.currentTimeMillis() - latest.getTime()) / 86400000L;
            if (daysSince <= 3) newsScore += 30;
            else if (daysSince <= 7) newsScore += 15;
            long annIn7d = newsList.stream().filter(n -> n.getNewsType() != null && n.getNewsType() == 2)
                    .filter(n -> n.getPublishTime() != null)
                    .filter(n -> (System.currentTimeMillis() - n.getPublishTime().getTime()) / 86400000L <= 7).count();
            if (annIn7d >= 2) newsScore += 30;
            else if (annIn7d >= 1) newsScore += 15;
            newsScore = Math.max(0, Math.min(100, newsScore));
        }

        double composite = Math.round(tech * 0.35 + fund * 0.30 + flowScore * 0.20 + newsScore * 0.15);
        String valueLevel;
        if (composite >= 80) valueLevel = "A（投资价值较高）";
        else if (composite >= 70) valueLevel = "B（有一定投资价值）";
        else if (composite >= 60) valueLevel = "C（中性观察）";
        else if (composite >= 50) valueLevel = "D（偏弱谨慎）";
        else valueLevel = "E（风险较大，宜回避）";

        Map<String, Object> score = new HashMap<>();
        score.put("composite", composite);
        score.put("tech", Math.round(tech));
        score.put("fund", Math.round(fund));
        score.put("flow", Math.round(flowScore));
        score.put("news", Math.round(newsScore));
        score.put("valueLevel", valueLevel);
        score.put("positionDesc", String.format("现价 %.2f 处于近一年区间 %.0f%% 位置（区间 %.2f~%.2f），距年内高点回撤 %.1f%%",
                lastClose, positionPct, yearLow, yearHigh, drawdownFromHigh));

        // ===== AI 综合分析 =====
        List<StockHolderNum> holders = stockAssetMapper.getStockHolderNum(stockCode, 8);
        String aiAnalysis = callAiAnalysis(basic, daily, ma5, ma10, ma20, ma60, macd, kdj, rsi14,
                positionPct, yearLow, yearHigh, drawdownFromHigh, sectorInfo, flows, finances, newsList, holders, score);

        Map<String, Object> result = new HashMap<>();
        result.put("ruleScore", score);
        result.put("aiAnalysis", aiAnalysis);
        result.put("sectorInfo", sectorInfo);
        return result;
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
                                  List<StockHolderNum> holders, Map<String, Object> score) {
        SimpleDateFormat daySdf = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat fullSdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        StringBuilder sb = new StringBuilder();
        sb.append("请基于以下实时数据对该股票进行专业综合分析。\n\n");
        sb.append("【公司概况与行情估值】\n");
        sb.append("代码/名称：").append(basic.getStockCode()).append(" ").append(basic.getStockName())
                .append("，所属行业：").append(basic.getIndustry()).append("，板块：").append(basic.getSector())
                .append("，概念：").append(basic.getConceptSectors()).append("\n");
        sb.append("最新价：").append(basic.getLastPrice()).append("，当日涨跌幅：").append(basic.getChangePct()).append("%")
                .append("，换手率：").append(basic.getTurnoverRate()).append("%，量比：").append(basic.getVolumeRatio()).append("\n");
        sb.append("总市值：").append(basic.getTotalMarketCap()).append("亿，市盈率TTM：").append(basic.getPeTtm())
                .append("，市净率：").append(basic.getPbRatio()).append("\n");
        sb.append("技术指标：MA5=").append(String.format("%.2f", ma5)).append("，MA10=").append(String.format("%.2f", ma10))
                .append("，MA20=").append(String.format("%.2f", ma20)).append("，MA60=").append(String.format("%.2f", ma60))
                .append("，MACD(DIF,DEA,HIST)=").append(String.format("%.3f,%.3f,%.3f", macd[0], macd[1], macd[2]))
                .append("，KDJ(K,D,J)=").append(String.format("%.1f,%.1f,%.1f", kdj[0], kdj[1], kdj[2]))
                .append("，RSI14=").append(String.format("%.1f", rsi14)).append("\n");
        sb.append("股价位置：现价处于近一年区间 ").append(String.format("%.0f%%", positionPct))
                .append(" 位置（").append(String.format("%.2f~%.2f", yearLow, yearHigh))
                .append("），距年内高点回撤 ").append(String.format("%.1f%%", drawdown)).append("\n");
        if (sectorInfo != null) {
            sb.append("板块情况：所属行业板块「").append(sectorInfo.getString("name")).append("」今日涨跌幅 ")
                    .append(sectorInfo.get("changePct")).append("%");
            if (sectorInfo.get("rank") != null) {
                sb.append("，在全部 ").append(sectorInfo.get("total")).append(" 个行业板块中涨幅排名第 ").append(sectorInfo.get("rank")).append(" 名");
            }
            sb.append("（排名靠前=板块强势）\n");
        }
        if (!finances.isEmpty()) {
            sb.append("\n【基本面-近几期财务指标】\n");
            for (StockFinance f : finances) {
                sb.append(f.getReportDate() == null ? "" : daySdf.format(f.getReportDate()))
                        .append("：营收").append(f.getRevenue()).append("亿(同比").append(f.getRevenueYoy()).append("%)")
                        .append("，净利润").append(f.getNetProfit()).append("亿(同比").append(f.getNetProfitYoy()).append("%)")
                        .append("，ROE ").append(f.getRoe()).append("%，毛利率").append(f.getGrossMargin()).append("%，负债率").append(f.getDebtRatio()).append("%\n");
            }
        }
        if (!flows.isEmpty()) {
            sb.append("\n【资金面-主力资金近 ").append(flows.size()).append(" 日流向（万元）】\n");
            double sum5 = 0, sum10 = 0;
            for (int i = 0; i < flows.size(); i++) {
                StockCapitalFlow fl = flows.get(i);
                double main = fl.getMainNetInflow() == null ? 0 : fl.getMainNetInflow().doubleValue();
                if (i < 5) sum5 += main;
                if (i < 10) sum10 += main;
                if (i < 15) {
                    sb.append(daySdf.format(fl.getTradeDate())).append("：主力").append(main).append("万\n");
                }
            }
            sb.append("近5日主力合计：").append(String.format("%.0f", sum5)).append("万，近10日合计：")
                    .append(String.format("%.0f", sum10)).append("万\n");
        }
        if (!newsList.isEmpty()) {
            sb.append("\n【消息面-最新消息（注意结合时间判断时效性，当前时间：").append(fullSdf.format(new Date())).append("）】\n");
            for (StockNews n : newsList) {
                sb.append(n.getPublishTime() == null ? "" : fullSdf.format(n.getPublishTime()))
                        .append(" [").append(n.getNewsType() != null && n.getNewsType() == 2 ? "公告" : "新闻").append("]")
                        .append(n.getTitle()).append("\n");
            }
        }
        if (holders != null && !holders.isEmpty()) {
            sb.append("\n【股东人数/筹码结构（股东户数下降=筹码集中主力吸筹；上升=筹码分散散户接盘）】\n");
            for (StockHolderNum h : holders) {
                sb.append(daySdf.format(h.getEndDate()))
                        .append("：股东户数 ").append(h.getHolderNum())
                        .append("，较上期变化率 ").append(h.getHolderNumRatio() == null ? "-" : h.getHolderNumRatio()).append("%")
                        .append("，户均持股市值 ").append(h.getAvgMarketCap() == null ? "-" : h.getAvgMarketCap()).append("元\n");
            }
        }
        sb.append("\n【系统规则评分（供参考，可修正）】\n");
        sb.append("技术面 ").append(score.get("tech")).append(" 分、基本面 ").append(score.get("fund"))
                .append(" 分、资金面 ").append(score.get("flow")).append(" 分、消息面活跃度 ").append(score.get("news"))
                .append(" 分，加权综合 ").append(score.get("composite")).append(" 分（权重 35/30/20/15），等级：").append(score.get("valueLevel")).append("\n");
        sb.append("\n请输出 Markdown 格式分析报告，必须包含以下章节：\n");
        sb.append("## 综合结论（200字以内，明确观点）\n## 基本面分析（结合财务趋势与估值）\n");
        sb.append("## 技术面分析（结合均线/MACD/KDJ/RSI与股价位置，判断趋势与买卖点）\n## 资金面分析（主力意图）\n");
        sb.append("## 消息面分析（重点结合消息时间与当前时间的时效性判断影响）\n## 板块联动分析（板块强度对个股影响）\n");
        sb.append("## 筹码结构分析（结合股东人数各期趋势、户均持股变化与资金面，判断当前是主力吸筹还是散户接盘，注意公告日期距当前时间的时效性）\n");
        sb.append("## 投资价值评估（给出你的AI综合评分0-100与理由）\n## 风险点（逐条列出）\n");
        sb.append("## 操作建议（短线/中线视角，含参考点位）\n");
        sb.append("要求：数据驱动、观点明确、不空话套话；若数据缺失如实说明。");

        String systemPrompt = "你是一位资深证券投资分析师，擅长A股个股综合分析。分析必须客观严谨，基于给定数据，"
                + "明确指出机会与风险，不构成绝对投资建议但要有明确倾向性判断。用简体中文回答。";
        String ai = aiCommonUtil.callWithSystem(systemPrompt, sb.toString());
        if (ai == null || ai.trim().isEmpty()) {
            return "AI 分析暂时不可用（请检查 ai.common 配置），以下为规则评分结果：\n\n"
                    + "- 综合评分：" + score.get("composite") + "（" + score.get("valueLevel") + "）\n"
                    + "- 技术面：" + score.get("tech") + " / 基本面：" + score.get("fund")
                    + " / 资金面：" + score.get("flow") + " / 消息面：" + score.get("news") + "\n"
                    + "- " + score.get("positionDesc");
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
