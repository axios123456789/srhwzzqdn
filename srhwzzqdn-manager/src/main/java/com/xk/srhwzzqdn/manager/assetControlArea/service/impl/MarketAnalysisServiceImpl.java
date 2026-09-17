package com.xk.srhwzzqdn.manager.assetControlArea.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.xk.srhwzzqdn.manager.assetControlArea.mapper.MarketAnalysisMapper;
import com.xk.srhwzzqdn.manager.assetControlArea.service.MarketAnalysisService;
import com.xk.srhwzzqdn.manager.util.AiCommonUtil;
import com.xk.srhwzzqdn.model.entity.assetControl.MarketAnalysisDaily;
import org.apache.http.client.config.RequestConfig;
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
import java.util.concurrent.ConcurrentHashMap;

/**
 * 市场实时分析服务实现
 * <p>
 * 数据全部来自东方财富公开接口（接口优先，字段映射均已实测验证）：
 * - 大盘指数+涨跌家数+全市场资金流：qt/ulist.np/get（f104/f105/f106 为沪深两市全市场涨跌平家数，f62/f66/f72/f78/f84 为大盘主力/超大/大/中/小单净流入）
 * - 昨日两市成交额：qt/stock/kline/get 指数日线（取昨日成交额做量能对比）
 * - 涨停池/炸板池/跌停池：push2ex getTopicZTPool/getTopicZBPool/getTopicDTPool（lbc 连板数、fbt 封板时间、hybk 行业、p 价格×1000）
 * - 行业/概念板块资金流：qt/clist/get fs=m:90+t:2 / m:90+t:3，f62 主力净流入排序（f128/f140/f136 领涨股）
 * - 7×24财经快讯：np-listapi getFastNewsList（消息面核心来源）
 * - 两融余额：datacenter RPTA_RZRQ_LSHJ（T-1）
 * - 北向资金：2024年8月起交易所已停止实时净买入披露，页面如实提示并以两融+主力资金衡量增量资金态度
 * <p>
 * 竞价时段（9:15-9:25）行情接口自然返回集合竞价撮合数据，开盘后返回实时行情，无需分接口处理。
 * 落库规则：market_date 按天唯一，重分析当天先删当天数据再插入，绝不触碰其他交易日记录。
 */
@Service
public class MarketAnalysisServiceImpl implements MarketAnalysisService {

    private static final Logger logger = LoggerFactory.getLogger(MarketAnalysisServiceImpl.class);

    @Autowired
    private MarketAnalysisMapper marketAnalysisMapper;

    @Autowired
    private AiCommonUtil aiCommonUtil;

    // 行情集群failover：push2delay 延迟行情集群优先（稳定），push2 兜底；push2 高频会被临时拉黑
    private static final String[] PUSH2_HOSTS = {"http://push2delay.eastmoney.com", "http://push2.eastmoney.com"};
    private static final String INDEX_URL_TPL =
            "%s/api/qt/ulist.np/get?fltt=2&secids=1.000001,0.399001,0.399006,1.000688"
                    + "&fields=f2,f3,f4,f6,f12,f14,f62,f66,f72,f78,f84,f184,f104,f105,f106,f124";
    private static final String KLINE_URL =
            "http://push2his.eastmoney.com/api/qt/stock/kline/get?secid=%s&klt=101&fqt=1&lmt=3&end=20500101"
                    + "&fields1=f1,f2,f3&fields2=f51,f52,f53,f54,f55,f56,f57";
    private static final String ZT_POOL_URL_TPL =
            "http://push2ex.eastmoney.com/getTopicZTPool?ut=7eea3edcaed734bea9cbfc24409ed989&dpt=wz.ztzt"
                    + "&Pageindex=0&pagesize=600&sort=fbt%%3Aasc&date=%s";
    private static final String ZB_POOL_URL_TPL =
            "http://push2ex.eastmoney.com/getTopicZBPool?ut=7eea3edcaed734bea9cbfc24409ed989&dpt=wz.ztzt"
                    + "&Pageindex=0&pagesize=600&sort=fbt%%3Aasc&date=%s";
    private static final String DT_POOL_URL_TPL =
            "http://push2ex.eastmoney.com/getTopicDTPool?ut=7eea3edcaed734bea9cbfc24409ed989&dpt=wz.ztzt"
                    + "&Pageindex=0&pagesize=600&sort=fund%%3Aasc&date=%s";
    private static final String SECTOR_URL_TPL =
            "%s/api/qt/clist/get?fltt=2&pn=1&pz=%d&po=%d&fid=%s&fs=%s"
                    + "&fields=f12,f14,f3,f62,f66,f72,f184,f128,f140,f136";
    private static final String NEWS_URL_TPL =
            "https://np-listapi.eastmoney.com/comm/web/getFastNewsList?client=web&biz=web_724"
                    + "&fastColumn=102&sortEnd=&pageSize=30&req_trace=%d";
    private static final String MARGIN_URL =
            "https://datacenter-web.eastmoney.com/api/data/v1/get?reportName=RPTA_RZRQ_LSHJ&columns=ALL"
                    + "&source=WEB&sortColumns=dim_date&sortTypes=-1&pageSize=1&pageNumber=1";

    // 实时聚合短缓存：切标签页/连续刷新时1分钟内复用，避免密集请求触发东财限流
    private static final long REALTIME_CACHE_TTL_MS = 60 * 1000L;
    private static volatile Map<String, Object> realtimeCache = null;
    private static volatile long realtimeCacheTime = 0L;

    // AI分析结果缓存：以数据指纹为key，数据未变（如收盘后）10分钟内复用，避免重复等待
    private static final long AI_CACHE_TTL_MS = 10 * 60 * 1000L;
    private static final int AI_CACHE_MAX = 8;
    private final Map<String, Map<String, Object>> aiCache = new ConcurrentHashMap<>();

    @Override
    public Map<String, Object> getRealtimeAnalysis() {
        long now = System.currentTimeMillis();
        if (realtimeCache != null && now - realtimeCacheTime < REALTIME_CACHE_TTL_MS) {
            return realtimeCache;
        }
        Map<String, Object> result = buildRealtimeAnalysis();
        realtimeCache = result;
        realtimeCacheTime = System.currentTimeMillis();
        return result;
    }

    @Override
    public Map<String, Object> analyzeWithAi() {
        Map<String, Object> realtime = getRealtimeAnalysis();

        // 数据指纹：核心数据未变（收盘后场景）时直接复用AI结果
        String fingerprint = buildFingerprint(realtime);
        Map<String, Object> cached = aiCache.get(fingerprint);
        if (cached != null && System.currentTimeMillis() - (long) cached.get("_cacheTime") < AI_CACHE_TTL_MS) {
            Map<String, Object> merged = new LinkedHashMap<>(realtime);
            merged.put("ai", cached.get("ai"));
            merged.put("saved", cached.get("saved"));
            return merged;
        }

        Map<String, Object> aiResult = callAiAnalysis(realtime);
        Map<String, Object> merged = new LinkedHashMap<>(realtime);
        merged.put("ai", aiResult);

        // 按天落库：当天已有记录先删当天再插入，保证始终是最新实时分析；不触碰其他交易日
        boolean saved = persistAnalysis(realtime, aiResult);
        merged.put("saved", saved);

        Map<String, Object> cacheEntry = new HashMap<>();
        cacheEntry.put("ai", aiResult);
        cacheEntry.put("_cacheTime", System.currentTimeMillis());
        cacheEntry.put("saved", saved);
        if (aiCache.size() >= AI_CACHE_MAX) {
            aiCache.clear();
        }
        aiCache.put(fingerprint, cacheEntry);
        return merged;
    }

    @Override
    public MarketAnalysisDaily getTodayAnalysis() {
        ensureTable();
        return marketAnalysisMapper.selectByMarketDate(new Date());
    }

    @Override
    public List<Map<String, Object>> getHistoryList(Integer limit) {
        ensureTable();
        int n = (limit == null || limit <= 0) ? 10 : Math.min(limit, 30);
        List<MarketAnalysisDaily> rows = marketAnalysisMapper.selectRecent(n);
        List<Map<String, Object>> list = new ArrayList<>();
        SimpleDateFormat dayFmt = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat timeFmt = new SimpleDateFormat("HH:mm:ss");
        for (MarketAnalysisDaily row : rows) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("marketDate", row.getMarketDate() == null ? null : dayFmt.format(row.getMarketDate()));
            m.put("analysisTime", row.getAnalysisTime() == null ? null : timeFmt.format(row.getAnalysisTime()));
            m.put("marketPhase", row.getMarketPhase());
            m.put("sentimentScore", row.getSentimentScore());
            m.put("sentimentLevel", row.getSentimentLevel());
            m.put("marketStatus", row.getMarketStatus());
            m.put("mainLine", row.getMainLine());
            m.put("mainLineSustainability", row.getMainLineSustainability());
            m.put("limitUpCount", row.getLimitUpCount());
            m.put("mainNetInflowYi", yi(row.getMainNetInflow()));
            m.put("shChangePct", row.getShChangePct());
            m.put("aiOutlook", row.getAiOutlook());
            m.put("aiHistoryReview", row.getAiHistoryReview());
            list.add(m);
        }
        return list;
    }

    @Override
    public Map<String, Object> getHistoryReview() {
        ensureTable();
        List<MarketAnalysisDaily> rows = marketAnalysisMapper.selectRecent(9);
        Map<String, Object> result = new LinkedHashMap<>();
        if (rows == null || rows.size() < 2) {
            result.put("hasData", false);
            result.put("message", "历史分析记录不足（至少需要2个交易日的分析数据），请先在其他交易日进行AI深度分析。");
            return result;
        }
        // rows 按日期倒序：rows[i-1] 比 rows[i] 晚一个交易日，构成「展望日 vs 次日实际」对比对
        List<Map<String, Object>> pairs = new ArrayList<>();
        for (int i = 0; i < rows.size() - 1; i++) {
            MarketAnalysisDaily newer = rows.get(i);   // 展望的次日（实际发生日）
            MarketAnalysisDaily older = rows.get(i + 1); // 做出展望的交易日
            if (older.getAiOutlook() == null || older.getAiOutlook().trim().isEmpty()) {
                continue;
            }
            Map<String, Object> pair = new LinkedHashMap<>();
            pair.put("outlookDate", fmtDay(older.getMarketDate()));
            pair.put("outlook", trunc(older.getAiOutlook(), 500));
            pair.put("nextDate", fmtDay(newer.getMarketDate()));
            pair.put("nextActual", String.format(
                    "上证涨跌幅%.2f%%，涨停%d家跌停%d家，主力净流入%.1f亿，最高连板%d板，市场主线：%s，情绪%d分(%s)",
                    val(newer.getShChangePct()), nvl(newer.getLimitUpCount()), nvl(newer.getLimitDownCount()),
                    yi(newer.getMainNetInflow()), nvl(newer.getMaxLianban()),
                    newer.getMainLine() == null ? "-" : newer.getMainLine(),
                    nvl(newer.getSentimentScore()), newer.getSentimentLevel()));
            pairs.add(pair);
        }
        result.put("hasData", true);
        result.put("pairs", pairs);
        if (pairs.isEmpty()) {
            result.put("hasData", false);
            result.put("message", "历史记录中暂无AI展望数据，请先进行AI深度分析。");
            return result;
        }
        String system = "你是A股市场策略复盘专家。用户给你若干组「某日AI展望」和「次日实际走势」数据，"
                + "请逐条评判展望准确性（准确/部分准确/偏差/错误），指出误判原因（如高估情绪持续性、低估板块轮动速度、忽视量能萎缩信号等），"
                + "最后总结3-5条可执行的纠错规则，供下次分析时规避同类失误。用Markdown输出，简明扼要，总字数600字以内。";
        String user = "以下是历史展望与实际走势对比数据：\n" + JSON.toJSONString(pairs);
        String review = aiCommonUtil.callWithSystem(system, user);
        result.put("review", review == null || review.isEmpty()
                ? "AI复盘暂时不可用，请稍后重试。" : review);
        return result;
    }

    // ==================== 实时聚合核心 ====================

    private Map<String, Object> buildRealtimeAnalysis() {
        Map<String, Object> result = new LinkedHashMap<>();

        String phase = detectPhase();
        Date now = new Date();

        // 1. 指数行情 + 全市场涨跌家数 + 大盘资金流（一次请求）
        JSONObject idx = fetchIndexQuotes();
        // 2. 昨日两市成交额（指数日线）
        JSONObject prevAmount = fetchPrevAmount();
        // 3. 涨停池/炸板池/跌停池（今日）+ 昨日涨停池（主线延续性对比）
        JSONObject ztToday = fetchPool(ZT_POOL_URL_TPL, true);
        JSONObject zbToday = fetchPool(ZB_POOL_URL_TPL, false);
        JSONObject dtToday = fetchPool(DT_POOL_URL_TPL, false);
        // 4. 板块资金流（行业+概念，净流入榜+净流出榜+涨幅榜）
        Map<String, List<Map<String, Object>>> sectors = fetchSectors();
        // 5. 7×24快讯
        List<Map<String, Object>> news = fetchNews();
        // 6. 两融（T-1）
        JSONObject margin = fetchMargin();

        // ---------- 指数与量能 ----------
        JSONArray indexes = idx.getJSONArray("indexes");
        BigDecimal totalAmount = idx.getBigDecimal("totalAmount");
        BigDecimal prevAmountYiBd = prevAmount.getBigDecimal("prevAmountYi");
        double prevAmountYi = prevAmountYiBd == null ? 0 : prevAmountYiBd.doubleValue();
        String prevDate = prevAmount.getString("prevDate");
        double totalAmountYi = totalAmount.doubleValue() / 1e8;
        double amountRatioPct = prevAmountYi > 0 ? totalAmountYi / prevAmountYi * 100 : 0;

        result.put("analysisTime", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(now));
        result.put("marketPhase", phase);
        result.put("phaseNote", phaseNote(phase, amountRatioPct));
        result.put("indexes", indexes);
        result.put("totalAmountYi", round2(totalAmountYi));
        result.put("prevAmountYi", round2(prevAmountYi));
        result.put("prevAmountDate", prevDate);
        result.put("amountRatioPct", round1(amountRatioPct));

        // ---------- 涨跌家数 ----------
        int upCount = idx.getIntValue("upCount");
        int downCount = idx.getIntValue("downCount");
        int flatCount = idx.getIntValue("flatCount");
        result.put("upCount", upCount);
        result.put("downCount", downCount);
        result.put("flatCount", flatCount);

        // ---------- 涨停跌停炸板与连板梯队 ----------
        int limitUp = ztToday.getIntValue("count");
        int zhaBan = zbToday.getIntValue("count");
        int limitDown = dtToday.getIntValue("count");
        double zhaBanRate = (limitUp + zhaBan) > 0 ? zhaBan * 100.0 / (limitUp + zhaBan) : 0;
        List<Map<String, Object>> ztList = parseZtStocks(ztToday.getJSONArray("pool"));
        int maxLianban = ztList.stream().mapToInt(s -> (int) s.get("lianban")).max().orElse(0);
        List<Map<String, Object>> lianbanTiers = buildLianbanTiers(ztList);
        result.put("limitUpCount", limitUp);
        result.put("limitDownCount", limitDown);
        result.put("zhaBanCount", zhaBan);
        result.put("zhaBanRate", round1(zhaBanRate));
        result.put("maxLianban", maxLianban);
        result.put("lianbanTiers", lianbanTiers);
        result.put("poolQdate", ztToday.getString("qdate"));

        // ---------- 资金流 ----------
        double mainNetYi = idx.getDoubleValue("mainNetYi");
        result.put("mainNetInflowYi", round1(mainNetYi));
        result.put("ultraNetInflowYi", round1(idx.getDoubleValue("ultraNetYi")));
        result.put("bigNetInflowYi", round1(idx.getDoubleValue("bigNetYi")));
        result.put("midNetInflowYi", round1(idx.getDoubleValue("midNetYi")));
        result.put("smallNetInflowYi", round1(idx.getDoubleValue("smallNetYi")));
        result.put("mainNetPct", idx.get("mainNetPct"));
        // 北向资金：2024年8月起停止实时披露，如实提示
        result.put("northNote", "北向资金实时净买入自2024年8月起交易所停止盘中披露，"
                + "增量资金态度以主力/超大单净流入与两融余额变化综合衡量。");

        // ---------- 两融 ----------
        result.put("marginBalanceYi", margin.get("balanceYi"));
        result.put("marginNetBuyYi", margin.get("netBuyYi"));
        result.put("marginDate", margin.get("date"));

        // ---------- 板块热力 ----------
        List<Map<String, Object>> hotSectors = sectors.get("hot");
        List<Map<String, Object>> coldSectors = sectors.get("cold");
        result.put("hotSectors", hotSectors);
        result.put("coldSectors", coldSectors);

        // ---------- 主线与持续性 ----------
        Map<String, Object> mainline = analyzeMainline(sectors, ztList, maxLianban, zhaBanRate);
        result.putAll(mainline);

        // ---------- 消息面 ----------
        result.put("news", news);

        // ---------- 情绪与结论 ----------
        double shPct = 0;
        if (indexes != null && !indexes.isEmpty()) {
            shPct = ((Number) indexes.getJSONObject(0).get("changePct")).doubleValue();
        }
        result.put("shChangePct", round2(shPct));
        int sentimentScore = calcSentiment(upCount, downCount, shPct, limitUp, limitDown, zhaBanRate, maxLianban, mainNetYi, amountRatioPct);
        String sentimentLevel = sentimentLevel(sentimentScore);
        String marketStatus = buildMarketStatus(shPct, upCount, downCount, limitUp, zhaBan, zhaBanRate,
                maxLianban, mainNetYi, totalAmountYi, sentimentLevel, hotSectors);
        result.put("sentimentScore", sentimentScore);
        result.put("sentimentLevel", sentimentLevel);
        result.put("marketStatus", marketStatus);

        // ---------- 历史摘要（近5天库内记录） ----------
        try {
            result.put("history", getHistoryList(5));
        } catch (Exception e) {
            result.put("history", Collections.emptyList());
        }

        return result;
    }

    /** 指数行情 + 全市场涨跌家数 + 大盘资金流（沪深指数f104/f105/f106相加，创业板为其子集不重复计） */
    private JSONObject fetchIndexQuotes() {
        JSONObject out = new JSONObject();
        JSONArray indexes = new JSONArray();
        BigDecimal totalAmount = BigDecimal.ZERO;
        int up = 0, down = 0, flat = 0;
        double mainNet = 0, ultraNet = 0, bigNet = 0, midNet = 0, smallNet = 0;
        BigDecimal mainNetPct = BigDecimal.ZERO;
        long quoteTime = 0;
        try {
            String body = httpGetHosts(String.format(INDEX_URL_TPL, PUSH2_HOSTS[0]));
            if (body == null) body = httpGetHosts(String.format(INDEX_URL_TPL, PUSH2_HOSTS[1]));
            JSONObject json = JSON.parseObject(body);
            JSONArray diff = json.getJSONObject("data").getJSONArray("diff");
            for (int i = 0; i < diff.size(); i++) {
                JSONObject d = diff.getJSONObject(i);
                JSONObject item = new JSONObject(true);
                item.put("code", d.getString("f12"));
                item.put("name", d.getString("f14"));
                item.put("close", d.getBigDecimal("f2"));
                item.put("changePct", d.getBigDecimal("f3"));
                item.put("changeAmt", d.getBigDecimal("f4"));
                item.put("amountYi", round2(d.getDoubleValue("f6") / 1e8));
                indexes.add(item);
                if (i <= 1) { // 仅 上证(沪全市场) + 深证成指(深全市场) 的家数口径，避免创业板重复
                    up += d.getIntValue("f104");
                    down += d.getIntValue("f105");
                    flat += d.getIntValue("f106");
                }
                if (i <= 1) {
                    mainNet += d.getDoubleValue("f62");
                    ultraNet += d.getDoubleValue("f66");
                    bigNet += d.getDoubleValue("f72");
                    midNet += d.getDoubleValue("f78");
                    smallNet += d.getDoubleValue("f84");
                    if (i == 0) mainNetPct = d.getBigDecimal("f184");
                }
                // 仅累加上证综指(沪全市场)+深证成指(深全市场口径)；创业板指/科创50为子集，重复计入会虚高
                if (i <= 1) {
                    totalAmount = totalAmount.add(BigDecimal.valueOf(d.getDoubleValue("f6")));
                }
                quoteTime = Math.max(quoteTime, d.getLongValue("f124"));
            }
        } catch (Exception e) {
            logger.error("获取大盘指数行情失败", e);
        }
        if (quoteTime > 0) {
            out.put("quoteTime", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(quoteTime * 1000L)));
        }
        out.put("indexes", indexes);
        out.put("totalAmount", totalAmount);
        out.put("upCount", up);
        out.put("downCount", down);
        out.put("flatCount", flat);
        out.put("mainNetYi", mainNet / 1e8);
        out.put("ultraNetYi", ultraNet / 1e8);
        out.put("bigNetYi", bigNet / 1e8);
        out.put("midNetYi", midNet / 1e8);
        out.put("smallNetYi", smallNet / 1e8);
        out.put("mainNetPct", mainNetPct);
        return out;
    }

    /** 昨日两市全天成交额（沪+深指数日线，量能对比基准） */
    private JSONObject fetchPrevAmount() {
        JSONObject out = new JSONObject();
        double prev = 0;
        String prevDate = null;
        String today = new SimpleDateFormat("yyyyMMdd").format(new Date());
        for (String secid : new String[]{"1.000001", "0.399001"}) {
            try {
                String body = httpGet(String.format(KLINE_URL, secid));
                JSONObject json = JSON.parseObject(body);
                JSONArray klines = json.getJSONObject("data").getJSONArray("klines");
                // klines 倒数第一条可能是今日盘中，倒数第二条即上一交易日
                for (int i = klines.size() - 1; i >= 1; i--) {
                    String[] parts = klines.getString(i).split(",");
                    if (!parts[0].replace("-", "").equals(today)) {
                        prev += Double.parseDouble(parts[6]);
                        prevDate = parts[0];
                        break;
                    }
                }
            } catch (Exception e) {
                logger.warn("获取昨日成交额失败 secid={}", secid, e);
            }
            sleep(200);
        }
        out.put("prevAmountYi", BigDecimal.valueOf(prev / 1e8));
        out.put("prevDate", prevDate);
        return out;
    }

    /** 涨停/炸板/跌停池（date=today；返回tc总数与pool明细） */
    private JSONObject fetchPool(String urlTpl, boolean needPool) {
        JSONObject out = new JSONObject();
        out.put("count", 0);
        out.put("pool", new JSONArray());
        String date = new SimpleDateFormat("yyyyMMdd").format(new Date());
        try {
            String body = httpGet(String.format(urlTpl, date));
            JSONObject json = JSON.parseObject(body);
            if (json != null && json.getJSONObject("data") != null) {
                out.put("count", json.getJSONObject("data").getIntValue("tc"));
                out.put("qdate", json.getJSONObject("data").getString("qdate"));
                if (needPool) {
                    out.put("pool", json.getJSONObject("data").getJSONArray("pool"));
                }
            }
        } catch (Exception e) {
            logger.warn("获取涨跌停池失败 url={}", urlTpl, e);
        }
        sleep(200);
        return out;
    }

    /** 解析涨停池明细 → 统一股票结构 */
    private List<Map<String, Object>> parseZtStocks(JSONArray pool) {
        List<Map<String, Object>> list = new ArrayList<>();
        if (pool == null) return list;
        for (int i = 0; i < pool.size(); i++) {
            JSONObject d = pool.getJSONObject(i);
            Map<String, Object> s = new LinkedHashMap<>();
            String code = d.getString("c");
            s.put("code", code);
            s.put("name", d.getString("n"));
            s.put("price", d.getIntValue("p") / 1000.0);
            s.put("pct", round2(d.getDoubleValue("zdp")));
            s.put("lianban", d.getIntValue("lbc"));
            s.put("sector", d.getString("hybk"));
            s.put("ltszYi", round1(d.getDoubleValue("ltsz") / 1e8));
            s.put("fundYi", round2(d.getDoubleValue("fund") / 1e8));
            JSONObject zttj = d.getJSONObject("zttj");
            s.put("ztStat", zttj == null ? "" : zttj.getIntValue("days") + "天" + zttj.getIntValue("ct") + "板");
            s.put("beginner", isBeginnerFriendly(code));
            list.add(s);
        }
        list.sort((a, b) -> Integer.compare((int) b.get("lianban"), (int) a.get("lianban")));
        return list;
    }

    /** 连板梯队：按连板数分组 */
    private List<Map<String, Object>> buildLianbanTiers(List<Map<String, Object>> ztList) {
        Map<Integer, List<Map<String, Object>>> byLb = new TreeMap<>(Collections.reverseOrder());
        for (Map<String, Object> s : ztList) {
            byLb.computeIfAbsent((int) s.get("lianban"), k -> new ArrayList<>()).add(s);
        }
        List<Map<String, Object>> tiers = new ArrayList<>();
        for (Map.Entry<Integer, List<Map<String, Object>>> e : byLb.entrySet()) {
            if (e.getKey() < 2 || tiers.size() >= 5) continue; // 展示2板以上的空间梯队
            Map<String, Object> tier = new LinkedHashMap<>();
            tier.put("lianban", e.getKey());
            tier.put("count", e.getValue().size());
            List<String> names = new ArrayList<>();
            for (Map<String, Object> s : e.getValue()) {
                names.add(s.get("name") + "(" + s.get("code") + ")");
            }
            tier.put("stocks", String.join("、", names));
            tiers.add(tier);
        }
        return tiers;
    }

    /** 板块资金流：行业+概念 各取 净流入榜/净流出榜（含涨幅与领涨股） */
    private Map<String, List<Map<String, Object>>> fetchSectors() {
        List<Map<String, Object>> hot = new ArrayList<>();
        List<Map<String, Object>> cold = new ArrayList<>();
        Map<String, List<Map<String, Object>>> out = new HashMap<>();
        out.put("hot", hot);
        out.put("cold", cold);

        // 行业净流入榜（取30）+ 行业净流出榜（取20）+ 概念净流入榜（取30）+ 概念净流出榜（取20）
        String[][] specs = {
                {"m:90+t:2", "1", "30"},  // 行业 净流入
                {"m:90+t:2", "0", "20"},  // 行业 净流出
                {"m:90+t:3", "1", "30"},  // 概念 净流入
                {"m:90+t:3", "0", "20"}   // 概念 净流出
        };
        for (String[] spec : specs) {
            String fs = spec[0];
            boolean desc = "1".equals(spec[1]);
            int pz = Integer.parseInt(spec[2]);
            try {
                String url = String.format(SECTOR_URL_TPL, PUSH2_HOSTS[0], pz, desc ? 1 : 0, desc ? "f62" : "f62", fs);
                String body = httpGetHosts(url);
                if (body == null) {
                    url = String.format(SECTOR_URL_TPL, PUSH2_HOSTS[1], pz, desc ? 1 : 0, desc ? "f62" : "f62", fs);
                    body = httpGetHosts(url);
                }
                JSONObject json = JSON.parseObject(body);
                JSONObject dataObj = json.getJSONObject("data");
                // clist 接口 diff 可能是数字键对象格式 {"0":{...}} 或数组格式，统一转换为 JSONArray
                JSONArray diff = toDiffArray(dataObj.get("diff"));
                if (diff == null) {
                    logger.warn("板块资金流返回结构异常 fs={} body前100字符={}", fs,
                            body.length() > 100 ? body.substring(0, 100) : body);
                    continue;
                }
                String type = fs.contains("t:2") ? "行业" : "概念";
                for (int i = 0; i < diff.size(); i++) {
                    JSONObject d = diff.getJSONObject(i);
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("type", type);
                    m.put("name", d.getString("f14"));
                    m.put("changePct", d.getBigDecimal("f3"));
                    m.put("mainInflowYi", round2(d.getDoubleValue("f62") / 1e8));
                    m.put("mainPct", d.getBigDecimal("f184"));
                    m.put("leaderName", d.getString("f128"));
                    m.put("leaderCode", d.getString("f140"));
                    m.put("leaderPct", d.getBigDecimal("f136"));
                    m.put("beginner", isBeginnerFriendly(d.getString("f140")));
                    (desc ? hot : cold).add(m);
                }
            } catch (Exception e) {
                logger.warn("获取板块资金流失败 fs={}", fs, e);
            }
            sleep(200);
        }
        return out;
    }

    /** 7×24财经快讯（消息面） */
    private List<Map<String, Object>> fetchNews() {
        List<Map<String, Object>> list = new ArrayList<>();
        try {
            String body = httpGet(String.format(NEWS_URL_TPL, System.currentTimeMillis()));
            JSONObject json = JSON.parseObject(body);
            JSONArray arr = json.getJSONObject("data").getJSONArray("fastNewsList");
            for (int i = 0; i < arr.size() && i < 25; i++) {
                JSONObject d = arr.getJSONObject(i);
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("time", d.getString("showTime"));
                String title = d.getString("title");
                m.put("title", title);
                String summary = d.getString("summary");
                m.put("summary", summary != null && summary.length() > 180 ? summary.substring(0, 180) + "…" : summary);
                JSONArray stocks = d.getJSONArray("stockList");
                if (stocks != null && !stocks.isEmpty()) {
                    List<String> codes = new ArrayList<>();
                    for (int j = 0; j < Math.min(stocks.size(), 3); j++) {
                        String raw = stocks.getString(j);
                        String[] seg = raw.split("\\.");
                        if (seg.length == 2 && seg[1].matches("\\d{6}")) {
                            codes.add(seg[1]);
                        }
                    }
                    m.put("relatedCodes", codes);
                }
                list.add(m);
            }
        } catch (Exception e) {
            logger.warn("获取7x24快讯失败", e);
        }
        return list;
    }

    /** 两融余额（T-1） */
    private JSONObject fetchMargin() {
        JSONObject out = new JSONObject();
        out.put("balanceYi", null);
        out.put("netBuyYi", null);
        out.put("date", null);
        try {
            String body = httpGet(MARGIN_URL);
            JSONObject json = JSON.parseObject(body);
            JSONArray data = json.getJSONObject("result").getJSONArray("data");
            if (data != null && !data.isEmpty()) {
                JSONObject d = data.getJSONObject(0);
                out.put("date", d.getString("DIM_DATE") != null ? d.getString("DIM_DATE").substring(0, 10) : null);
                out.put("balanceYi", round1(d.getDoubleValue("RZRQYE") / 1e8));
                out.put("netBuyYi", round1(d.getDoubleValue("RZJME") / 1e8));
            }
        } catch (Exception e) {
            logger.warn("获取两融数据失败", e);
        }
        return out;
    }

    // ==================== 规则分析 ====================

    /** 主线判定：概念(净流入rank×2+涨幅rank) + 行业(净流入+涨停聚集+涨幅投票)，并给出持续性初判 */
    private Map<String, Object> analyzeMainline(Map<String, List<Map<String, Object>>> sectors,
                                                List<Map<String, Object>> ztList, int maxLianban, double zhaBanRate) {
        Map<String, Object> out = new LinkedHashMap<>();
        List<Map<String, Object>> hotSectors = sectors.get("hot");

        // 概念主线评分：仅统计净流入>2亿的概念
        List<Map<String, Object>> conceptHot = new ArrayList<>();
        for (Map<String, Object> s : hotSectors) {
            if ("概念".equals(s.get("type")) && (double) s.get("mainInflowYi") > 2) conceptHot.add(s);
        }
        // 行业净流入TOP3 / 涨幅TOP3
        List<String> topInflowIndustry = new ArrayList<>();
        List<String> topInflowConcept = new ArrayList<>();
        for (Map<String, Object> s : hotSectors) {
            if (topInflowIndustry.size() < 3 && "行业".equals(s.get("type"))) topInflowIndustry.add((String) s.get("name"));
            if (topInflowConcept.size() < 3 && "概念".equals(s.get("type"))) topInflowConcept.add((String) s.get("name"));
        }
        List<Map<String, Object>> industryByPct = new ArrayList<>();
        for (Map<String, Object> s : hotSectors) {
            if ("行业".equals(s.get("type"))) industryByPct.add(s);
        }
        industryByPct.sort((a, b) -> ((BigDecimal) b.get("changePct")).compareTo((BigDecimal) a.get("changePct")));
        List<String> topPctIndustry = new ArrayList<>();
        for (Map<String, Object> s : industryByPct) {
            if (topPctIndustry.size() < 3) topPctIndustry.add((String) s.get("name"));
        }

        // 涨停聚集行业（今日涨停池行业分布）
        Map<String, Integer> ztIndustry = new LinkedHashMap<>();
        for (Map<String, Object> s : ztList) {
            String ind = (String) s.get("sector");
            if (ind != null && !ind.isEmpty()) ztIndustry.merge(ind, 1, Integer::sum);
        }
        List<Map<String, Object>> ztAggr = new ArrayList<>();
        ztIndustry.entrySet().stream()
                .filter(e -> e.getValue() >= 2)
                .sorted((a, b) -> b.getValue() - a.getValue())
                .limit(5)
                .forEach(e -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("sector", e.getKey());
                    m.put("ztCount", e.getValue());
                    ztAggr.add(m);
                });

        String mainLine;
        if (!topInflowConcept.isEmpty()) {
            mainLine = "概念主线：" + String.join("、", topInflowConcept)
                    + (topInflowIndustry.isEmpty() ? "" : "；行业主线：" + String.join("、", topInflowIndustry));
        } else {
            mainLine = "主线暂不清晰，资金呈板块轮动状态";
        }
        out.put("mainLine", mainLine);
        out.put("ztIndustryAggregation", ztAggr);
        out.put("topInflowIndustry", topInflowIndustry);
        out.put("topPctIndustry", topPctIndustry);

        // 持续性规则初判
        List<String> reasons = new ArrayList<>();
        String sustain;
        boolean ztAggrStrong = !ztAggr.isEmpty() && (int) ztAggr.get(0).get("ztCount") >= 5;
        boolean ladderOk = maxLianban >= 3;
        boolean mainTopPct = !topPctIndustry.isEmpty() && !topInflowIndustry.isEmpty()
                && topPctIndustry.get(0).equals(topInflowIndustry.get(0));
        if (ztAggrStrong && ladderOk) {
            sustain = "主线延续概率较高（非一日游）";
            reasons.add("涨停聚集度强：" + ztAggr.get(0).get("sector") + " 单行业涨停 " + ztAggr.get(0).get("ztCount") + " 家");
            reasons.add("连板空间打开：最高 " + maxLianban + " 连板，梯队完整");
            if (zhaBanRate < 20) reasons.add(String.format("炸板率仅 %.0f%%，封板质量高", zhaBanRate));
        } else if (mainTopPct) {
            sustain = "当日新启动方向，警惕一日游";
            reasons.add("资金净流入榜首与涨幅榜首重合，属当日新启动");
            reasons.add("连板梯队未形成（最高" + maxLianban + "板），持续性待验证");
            reasons.add("观察明日承接：若缩量且高标断板则确认一日游");
        } else {
            sustain = "板块轮动市，主线模糊";
            reasons.add("涨停分散、净流入与涨幅榜不重合");
            reasons.add("存量博弈特征明显，快进快出");
        }
        out.put("mainLineSustain", sustain);
        out.put("sustainReasons", reasons);

        // 龙头股：连板龙头(≥2板) + 封单最大的首板 + 板块领涨股
        List<Map<String, Object>> leaders = new ArrayList<>();
        Set<String> seen = new LinkedHashSet<>();
        for (Map<String, Object> s : ztList) {
            if ((int) s.get("lianban") >= 2 && seen.add((String) s.get("code"))) {
                Map<String, Object> l = new LinkedHashMap<>(s);
                l.put("source", "连板龙头");
                leaders.add(l);
                if (leaders.size() >= 10) break;
            }
        }
        for (Map<String, Object> s : ztList) {
            if ((int) s.get("lianban") == 1 && seen.add((String) s.get("code")) && leaders.size() < 13) {
                Map<String, Object> l = new LinkedHashMap<>(s);
                l.put("source", "首板龙头");
                leaders.add(l);
            }
        }
        for (Map<String, Object> s : hotSectors) {
            String code = (String) s.get("leaderCode");
            if (code != null && code.matches("\\d{6}") && seen.add(code) && leaders.size() < 15) {
                Map<String, Object> l = new LinkedHashMap<>();
                l.put("code", code);
                l.put("name", s.get("leaderName"));
                l.put("pct", s.get("leaderPct"));
                l.put("sector", s.get("name"));
                l.put("lianban", 0);
                l.put("source", (String) s.get("type") + "领涨股");
                l.put("beginner", isBeginnerFriendly(code));
                leaders.add(l);
            }
        }
        out.put("leaders", leaders);
        return out;
    }

    /** 市场情绪温度 0-100 */
    private int calcSentiment(int up, int down, double shPct, int zt, int dt,
                              double zhaBanRate, int maxLianban, double mainNetYi, double amountRatioPct) {
        int score = 50;
        double ratio = up * 1.0 / Math.max(down, 1);
        if (ratio >= 3) score += 20; else if (ratio >= 2) score += 15; else if (ratio >= 1.2) score += 8;
        else if (ratio >= 0.8) score += 0; else if (ratio >= 0.5) score -= 8; else if (ratio >= 0.33) score -= 15; else score -= 20;
        if (shPct >= 2) score += 15; else if (shPct >= 1) score += 10; else if (shPct >= 0.3) score += 5;
        else if (shPct <= -2) score -= 15; else if (shPct <= -1) score -= 10; else if (shPct <= -0.3) score -= 5;
        int netZt = zt - dt;
        if (netZt >= 80) score += 15; else if (netZt >= 40) score += 10; else if (netZt >= 10) score += 5;
        else if (netZt <= -10) score -= 10;
        if (zhaBanRate <= 10) score += 10; else if (zhaBanRate <= 20) score += 5;
        else if (zhaBanRate >= 35) score -= 10; else if (zhaBanRate >= 25) score -= 5;
        if (maxLianban >= 5) score += 10; else if (maxLianban >= 4) score += 6; else if (maxLianban >= 3) score += 3;
        else if (maxLianban <= 1) score -= 3;
        if (mainNetYi > 50) score += 15; else if (mainNetYi > 10) score += 8; else if (mainNetYi > 0) score += 3;
        else if (mainNetYi < -50) score -= 15; else if (mainNetYi < -10) score -= 8; else if (mainNetYi < 0) score -= 3;
        // 放量配合：成交额已超昨日全天（临近尾盘才可能触发），放量上攻加分、放量下跌减分
        if (amountRatioPct > 105) score += shPct > 0 ? 5 : -5;
        return Math.max(0, Math.min(100, score));
    }

    private String sentimentLevel(int score) {
        if (score < 20) return "冰点";
        if (score < 40) return "低迷";
        if (score < 60) return "中性";
        if (score < 80) return "活跃";
        return "亢奋";
    }

    private String buildMarketStatus(double shPct, int up, int down, int zt, int zb, double zbRate,
                                     int maxLb, double mainNetYi, double amountYi, String level,
                                     List<Map<String, Object>> hotSectors) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("沪指%.2f%%，两市上涨%d家/下跌%d家，涨停%d家炸板%d家（炸板率%.0f%%），最高%d连板，",
                shPct, up, down, zt, zb, zbRate, maxLb));
        sb.append(String.format("主力净流入%.0f亿，两市成交%.0f亿，", mainNetYi, amountYi));
        sb.append("市场情绪【").append(level).append("】。");
        if (!hotSectors.isEmpty()) {
            sb.append("资金进攻方向：");
            int c = 0;
            for (Map<String, Object> s : hotSectors) {
                if ("行业".equals(s.get("type")) && c < 3) {
                    sb.append(s.get("name")).append(" ");
                    c++;
                }
            }
        }
        return sb.toString();
    }

    // ==================== AI 分析 ====================

    private Map<String, Object> callAiAnalysis(Map<String, Object> realtime) {
        Map<String, Object> aiOut = new LinkedHashMap<>();
        aiOut.put("aiMarketStatus", "");
        aiOut.put("aiLogic", "");
        aiOut.put("aiMoneyFlow", "");
        aiOut.put("aiMainline", "");
        aiOut.put("aiOutlook", "");
        aiOut.put("aiHistoryReview", "");
        try {
            Map<String, Object> aiData = buildAiData(realtime);
            String system = "你是资深A股市场分析专家，擅长从盘面数据解读市场状态、资金意图与题材持续性。"
                    + "你必须基于给定的实时盘面数据与规则初判输出分析，禁止编造数据中不存在的信息。"
                    + "输出严格的JSON（不要markdown代码块包裹），格式：\n"
                    + "{\"ai_market_status\":\"市场在做什么/情绪综合解读(200字内)\","
                    + "\"ai_logic\":\"当前催动市场的核心消息面与驱动逻辑，结合给定快讯(250字内)\","
                    + "\"ai_money_flow\":\"资金从哪流出到哪流入、增量资金是否进场、杠杆资金态度(250字内)\","
                    + "\"ai_mainline\":\"市场主线是什么、正在拉什么弃什么、主线是一日游还是可持续主线，判断依据(300字内)\","
                    + "\"ai_outlook\":\"明日盘前展望：关注什么信号、风险点(200字内)\""
                    + (aiData.containsKey("historyBlock") ? ",\"ai_history_review\":\"结合历史展望vs实际走势复盘，指出过往误判模式与本次需规避的失误(250字内)\"" : "")
                    + "}";
            String user = "今天是" + new SimpleDateFormat("yyyy-MM-dd").format(new Date())
                    + "，实时盘面数据（含竞价/盘中阶段标注）如下：\n"
                    + JSON.toJSONString(aiData);
            String resp = aiCommonUtil.callWithSystem(system, user);
            if (resp != null && !resp.isEmpty()) {
                parseAiJson(resp, aiOut);
            } else {
                aiOut.put("aiMarketStatus", "AI服务暂不可用，请稍后重试或查看规则分析结论。");
            }
        } catch (Exception e) {
            logger.error("AI市场分析失败", e);
            aiOut.put("aiMarketStatus", "AI分析异常：" + e.getMessage());
        }
        return aiOut;
    }

    /** 组装AI输入数据（控制体积：快讯截断、龙头精简、附加历史复盘块） */
    private Map<String, Object> buildAiData(Map<String, Object> realtime) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("阶段", realtime.get("marketPhase"));
        data.put("指数", realtime.get("indexes"));
        data.put("两市成交亿", realtime.get("totalAmountYi"));
        data.put("昨日全天成交亿", realtime.get("prevAmountYi"));
        data.put("量能比%", realtime.get("amountRatioPct"));
        data.put("上涨家数", realtime.get("upCount"));
        data.put("下跌家数", realtime.get("downCount"));
        data.put("涨停数", realtime.get("limitUpCount"));
        data.put("跌停数", realtime.get("limitDownCount"));
        data.put("炸板数", realtime.get("zhaBanCount"));
        data.put("炸板率", realtime.get("zhaBanRate"));
        data.put("最高连板", realtime.get("maxLianban"));
        data.put("连板梯队", realtime.get("lianbanTiers"));
        data.put("主力净流入亿", realtime.get("mainNetInflowYi"));
        data.put("超大单亿", realtime.get("ultraNetInflowYi"));
        data.put("大单亿", realtime.get("bigNetInflowYi"));
        data.put("中单亿", realtime.get("midNetInflowYi"));
        data.put("小单亿", realtime.get("smallNetInflowYi"));
        data.put("两融余额亿", realtime.get("marginBalanceYi"));
        data.put("融资净买入亿", realtime.get("marginNetBuyYi"));
        data.put("两融日期", realtime.get("marginDate"));
        data.put("北向说明", "实时净买入已停止披露");
        data.put("主拉板块TOP10", realtime.get("hotSectors"));
        data.put("主弃板块TOP8", realtime.get("coldSectors"));
        data.put("规则主线初判", realtime.get("mainLine"));
        data.put("规则持续性初判", realtime.get("mainLineSustain") + "；依据：" + realtime.get("sustainReasons"));
        data.put("涨停行业聚集", realtime.get("ztIndustryAggregation"));
        data.put("情绪评分", realtime.get("sentimentScore") + "(" + realtime.get("sentimentLevel") + ")");
        data.put("规则结论", realtime.get("marketStatus"));

        // 龙头股精简
        List<Map<String, Object>> leaders = (List<Map<String, Object>>) realtime.get("leaders");
        List<Map<String, Object>> simpleLeaders = new ArrayList<>();
        if (leaders != null) {
            for (Map<String, Object> l : leaders) {
                Map<String, Object> sl = new LinkedHashMap<>();
                sl.put("代码", l.get("code"));
                sl.put("名称", l.get("name"));
                sl.put("涨幅", l.get("pct"));
                sl.put("连板", l.get("lianban"));
                sl.put("身份", l.get("source"));
                sl.put("板块", l.get("sector"));
                sl.put("新手可买", l.get("beginner"));
                simpleLeaders.add(sl);
            }
        }
        data.put("龙头股", simpleLeaders);

        // 快讯精简（只保留标题+时间）
        List<Map<String, Object>> news = (List<Map<String, Object>>) realtime.get("news");
        List<String> newsLines = new ArrayList<>();
        if (news != null) {
            for (Map<String, Object> n : news) {
                newsLines.add(n.get("time") + " " + n.get("title"));
            }
        }
        data.put("7x24快讯", newsLines);

        // 历史复盘块：昨日AI展望 + 今日实际走势
        try {
            List<MarketAnalysisDaily> rows = marketAnalysisMapper.selectRecent(4);
            if (rows != null && rows.size() >= 2) {
                MarketAnalysisDaily prev = rows.get(0); // 最近一次（可能是昨日或更早）
                if (prev.getAiOutlook() != null && !prev.getAiOutlook().trim().isEmpty()
                        && !fmtDay(prev.getMarketDate()).equals(fmtDay(new Date()))) {
                    Map<String, Object> hb = new LinkedHashMap<>();
                    hb.put("上次分析日", fmtDay(prev.getMarketDate()));
                    hb.put("上次AI展望", trunc(prev.getAiOutlook(), 400));
                    hb.put("上次情绪", prev.getSentimentScore() + "(" + prev.getSentimentLevel() + ")");
                    hb.put("今日实际", String.format("上证%.2f%%，涨停%d跌停%d，主力净流入%.1f亿，最高%d连板，主线：%s",
                            val(realtime.get("shChangePct")),
                            (int) realtime.get("limitUpCount"), (int) realtime.get("limitDownCount"),
                            (double) realtime.get("mainNetInflowYi"), (int) realtime.get("maxLianban"),
                            realtime.get("mainLine")));
                    data.put("历史复盘素材", hb);
                }
            }
        } catch (Exception e) {
            logger.warn("组装历史复盘块失败", e);
        }
        return data;
    }

    /** 解析AI返回JSON（容错：剥离代码围栏、截取大括号区间），失败则整体放入市场状态字段 */
    private void parseAiJson(String resp, Map<String, Object> aiOut) {
        String text = resp.trim();
        text = text.replace("```json", "").replace("```", "").trim();
        int s = text.indexOf('{');
        int e = text.lastIndexOf('}');
        if (s >= 0 && e > s) {
            try {
                JSONObject json = JSON.parseObject(text.substring(s, e + 1));
                aiOut.put("aiMarketStatus", str(json.getString("ai_market_status")));
                aiOut.put("aiLogic", str(json.getString("ai_logic")));
                aiOut.put("aiMoneyFlow", str(json.getString("ai_money_flow")));
                aiOut.put("aiMainline", str(json.getString("ai_mainline")));
                aiOut.put("aiOutlook", str(json.getString("ai_outlook")));
                aiOut.put("aiHistoryReview", str(json.getString("ai_history_review")));
                return;
            } catch (Exception ex) {
                logger.warn("AI输出JSON解析失败，回退为全文展示", ex);
            }
        }
        aiOut.put("aiMarketStatus", text);
    }

    // ==================== 落库 ====================

    private boolean persistAnalysis(Map<String, Object> realtime, Map<String, Object> aiResult) {
        try {
            ensureTable();
            SimpleDateFormat dayFmt = new SimpleDateFormat("yyyy-MM-dd");
            Date today = new Date();
            MarketAnalysisDaily record = new MarketAnalysisDaily();
            record.setMarketDate(today);
            record.setAnalysisTime(today);
            record.setMarketPhase((String) realtime.get("marketPhase"));
            record.setMarketStatus(trunc((String) realtime.get("marketStatus"), 480));
            record.setSentimentScore((Integer) realtime.get("sentimentScore"));
            record.setSentimentLevel((String) realtime.get("sentimentLevel"));
            JSONArray indexes = (JSONArray) realtime.get("indexes");
            if (indexes != null) {
                for (int i = 0; i < indexes.size(); i++) {
                    JSONObject idx = indexes.getJSONObject(i);
                    String name = idx.getString("name");
                    if ("上证指数".equals(name)) {
                        record.setShClose(idx.getBigDecimal("close"));
                        record.setShChangePct(idx.getBigDecimal("changePct"));
                    } else if ("深证成指".equals(name)) {
                        record.setSzClose(idx.getBigDecimal("close"));
                        record.setSzChangePct(idx.getBigDecimal("changePct"));
                    } else if ("创业板指".equals(name)) {
                        record.setCybClose(idx.getBigDecimal("close"));
                        record.setCybChangePct(idx.getBigDecimal("changePct"));
                    } else if ("科创50".equals(name)) {
                        record.setKc50Close(idx.getBigDecimal("close"));
                        record.setKc50ChangePct(idx.getBigDecimal("changePct"));
                    }
                }
            }
            record.setTotalAmount(bd(realtime.get("totalAmountYi")).multiply(new BigDecimal("100000000")));
            record.setPrevTotalAmount(bd(realtime.get("prevAmountYi")).multiply(new BigDecimal("100000000")));
            record.setAmountRatio(bd(realtime.get("amountRatioPct")));
            record.setUpCount((Integer) realtime.get("upCount"));
            record.setDownCount((Integer) realtime.get("downCount"));
            record.setFlatCount((Integer) realtime.get("flatCount"));
            record.setLimitUpCount((Integer) realtime.get("limitUpCount"));
            record.setLimitDownCount((Integer) realtime.get("limitDownCount"));
            record.setZhaBanCount((Integer) realtime.get("zhaBanCount"));
            record.setZhaBanRate(bd(realtime.get("zhaBanRate")));
            record.setMaxLianban((Integer) realtime.get("maxLianban"));
            record.setLianbanStocks(JSON.toJSONString(realtime.get("lianbanTiers")));
            record.setMainNetInflow(bd(realtime.get("mainNetInflowYi")).multiply(new BigDecimal("100000000")));
            record.setUltraNetInflow(bd(realtime.get("ultraNetInflowYi")).multiply(new BigDecimal("100000000")));
            record.setBigNetInflow(bd(realtime.get("bigNetInflowYi")).multiply(new BigDecimal("100000000")));
            record.setMidNetInflow(bd(realtime.get("midNetInflowYi")).multiply(new BigDecimal("100000000")));
            record.setSmallNetInflow(bd(realtime.get("smallNetInflowYi")).multiply(new BigDecimal("100000000")));
            record.setMarginBalance(bd(realtime.get("marginBalanceYi")).multiply(new BigDecimal("100000000")));
            record.setMarginNetBuy(bd(realtime.get("marginNetBuyYi")).multiply(new BigDecimal("100000000")));
            record.setHotSectors(JSON.toJSONString(realtime.get("hotSectors")));
            record.setColdSectors(JSON.toJSONString(realtime.get("coldSectors")));
            record.setMainLine(trunc((String) realtime.get("mainLine"), 990));
            record.setMainLineSustainability(trunc((String) realtime.get("mainLineSustain") + "；依据："
                    + realtime.get("sustainReasons"), 490));
            record.setLeaders(JSON.toJSONString(realtime.get("leaders")));
            record.setNewsHighlights(JSON.toJSONString(realtime.get("news")));
            record.setAiMarketStatus((String) aiResult.get("aiMarketStatus"));
            record.setAiLogic((String) aiResult.get("aiLogic"));
            record.setAiMoneyFlow((String) aiResult.get("aiMoneyFlow"));
            record.setAiMainline((String) aiResult.get("aiMainline"));
            record.setAiOutlook((String) aiResult.get("aiOutlook"));
            record.setAiHistoryReview((String) aiResult.get("aiHistoryReview"));
            // realtime_json：全量快照（去除新闻summary控制体积）
            Map<String, Object> snapshot = new LinkedHashMap<>(realtime);
            snapshot.remove("history");
            record.setRealtimeJson(trunc(JSON.toJSONString(snapshot), 400000));

            marketAnalysisMapper.deleteByMarketDate(today);
            marketAnalysisMapper.insertAnalysis(record);
            logger.info("市场分析已落库 marketDate={} 情绪={} 主线={}", dayFmt.format(today),
                    record.getSentimentScore(), record.getMainLine());
            return true;
        } catch (Exception e) {
            logger.error("市场分析落库失败", e);
            return false;
        }
    }

    private void ensureTable() {
        marketAnalysisMapper.createTableIfNotExists();
    }

    // ==================== 工具方法 ====================

    private String buildFingerprint(Map<String, Object> realtime) {
        return String.format("%s|%s|%.0f|%.0f|%s",
                realtime.get("marketPhase"), realtime.get("limitUpCount"),
                bd(realtime.get("totalAmountYi")).doubleValue(),
                bd(realtime.get("mainNetInflowYi")).doubleValue(),
                realtime.get("sentimentScore"));
    }

    private String phaseNote(String phase, double amountRatioPct) {
        switch (phase) {
            case "集合竞价":
            case "竞价撮合":
                return "当前处于集合竞价阶段（9:15-9:25），指数与个股价格为竞价撮合数据，可提前感知当日资金态度";
            case "早盘":
                return String.format("早盘阶段，成交额已达昨日全天的%.0f%%", amountRatioPct);
            case "盘中":
                return String.format("盘中实时行情，成交额已达昨日全天的%.0f%%", amountRatioPct);
            case "尾盘":
                return String.format("尾盘阶段，成交额已达昨日全天的%.0f%%", amountRatioPct);
            case "已收盘":
                return "已收盘，展示当日全天收盘统计";
            default:
                return "非交易时段，展示最近一个交易日的收盘统计";
        }
    }

    private String detectPhase() {
        Calendar cal = Calendar.getInstance();
        cal.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        int dow = cal.get(Calendar.DAY_OF_WEEK);
        if (dow == Calendar.SATURDAY || dow == Calendar.SUNDAY) return "非交易时段";
        int hm = cal.get(Calendar.HOUR_OF_DAY) * 100 + cal.get(Calendar.MINUTE);
        if (hm < 915) return "盘前";
        if (hm < 925) return "集合竞价";
        if (hm < 930) return "竞价撮合";
        if (hm < 1130) return "早盘";
        if (hm < 1300) return "午间休市";
        if (hm < 1450) return "盘中";
        if (hm < 1500) return "尾盘";
        return "已收盘";
    }

    private boolean isBeginnerFriendly(String code) {
        return code != null && (code.startsWith("00") || code.startsWith("60"));
    }

    /**
     * 带超时与重试的GET请求：东财接口偶发拦截（空响应/断连），最多3次，间隔递增500ms/1000ms
     */
    private static String httpGet(String url) {
        for (int attempt = 1; attempt <= 3; attempt++) {
            try (CloseableHttpClient client = HttpClients.createDefault()) {
                HttpGet request = new HttpGet(url);
                request.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)");
                request.setHeader("Referer", "https://quote.eastmoney.com/");
                request.setConfig(RequestConfig.custom()
                        .setConnectTimeout(10000).setSocketTimeout(15000).build());
                try (CloseableHttpResponse resp = client.execute(request)) {
                    if (resp.getStatusLine().getStatusCode() == 200) {
                        String body = EntityUtils.toString(resp.getEntity(), "UTF-8");
                        if (body != null && !body.trim().isEmpty()) return body;
                    }
                }
            } catch (Exception e) {
                logger.warn("HTTP请求失败 attempt={} url={}", attempt, url, e);
            }
            if (attempt < 3) {
                try {
                    Thread.sleep(attempt * 500L);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }
        }
        return null;
    }

    /** 指定host请求（供failover）：仅尝试1次，避免多域名×多次数放大请求量 */
    private static String httpGetHosts(String url) {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(url);
            request.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)");
            request.setHeader("Referer", "https://quote.eastmoney.com/");
            request.setConfig(RequestConfig.custom()
                    .setConnectTimeout(10000).setSocketTimeout(15000).build());
            try (CloseableHttpResponse resp = client.execute(request)) {
                if (resp.getStatusLine().getStatusCode() == 200) {
                    String body = EntityUtils.toString(resp.getEntity(), "UTF-8");
                    if (body != null && !body.trim().isEmpty()) return body;
                }
            }
        } catch (Exception e) {
            logger.warn("HTTP请求失败 url={}", url, e);
        }
        return null;
    }

    private static void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * clist 接口 diff 字段可能是数组格式，也可能是数字键对象格式 {"0":{...},"1":{...}}，
     * 统一转换为 JSONArray，异常结构返回 null。
     */
    private static JSONArray toDiffArray(Object diffObj) {
        if (diffObj instanceof JSONArray) {
            return (JSONArray) diffObj;
        }
        if (diffObj instanceof JSONObject) {
            JSONObject jo = (JSONObject) diffObj;
            JSONArray arr = new JSONArray();
            // 按数字键顺序取出，保证榜单排序不变
            List<String> keys = new ArrayList<>(jo.keySet());
            keys.sort(Comparator.comparingInt(k -> {
                try {
                    return Integer.parseInt(k);
                } catch (NumberFormatException e) {
                    return Integer.MAX_VALUE;
                }
            }));
            for (String key : keys) {
                JSONObject item = jo.getJSONObject(key);
                if (item != null) {
                    arr.add(item);
                }
            }
            return arr;
        }
        return null;
    }

    private static double round2(double v) {
        return BigDecimal.valueOf(v).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    private static double round1(double v) {
        return BigDecimal.valueOf(v).setScale(1, RoundingMode.HALF_UP).doubleValue();
    }

    private static BigDecimal bd(Object v) {
        if (v == null) return BigDecimal.ZERO;
        if (v instanceof BigDecimal) return (BigDecimal) v;
        try {
            return new BigDecimal(String.valueOf(v));
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    private static String str(String s) {
        return s == null ? "" : s;
    }

    private static int nvl(Integer v) {
        return v == null ? 0 : v;
    }

    private static double val(Object v) {
        if (v instanceof Number) return ((Number) v).doubleValue();
        if (v instanceof BigDecimal) return ((BigDecimal) v).doubleValue();
        return 0;
    }

    private static double yi(BigDecimal v) {
        return v == null ? 0 : v.divide(new BigDecimal("100000000"), 1, RoundingMode.HALF_UP).doubleValue();
    }

    private static String fmtDay(Date d) {
        return d == null ? "" : new SimpleDateFormat("yyyy-MM-dd").format(d);
    }

    private static String trunc(String s, int max) {
        if (s == null) return null;
        return s.length() > max ? s.substring(0, max) + "…" : s;
    }
}
