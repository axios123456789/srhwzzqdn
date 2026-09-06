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
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class StockAssetServiceImpl implements StockAssetService {

    private static final Logger logger = LoggerFactory.getLogger(StockAssetServiceImpl.class);

    @Autowired
    private StockAssetMapper stockAssetMapper;

    private static final String QUOTE_URL = "http://push2.eastmoney.com/api/qt/stock/get";
    private static final String KLINE_URL = "http://push2his.eastmoney.com/api/qt/stock/kline/get";
    private static final String FLOW_URL = "http://push2.eastmoney.com/api/qt/stock/fflow/daykline/get";
    private static final String FINANCE_URL = "https://datacenter.eastmoney.com/securities/api/data/get";

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

        // 财务指标（最近12个报告期）
        List<StockFinance> financeList = fetchFinanceData(stockCode);
        if (!financeList.isEmpty()) {
            stockAssetMapper.batchAddStockFinance(financeList);
        }

        List<StockCapitalFlow> flowList = fetchCapitalFlowData(secid, stockCode, 30);
        if (!flowList.isEmpty()) {
            stockAssetMapper.batchAddStockCapitalFlow(flowList);
        }

        return "成功获取股票数据：" + stockBasic.getStockName() + "（" + stockCode + "），含行情/K线/财务/资金流";
    }

    private StockBasic fetchStockBasic(String secid, String stockCode) {
        String fields = "f43,f44,f45,f46,f47,f48,f57,f58,f59,f60,f84,f85,f116,f117,f162,f163,f167,f168,f169,f170,f171,f173,f184,f186,f187,f188,f189,f277,f292";
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
            stock.setVolumeRatio(toDecimal(d.get("f184")));
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
        String url = FLOW_URL + "?secid=" + secid + "&lmt=" + days;
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
     * 获取最近12个报告期的主要财务指标（东方财富F10接口）
     * 金额统一换算为亿元，比率为原值
     */
    private List<StockFinance> fetchFinanceData(String stockCode) {
        String secucode = stockCode + (stockCode.startsWith("6") ? ".SH" : ".SZ");
        String url = FINANCE_URL +
                "?type=RPT_F10_FINANCE_MAINFINADATA&sty=APP_F10_MAINFINADATA" +
                "&filter=(SECUCODE%3D%22" + secucode + "%22)" +
                "&p=1&ps=12&sr=-1&st=REPORT_DATE&source=HSF10&client=PC";
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
     * 批量刷新所有股票的最新实时数据：
     * 更新实时行情（部分字段更新，不覆盖行业/公司信息等），
     * 重建三种周期K线与资金流向，补全缺失的财务数据
     */
    @Override
    public String refreshAllStockRealtime() {
        List<StockBasic> all = stockAssetMapper.getAllStockBasic();
        if (all == null || all.isEmpty()) {
            return "暂无股票，请先通过【数据获取】添加股票";
        }
        int ok = 0, fail = 0;
        for (StockBasic item : all) {
            String code = item.getStockCode();
            try {
                String secid = buildSecId(code);
                StockBasic quote = fetchStockBasic(secid, code);
                if (quote == null || quote.getLastPrice() == null) {
                    fail++;
                    continue;
                }
                quote.setStockCode(code);
                quote.setUpdateBy("system");
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

                // 补全缺失的财务数据
                List<StockFinance> exist = stockAssetMapper.getStockFinance(code, 1);
                if (exist == null || exist.isEmpty()) {
                    List<StockFinance> fins = fetchFinanceData(code);
                    if (!fins.isEmpty()) {
                        stockAssetMapper.batchAddStockFinance(fins);
                    }
                }
                ok++;
                Thread.sleep(200); // 限速，避免触发接口风控
            } catch (Exception e) {
                fail++;
                logger.error("刷新股票实时数据失败: {}", code, e);
            }
        }
        return "实时数据刷新完成：共" + all.size() + "只，成功" + ok + "只，失败" + fail + "只";
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
    public void updateStockBasic(StockBasic stockBasic) {
        stockBasic.setUpdateBy("system");
        stockAssetMapper.updateStockBasic(stockBasic);
    }

    @Override
    public void deleteStockDataByCode(String stockCode) {
        stockAssetMapper.deleteStockKlineByCode(stockCode);
        stockAssetMapper.deleteStockFinanceByCode(stockCode);
        stockAssetMapper.deleteStockCapitalFlowByCode(stockCode);
        stockAssetMapper.deleteStockBasicByCode(stockCode);
    }

    @Override
    public List<StockBasic> getAllStockBasic() {
        return stockAssetMapper.getAllStockBasic();
    }
}
