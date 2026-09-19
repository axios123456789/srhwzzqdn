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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

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
            // clist熔断期直接跳过（指数该轮缺省），防拉黑期间反复请求延长封禁
            String body = isPush2ClistBlocked() ? null : httpGetHosts(String.format(INDEX_URL_TPL, PUSH2_HOSTS[0]));
            if (body == null && !isPush2ClistBlocked()) {
                body = httpGetHosts(String.format(INDEX_URL_TPL, PUSH2_HOSTS[1]));
                if (body == null) markPush2ClistFail();
            }
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

    /** 昨日两市全天成交额（沪+深指数日线，量能对比基准）；push2his熔断期直接跳过东财，用市场分析表最近交易日记录兜底 */
    private JSONObject fetchPrevAmount() {
        JSONObject out = new JSONObject();
        double prev = 0; // 单位：元
        String prevDate = null;
        if (isPush2hisBlocked()) {
            logger.info("push2his K线熔断中，昨日成交额改用市场分析表兜底");
        } else {
            String today = new SimpleDateFormat("yyyyMMdd").format(new Date());
            for (String secid : new String[]{"1.000001", "0.399001"}) {
                String body = httpGetNoRetry(String.format(KLINE_URL, secid));
                if (body == null) {
                    // 首次失败即熔断（push2his拉黑特征），剩余指数不再请求，转表兜底
                    markPush2hisFail();
                    break;
                }
                try {
                    JSONArray klines = JSON.parseObject(body).getJSONObject("data").getJSONArray("klines");
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
                    logger.warn("解析昨日成交额K线失败 secid={}", secid);
                }
                sleep(200);
            }
        }
        if (prev <= 0) {
            // 兜底：市场分析表最近一个交易日记录的两市成交额（表内单位=元）
            try {
                SimpleDateFormat dayKey = new SimpleDateFormat("yyyy-MM-dd");
                String todayKey = dayKey.format(new Date());
                List<MarketAnalysisDaily> recent = marketAnalysisMapper.selectRecent(3);
                if (recent != null) {
                    for (MarketAnalysisDaily r : recent) {
                        if (r.getMarketDate() != null && r.getTotalAmount() != null
                                && dayKey.format(r.getMarketDate()).compareTo(todayKey) < 0) {
                            prev = r.getTotalAmount().doubleValue();
                            prevDate = dayKey.format(r.getMarketDate());
                            break;
                        }
                    }
                }
            } catch (Exception e) {
                logger.warn("昨日成交额表兜底失败：{}", e.getMessage());
            }
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

    /** 连板梯队：按连板数分组，2板以上全部档位、每档全部个股结构化返回（前端流式铺满展示，不做截断） */
    private List<Map<String, Object>> buildLianbanTiers(List<Map<String, Object>> ztList) {
        Map<Integer, List<Map<String, Object>>> byLb = new TreeMap<>(Collections.reverseOrder());
        for (Map<String, Object> s : ztList) {
            byLb.computeIfAbsent((int) s.get("lianban"), k -> new ArrayList<>()).add(s);
        }
        List<Map<String, Object>> tiers = new ArrayList<>();
        for (Map.Entry<Integer, List<Map<String, Object>>> e : byLb.entrySet()) {
            if (e.getKey() < 2) continue; // 仅展示2板以上的空间梯队
            List<Map<String, Object>> stocks = new ArrayList<>();
            for (Map<String, Object> s : e.getValue()) {
                Map<String, Object> st = new LinkedHashMap<>();
                st.put("code", s.get("code"));
                st.put("name", s.get("name"));
                st.put("pct", s.get("pct"));
                st.put("price", s.get("price"));
                st.put("hybk", s.get("hybk"));
                st.put("ztStat", s.get("ztStat"));
                Object fund = s.get("fund");
                st.put("fund", fund == null ? null : String.format("%.1f", ((Number) fund).doubleValue() / 1e8));
                stocks.add(st);
            }
            Map<String, Object> tier = new LinkedHashMap<>();
            tier.put("lianban", e.getKey());
            tier.put("count", stocks.size());
            tier.put("stocks", stocks);
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
                // clist熔断期直接跳过（板块列表该轮缺省），防拉黑期间反复请求延长封禁
                String body = isPush2ClistBlocked() ? null : httpGetHosts(url);
                if (body == null && !isPush2ClistBlocked()) {
                    url = String.format(SECTOR_URL_TPL, PUSH2_HOSTS[1], pz, desc ? 1 : 0, desc ? "f62" : "f62", fs);
                    body = httpGetHosts(url);
                    if (body == null) markPush2ClistFail();
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

    // ==================== 中期市场研判（近10~30天放大视角） ====================

    // 研判当日缓存：特征构建涉及指数K线+两融历史+约40个板块30日K线（约15秒），当日30分钟内复用
    private static final long CYCLE_CACHE_TTL_MS = 30 * 60 * 1000L;
    // 降级结果（东财K线被熔断，走了腾讯兜底/板块特征缺失）只缓存5分钟，封禁过期后自动恢复完整数据
    private static final long CYCLE_DEGRADED_TTL_MS = 5 * 60 * 1000L;
    private final Map<String, Object> cycleCacheHolder = new ConcurrentHashMap<>();
    // AI策略推荐缓存：key=当日日期|涨停数指纹，当日数据小变时不重复调用大模型
    private final Map<String, Map<String, Object>> cycleAiCache = new ConcurrentHashMap<>();

    // push2his K线熔断（同个股K线熔断机制：高频请求触发临时拉黑，TCP通但HTTP静默丢响应，NoHttpResponseException即拉黑特征）：
    // 首次失败立即熔断5分钟，期间所有push2his K线类请求直接跳过（指数走腾讯兜底、板块K线跳过、昨日成交额用表数据兜底），
    // 避免拉黑期间重试延长封禁+日志刷屏；封禁过期后下一次构建自动拿到完整数据
    private static final long PUSH2HIS_BREAK_MS = 5 * 60 * 1000L;
    private static volatile long push2hisBlockedUntil = 0L;

    private static boolean isPush2hisBlocked() {
        return System.currentTimeMillis() < push2hisBlockedUntil;
    }

    private static void markPush2hisFail() {
        push2hisBlockedUntil = System.currentTimeMillis() + PUSH2HIS_BREAK_MS;
        logger.warn("东财push2his K线请求失败，首次即熔断5分钟：期间K线类请求全部跳过，改用腾讯兜底/表数据降级");
    }

    // push2/push2delay 行情集群clist熔断（clist高频分页会触发该集群临时拉黑，与push2his独立）：
    // 首次失败熔断3分钟，期间clist类请求直接跳过（板块池返回空=研判降级，实时分析板块/指数该轮缺省）
    private static final long PUSH2_CLIST_BREAK_MS = 3 * 60 * 1000L;
    private static volatile long push2ClistBlockedUntil = 0L;

    private static boolean isPush2ClistBlocked() {
        return System.currentTimeMillis() < push2ClistBlockedUntil;
    }

    private static void markPush2ClistFail() {
        push2ClistBlockedUntil = System.currentTimeMillis() + PUSH2_CLIST_BREAK_MS;
        logger.warn("东财行情集群clist请求失败，首次即熔断3分钟：期间clist类请求跳过防延长封禁");
    }

    /** 研判特征板块池：按当日成交额取活跃行业板块30个 + 活跃概念板块10个（控制push2his请求量防拉黑） */
    private static final String CYCLE_SECTOR_INDUSTRY_URL_TPL =
            "%s/api/qt/clist/get?fltt=2&pn=1&pz=30&po=1&fid=f6&fs=m:90+t:2&fields=f12,f14,f3,f6";
    private static final String CYCLE_SECTOR_CONCEPT_URL_TPL =
            "%s/api/qt/clist/get?fltt=2&pn=1&pz=10&po=1&fid=f6&fs=m:90+t:3&fields=f12,f14,f3,f6";
    private static final String CYCLE_KLINE_URL_TPL =
            "http://push2his.eastmoney.com/api/qt/stock/kline/get?secid=%s&klt=101&fqt=1&lmt=30&end=20500101"
                    + "&fields1=f1,f2,f3&fields2=f51,f52,f53,f54,f55,f56,f57";
    /** 腾讯K线兜底（指数用，口径与个股K线兜底一致） */
    private static final String CYCLE_TX_KLINE_URL = "https://web.ifzq.gtimg.cn/appstock/app/fqkline/get";

    @Override
    public Map<String, Object> getMarketCycleAnalysis() {
        String today = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        Map<String, Object> cached = (Map<String, Object>) cycleCacheHolder.get("result");
        Long cachedDay = (Long) cycleCacheHolder.get("time");
        String cachedDate = (String) cycleCacheHolder.get("date");
        if (cached != null && today.equals(cachedDate) && cachedDay != null) {
            // 降级结果（东财K线被熔断）只缓存5分钟，封禁过期后自动重建完整数据
            long ttl = Boolean.TRUE.equals(cached.get("klineDegraded")) ? CYCLE_DEGRADED_TTL_MS : CYCLE_CACHE_TTL_MS;
            if (System.currentTimeMillis() - cachedDay < ttl) {
                return cached;
            }
        }
        Map<String, Object> result = buildCycleAnalysis();
        cycleCacheHolder.put("result", result);
        cycleCacheHolder.put("time", System.currentTimeMillis());
        cycleCacheHolder.put("date", today);
        return result;
    }

    @Override
    public Map<String, Object> analyzeMarketCycleWithAi() {
        Map<String, Object> cycle = getMarketCycleAnalysis();
        Map<String, Object> result = new LinkedHashMap<>(cycle);

        // 资金异动候选池提前拉取：其完整性参与指纹，避免"数据缺失期间生成的空仓结论"被缓存固化
        List<Map<String, Object>> candidates = fetchCycleCandidates();
        int sfSize = cycle.get("sectorFeatures") instanceof List ? ((List<?>) cycle.get("sectorFeatures")).size() : 0;

        // 指纹缓存：当日+涨停数+主力净流入+候选池/轮动明细完整性，数据小变不重复调用大模型；
        // 数据从缺失恢复为可用时指纹变化，自动重新分析覆盖旧的降级结论
        Map<String, Object> realtime;
        try {
            realtime = getRealtimeAnalysis();
        } catch (Exception e) {
            realtime = Collections.emptyMap();
        }
        String fingerprint = String.format("cycle|%s|%s|%s|cand%d|sf%d",
                new SimpleDateFormat("yyyy-MM-dd").format(new Date()),
                realtime.get("limitUpCount"), realtime.get("mainNetInflowYi"),
                candidates.size(), sfSize);
        Map<String, Object> cachedAi = cycleAiCache.get(fingerprint);
        if (cachedAi != null && System.currentTimeMillis() - (long) cachedAi.get("_cacheTime") < AI_CACHE_TTL_MS) {
            result.put("ai", cachedAi.get("ai"));
            return result;
        }

        Map<String, Object> ai = callCycleAi(cycle, realtime, candidates);
        result.put("ai", ai);
        Map<String, Object> cacheEntry = new HashMap<>(ai);
        cacheEntry.put("_cacheTime", System.currentTimeMillis());
        cycleAiCache.clear();
        cycleAiCache.put(fingerprint, cacheEntry);
        return result;
    }

    /** 特征构建：指数30日K线 + 两融30天历史 + 活跃板块30日K线 + 表内已有分析记录 */
    private Map<String, Object> buildCycleAnalysis() {
        Map<String, Object> out = new LinkedHashMap<>();
        boolean klineDegraded = false;
        try {
            // 1. 指数30日K线（上证=趋势主锚，创业板=弹性风向标），东财熔断时走腾讯兜底；上证原始行复用给每日特征表（省1次K线请求）
            Map<String, List<double[]>> indexK = new LinkedHashMap<>(); // key -> [pct, amount, close]
            List<String[]> shRaw = fetchIndexKlineRaw("1.000001", "sh000001");
            List<double[]> shRows = parseIndexRows(shRaw);
            Thread.sleep(200);
            List<double[]> cybRows = parseIndexRows(fetchIndexKlineRaw("0.399006", "sz399006"));
            Thread.sleep(200);
            if (shRows.isEmpty() || cybRows.isEmpty()) klineDegraded = true;
            indexK.put("上证指数", shRows);
            indexK.put("创业板指", cybRows);
            // 2. 两融30天
            List<Map<String, Object>> marginRows = fetchMarginHistory(30);
            // 3. 活跃板块池（行业30+概念10）30日K线（东财熔断时跳过，研判降级）
            List<Map<String, Object>> sectorFeatures = fetchCycleSectorFeatures();
            if (sectorFeatures.size() < 10) klineDegraded = true;
            // 4. 表内已有分析记录（直接用）
            List<MarketAnalysisDaily> tableRows = marketAnalysisMapper.selectRecent(30);

            // 5. 算法研判
            Map<String, Object> cycle = analyzeCycle(indexK, marginRows, sectorFeatures, tableRows);
            out.putAll(cycle);
            out.put("dailyFeatures", buildDailyFeatures(shRaw, marginRows, sectorFeatures, tableRows));
            out.put("sectorFeatures", buildSectorFeaturesForAi(sectorFeatures)); // 近10日板块轮动明细（AI预测轮动回踩用）
            out.put("klineDegraded", klineDegraded);
        } catch (Exception e) {
            logger.error("中期市场研判构建失败", e);
            out.put("cycleType", "研判失败");
            out.put("reasons", Collections.singletonList("数据构建异常：" + e.getMessage()));
            out.put("klineDegraded", true);
        }
        return out;
    }

    /** 指数30日K线原始行（旧→新，行=date,open,close,high,low,vol(,amount)）：东财优先（熔断期跳过），失败退腾讯兜底 */
    private List<String[]> fetchIndexKlineRaw(String secid, String txCode) throws Exception {
        if (!isPush2hisBlocked()) {
            List<String[]> rows = new ArrayList<>();
            boolean ok = false;
            String body = httpGetNoRetry(String.format(CYCLE_KLINE_URL_TPL, secid));
            if (body != null) {
                JSONObject data = JSON.parseObject(body).getJSONObject("data");
                JSONArray klines = data == null ? null : data.getJSONArray("klines");
                if (klines != null && !klines.isEmpty()) {
                    for (int i = 0; i < klines.size(); i++) rows.add(((String) klines.get(i)).split(","));
                    ok = true;
                }
            }
            if (ok) {
                return rows;
            }
            markPush2hisFail(); // 首次失败即熔断，本轮后续K线请求全部跳过
        }
        return fetchIndexKlineRawFromTencent(txCode);
    }

    /** 腾讯指数K线兜底（口径与个股K线兜底一致）：行无成交额（第7列补空），仅能算涨跌幅不能算量能比 */
    private List<String[]> fetchIndexKlineRawFromTencent(String txCode) {
        List<String[]> rows = new ArrayList<>();
        String body = httpGetNoRetry(CYCLE_TX_KLINE_URL + "?param=" + txCode + ",day,,,40,qfq");
        if (body == null) return rows;
        try {
            JSONObject json = JSON.parseObject(body);
            if (json.getIntValue("code") != 0) return rows;
            JSONObject data = json.getJSONObject("data");
            JSONObject stock = data == null ? null : data.getJSONObject(txCode);
            if (stock == null) return rows;
            JSONArray klines = stock.getJSONArray("qfqday");
            if (klines == null) klines = stock.getJSONArray("day");
            if (klines == null) return rows;
            int from = Math.max(0, klines.size() - 30);
            for (int i = from; i < klines.size(); i++) {
                JSONArray r = klines.getJSONArray(i);
                String[] row = new String[7];
                for (int j = 0; j < 6 && j < r.size(); j++) row[j] = r.getString(j);
                row[6] = r.size() > 6 ? r.getString(6) : "";
                rows.add(row);
            }
        } catch (Exception e) {
            logger.warn("腾讯指数K线兜底失败 txCode={}：{}", txCode, e.getMessage());
        }
        return rows;
    }

    /** 指数30日K线 → [涨跌幅, 成交额亿, 收盘价] 旧→新（腾讯兜底行无成交额→amount=0，量能比自动降级） */
    private List<double[]> parseIndexRows(List<String[]> raw) {
        List<double[]> rows = new ArrayList<>();
        for (int i = 0; i < raw.size(); i++) {
            String[] p = raw.get(i);
            double close = Double.parseDouble(p[2]);
            double prevClose = i > 0 ? Double.parseDouble(raw.get(i - 1)[2]) : close;
            double pct = prevClose > 0 ? (close - prevClose) / prevClose * 100 : 0;
            double amountYi = p.length > 6 && p[6] != null && !p[6].isEmpty() ? Double.parseDouble(p[6]) / 1e8 : 0;
            rows.add(new double[]{pct, amountYi, close});
        }
        return rows;
    }

    /** 两融历史（T-1起往前n天，含融资余额与融资净买入，单位元） */
    private List<Map<String, Object>> fetchMarginHistory(int n) throws Exception {
        String url = "https://datacenter-web.eastmoney.com/api/data/v1/get?reportName=RPTA_RZRQ_LSHJ&columns=ALL"
                + "&source=WEB&sortColumns=dim_date&sortTypes=-1&pageSize=" + n + "&pageNumber=1";
        List<Map<String, Object>> rows = new ArrayList<>();
        String body = httpGet(url);
        if (body == null) return rows;
        JSONObject data = JSON.parseObject(body).getJSONObject("result");
        if (data == null) return rows;
        JSONArray arr = data.getJSONArray("data");
        if (arr == null) return rows;
        for (int i = arr.size() - 1; i >= 0; i--) { // 旧→新
            JSONObject d = arr.getJSONObject(i);
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("date", d.getString("DIM_DATE") == null ? "" : d.getString("DIM_DATE").substring(0, 10));
            m.put("rzrqYi", d.getDoubleValue("RZRQYE") / 1e8);
            m.put("rzNetBuyYi", d.getDoubleValue("RZJME") / 1e8);
            rows.add(m);
        }
        return rows;
    }

    /** 活跃板块（行业30+概念10，按成交额）近30日K线特征；3线程并行限速（参考价值选股已验证并发度），push2his首次失败即熔断并终止剩余请求 */
    private List<Map<String, Object>> fetchCycleSectorFeatures() throws Exception {
        List<Map<String, Object>> pool = fetchCycleSectorPool();
        if (pool.isEmpty()) return new ArrayList<>();
        // 每板块30日K线 → 5/10/20日累计涨幅 + 每日涨幅序列；3线程并行（线程内串行+250ms间隔），熔断后剩余板块全部快速跳过
        List<Map<String, Object>> features = Collections.synchronizedList(new ArrayList<>());
        int threads = Math.min(3, pool.size());
        int step = (pool.size() + threads - 1) / threads;
        ExecutorService exec = Executors.newFixedThreadPool(threads);
        List<Future<?>> futures = new ArrayList<>();
        for (int t = 0; t < threads; t++) {
            final List<Map<String, Object>> part =
                    pool.subList(t * step, Math.min(pool.size(), (t + 1) * step));
            futures.add(exec.submit(() -> {
                for (Map<String, Object> s : part) {
                    if (isPush2hisBlocked()) break; // 熔断后剩余板块不再发请求（防延长封禁）
                    if (fetchOneSectorKline(s)) {
                        features.add(s);
                    }
                    try {
                        Thread.sleep(250);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }));
        }
        for (Future<?> f : futures) {
            try {
                f.get(90, TimeUnit.SECONDS);
            } catch (Exception e) {
                logger.warn("板块K线并行任务异常", e);
            }
        }
        exec.shutdown();
        return features;
    }

    /** 单板块30日K线特征；返回是否成功（数据解析异常当跳过不计熔断，网络失败返回false计一次熔断） */
    private boolean fetchOneSectorKline(Map<String, Object> s) {
        String secid = "90." + s.get("code");
        try {
            String body = httpGetNoRetry(String.format(CYCLE_KLINE_URL_TPL, secid));
            if (body == null) {
                markPush2hisFail();
                return false;
            }
            JSONObject data = JSON.parseObject(body).getJSONObject("data");
            JSONArray klines = data == null ? null : data.getJSONArray("klines");
            if (klines == null || klines.isEmpty()) {
                markPush2hisFail();
                return false;
            }
            List<Double> pcts = new ArrayList<>();
            for (int i = 0; i < klines.size(); i++) {
                String[] p = ((String) klines.get(i)).split(",");
                double close = Double.parseDouble(p[2]);
                double prev = i > 0 ? Double.parseDouble(((String) klines.get(i - 1)).split(",")[2]) : close;
                pcts.add(prev > 0 ? (close - prev) / prev * 100 : 0);
            }
            int n = pcts.size();
            s.put("pct5", sumRange(pcts, n - 5, n));
            s.put("pct10", sumRange(pcts, n - 10, n));
            s.put("pct20", sumRange(pcts, n - 20, n));
            s.put("dailyPcts", pcts);
            return true;
        } catch (Exception ignore) {
            return true; // 数据解析问题跳过，不计入熔断（网络失败走 body==null 分支）
        }
    }

    /** 板块池：当日成交额排行（push2delay→push2 failover，clist熔断期直接返回空=研判降级） */
    private List<Map<String, Object>> fetchCycleSectorPool() throws Exception {
        List<Map<String, Object>> pool = new ArrayList<>();
        if (isPush2ClistBlocked()) {
            logger.info("行情集群clist熔断中，板块池跳过（本轮研判降级，指数/量能/两融维度保留）");
            return pool;
        }
        boolean anyOk = false;
        for (String tpl : new String[]{CYCLE_SECTOR_INDUSTRY_URL_TPL, CYCLE_SECTOR_CONCEPT_URL_TPL}) {
            for (String host : PUSH2_HOSTS) {
                String body = httpGetHosts(String.format(tpl, host));
                if (body == null) continue;
                JSONObject data = JSON.parseObject(body).getJSONObject("data");
                Object diffObj = data == null ? null : data.get("diff");
                JSONArray diff = toDiffArray(diffObj);
                if (diff == null) continue;
                anyOk = true;
                for (int i = 0; i < diff.size(); i++) {
                    JSONObject d = diff.getJSONObject(i);
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("code", d.getString("f12"));
                    m.put("name", d.getString("f14"));
                    m.put("pct", d.getDoubleValue("f3"));
                    m.put("amountYi", d.getDoubleValue("f6") / 1e8);
                    pool.add(m);
                }
                break;
            }
            Thread.sleep(200);
        }
        if (!anyOk) markPush2ClistFail();
        return pool;
    }

    /** 板块轮动明细（AI输入）：今日/3日/5日/10日/20日涨幅+成交额，供AI识别"曾强势+近3日回踩"的轮动回踩板块 */
    private List<Map<String, Object>> buildSectorFeaturesForAi(List<Map<String, Object>> sectorFeatures) {
        List<Map<String, Object>> list = new ArrayList<>();
        if (sectorFeatures == null) return list;
        for (Map<String, Object> s : sectorFeatures) {
            List<Double> pcts = (List<Double>) s.get("dailyPcts");
            if (pcts == null || pcts.isEmpty()) continue;
            int n = pcts.size();
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("name", s.get("name"));
            m.put("todayPct", round2(pcts.get(n - 1)));
            m.put("pct3", round2(sumRange(pcts, n - 3, n)));
            m.put("pct5", round2((Double) s.get("pct5")));
            m.put("pct10", round2((Double) s.get("pct10")));
            m.put("pct20", round2((Double) s.get("pct20")));
            m.put("amountYi", round2((Double) s.get("amountYi")));
            list.add(m);
        }
        return list;
    }

    /** 资金异动候选池（AI预测输入）：全市场当日主力净流入前列且当前未启动（涨幅-3%~7%、未涨停、非ST），
     *  识别"资金先行潜伏"个股；inflowPct=主力净流入占成交比（越高潜伏迹象越明显）。
     *  走clist熔断，失败返回空（AI退化为仅用板块轮动明细研判） */
    private List<Map<String, Object>> fetchCycleCandidates() {
        List<Map<String, Object>> list = new ArrayList<>();
        if (isPush2ClistBlocked()) {
            logger.info("行情集群clist熔断中，资金异动候选池跳过");
            return list;
        }
        // fs=沪深A股；fid=f62主力净流入降序；fltt=2返回已是元/百分比；f12代码/f14名称/f3涨幅/f8换手/f62主力净流入(元)/f184主力净占比
        String tpl = "https://%s/api/qt/clist/get?pn=1&pz=120&po=1&np=1&fltt=2&invt=2&fid=f62"
                + "&fs=m:0+t:6,m:0+t:80,m:1+t:2,m:1+t:23&fields=f12,f13,f14,f2,f3,f8,f62,f184";
        boolean ok = false;
        for (String host : PUSH2_HOSTS) {
            String body = httpGetHosts(String.format(tpl, host));
            if (body == null) continue;
            try {
                JSONObject data = JSON.parseObject(body).getJSONObject("data");
                JSONArray diff = toDiffArray(data == null ? null : data.get("diff"));
                if (diff == null) continue;
                ok = true;
                for (int i = 0; i < diff.size() && list.size() < 60; i++) {
                    JSONObject d = diff.getJSONObject(i);
                    String name = d.getString("f14");
                    Double pct = d.getDouble("f3");
                    Double inflow = d.getDouble("f62"); // 元
                    if (name == null || pct == null || inflow == null) continue;
                    if (name.contains("ST")) continue;   // 剔除ST
                    if (pct >= 7 || pct <= -3) continue; // 已启动/深跌剔除，保留"当前未启动"
                    if (inflow <= 0) continue;           // 必须当日资金净流入
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("code", d.getString("f12"));
                    m.put("name", name);
                    m.put("pct", pct);
                    m.put("mainInflowYi", round2(inflow / 1e8));
                    Double turn = d.getDouble("f8");
                    if (turn != null) m.put("turnover", turn);
                    Double inflowPct = d.getDouble("f184");
                    if (inflowPct != null) m.put("inflowPct", inflowPct);
                    list.add(m);
                }
                break;
            } catch (Exception e) {
                logger.warn("解析资金异动候选池失败：{}", e.getMessage());
            }
        }
        if (!ok) markPush2ClistFail();
        return list;
    }

    private double sumRange(List<Double> list, int from, int to) {
        double sum = 0;
        from = Math.max(0, from);
        for (int i = from; i < Math.min(to, list.size()); i++) sum += list.get(i);
        return sum;
    }

    /**
     * 市场类型判定算法（近10~30天放大视角，全部依据可量化）：
     * - 指数维度：上证20日累计涨跌幅、量能比（5日均额/20日均额）
     * - 杠杆维度：两融余额5日变化
     * - 板块维度：每日涨幅TOP5板块集合的相邻日重合率（轮动指数）、最强板块群连续霸榜天数（主线持续性）
     * - 盘面维度：表内分析记录的涨停数/连板高度均值（有记录则加权）
     * 类型优先级：熊市退潮 > 趋势牛市 > 主线市场 > 轮动市场 > 震荡市；输出全部判定依据。
     */
    private Map<String, Object> analyzeCycle(Map<String, List<double[]>> indexK,
                                             List<Map<String, Object>> marginRows,
                                             List<Map<String, Object>> sectorFeatures,
                                             List<MarketAnalysisDaily> tableRows) {
        Map<String, Object> out = new LinkedHashMap<>();
        List<String> reasons = new ArrayList<>();
        List<double[]> shRows = indexK.get("上证指数");
        List<double[]> cybRows = indexK.get("创业板指");
        int n = shRows == null ? 0 : shRows.size();

        // 1. 指数趋势
        double pct20 = n >= 20 ? sumD(shRows, n - 20, n, 0) : (n > 0 ? sumD(shRows, 0, n, 0) : 0);
        double cybPct20 = cybRows != null && cybRows.size() >= 20 ? sumD(cybRows, cybRows.size() - 20, cybRows.size(), 0) : 0;
        double volRatio = 0;
        if (n >= 20) {
            double avg5 = sumD(shRows, n - 5, n, 1) / 5;
            double avg20 = sumD(shRows, n - 20, n, 1) / 20;
            volRatio = avg20 > 0 ? avg5 / avg20 : 0;
        }
        reasons.add(String.format("上证近20日累计%s%.2f%%，创业板同期%s%.2f%%",
                pct20 >= 0 ? "+" : "", pct20, cybPct20 >= 0 ? "+" : "", cybPct20));
        if (volRatio > 0) {
            reasons.add(String.format("量能比（5日均额/20日均额）%.2f，%s", volRatio,
                    volRatio >= 1.15 ? "显著放量" : volRatio >= 0.95 ? "量能平稳" : "量能萎缩"));
        } else {
            reasons.add("量能比暂缺（东财K线临时受限，指数K线走腾讯兜底无成交额），本轮量能维度不参与判定");
        }

        // 2. 两融趋势
        double marginChg = 0;
        if (marginRows != null && marginRows.size() >= 6) {
            double latest = (double) marginRows.get(marginRows.size() - 1).get("rzrqYi");
            double prev5 = (double) marginRows.get(marginRows.size() - 6).get("rzrqYi");
            marginChg = prev5 > 0 ? (latest - prev5) / prev5 * 100 : 0;
            reasons.add(String.format("两融余额5日%s%.2f%%，杠杆资金%s", marginChg >= 0 ? "+" : "", marginChg,
                    marginChg >= 0.5 ? "加速进场" : marginChg >= 0 ? "温和进场" : "退场观望"));
        }

        // 3. 板块轮动指数与主线持续性
        double rotationIdx = 0;       // 相邻日TOP5平均重合率（低=聚焦=主线，高=快速切换=轮动）
        int mainlineDays = 0;         // 最强板块连续霸榜天数
        String mainlineName = "";
        if (sectorFeatures != null && sectorFeatures.size() >= 10) {
            int days = sectorFeatures.stream().mapToInt(s -> ((List<Double>) s.get("dailyPcts")).size()).min().orElse(0);
            int topN = 5;
            List<List<String>> dailyTop = new ArrayList<>();
            for (int d = Math.max(0, days - 10); d < days; d++) {
                final int day = d;
                List<Map<String, Object>> sorted = new ArrayList<>(sectorFeatures);
                sorted.sort((a, b) -> Double.compare(
                        ((List<Double>) b.get("dailyPcts")).get(Math.min(day, ((List<Double>) b.get("dailyPcts")).size() - 1)),
                        ((List<Double>) a.get("dailyPcts")).get(Math.min(day, ((List<Double>) a.get("dailyPcts")).size() - 1))));
                List<String> top = new ArrayList<>();
                for (int i = 0; i < Math.min(topN, sorted.size()); i++) top.add((String) sorted.get(i).get("name"));
                dailyTop.add(top);
            }
            if (dailyTop.size() >= 2) {
                double sumOverlap = 0;
                for (int i = 1; i < dailyTop.size(); i++) {
                    List<String> prev = dailyTop.get(i - 1);
                    long ov = dailyTop.get(i).stream().filter(prev::contains).count();
                    sumOverlap += (double) ov / topN;
                }
                rotationIdx = sumOverlap / (dailyTop.size() - 1);
            }
            // 最强板块：近5日累计涨幅第一；霸榜天数=它连续出现在每日TOP5里的天数（从最后一天往回数）
            Map<String, Object> strongest = sectorFeatures.stream()
                    .filter(s -> ((List<Double>) s.get("dailyPcts")).size() >= 5)
                    .max(Comparator.comparingDouble(s -> sumRange((List<Double>) s.get("dailyPcts"),
                            ((List<Double>) s.get("dailyPcts")).size() - 5, ((List<Double>) s.get("dailyPcts")).size())))
                    .orElse(null);
            if (strongest != null) {
                mainlineName = (String) strongest.get("name");
                for (int d = dailyTop.size() - 1; d >= 0; d--) {
                    if (dailyTop.get(d).contains(mainlineName)) mainlineDays++;
                    else break;
                }
                double pct5 = (double) strongest.get("pct5");
                reasons.add(String.format("近5日最强板块【%s】累计%s%.2f%%，已连续%d日霸榜涨幅TOP5",
                        mainlineName, pct5 >= 0 ? "+" : "", pct5, mainlineDays));
            }
            reasons.add(String.format("板块轮动指数%.2f（相邻日涨幅TOP5重合率，<0.45聚焦/0.45~0.65均衡/>0.65快速切换）", rotationIdx));
        }

        // 4. 盘面维度（表内记录：涨停/连板高度）
        double avgZt = 0, avgLb = 0;
        if (tableRows != null && !tableRows.isEmpty()) {
            avgZt = tableRows.stream().filter(r -> r.getLimitUpCount() != null)
                    .mapToInt(MarketAnalysisDaily::getLimitUpCount).average().orElse(0);
            avgLb = tableRows.stream().filter(r -> r.getMaxLianban() != null)
                    .mapToInt(MarketAnalysisDaily::getMaxLianban).average().orElse(0);
            reasons.add(String.format("近%d个已记录交易日：日均涨停%.0f家、平均最高连板%.1f板",
                    tableRows.size(), avgZt, avgLb));
        }

        // 5. 类型判定（优先级：熊市退潮 > 趋势牛市 > 主线市场 > 轮动市场 > 震荡市）
        String cycleType;
        String desc;
        if (pct20 < -8 && (marginChg < 0 || (volRatio > 0 && volRatio < 0.9))) {
            cycleType = "熊市退潮期";
            desc = "指数趋势向下且杠杆资金离场，赚钱效应差，宜轻仓防守";
        } else if (pct20 > 8 && volRatio >= 1.05) {
            cycleType = "趋势上行市";
            desc = "指数放量上行，趋势健康，回调即买点";
        } else if (mainlineDays >= 5 && rotationIdx < 0.55) {
            cycleType = "主线明确市场";
            desc = "资金高度聚焦【" + mainlineName + "】等核心方向，主线内强者恒强";
        } else if (rotationIdx > 0.62 && pct20 > -5 && pct20 < 8) {
            cycleType = "轮动博弈市";
            desc = "热点快速轮动切换、缺乏持续主线，追高易被套，宜潜伏低吸";
        } else if (pct20 < -3) {
            cycleType = "弱势震荡市";
            desc = "指数阴跌缩量，题材收缩，控制仓位等待企稳";
            if (avgZt >= 60 || avgLb >= 5) {
                desc += String.format("；但涨停中枢%.0f家/最高%.0f板，题材局部活跃，可轻仓参与结构性主线", avgZt, avgLb);
            }
        } else {
            cycleType = "均衡震荡市";
            desc = "指数区间震荡，结构性行情为主，围绕活跃板块高抛低吸";
            if (avgZt >= 60 || avgLb >= 5) {
                desc += String.format("；涨停中枢%.0f家/最高%.0f板，情绪偏活跃，题材参与度可适当提高", avgZt, avgLb);
            } else if (avgZt > 0 && avgZt < 40) {
                desc += String.format("；涨停中枢仅%.0f家，情绪偏冰点，降低仓位与预期", avgZt);
            }
        }
        out.put("cycleType", cycleType);
        out.put("cycleDesc", desc);
        out.put("reasons", reasons);
        Map<String, Object> metrics = new LinkedHashMap<>();
        metrics.put("shPct20", round2(pct20));
        metrics.put("cybPct20", round2(cybPct20));
        // 缺数据的维度输出null（前端显示'-'）：降级期0值有误导性（会被误读为"量能极低/完全无轮动"）
        metrics.put("volRatio", volRatio > 0 ? round2(volRatio) : null);
        metrics.put("marginChg5d", round2(marginChg));
        metrics.put("rotationIdx", rotationIdx > 0 ? round2(rotationIdx) : null);
        metrics.put("mainlineDays", mainlineDays > 0 ? mainlineDays : null);
        metrics.put("mainlineName", mainlineDays > 0 ? mainlineName : null);
        metrics.put("avgLimitUp", round2(avgZt));
        metrics.put("avgMaxLianban", round2(avgLb));
        out.put("metrics", metrics);
        return out;
    }

    private double sumD(List<double[]> rows, int from, int to, int col) {
        double s = 0;
        for (int i = Math.max(0, from); i < Math.min(to, rows.size()); i++) s += rows.get(i)[col];
        return s;
    }

    /** 近10天每日特征表（前端展示+AI输入）：上证原始行由研判阶段复用（不重复请求），两融来自接口历史，涨停/连板/主线来自表记录（无记录日留空） */
    private List<Map<String, Object>> buildDailyFeatures(List<String[]> shRaw,
                                                         List<Map<String, Object>> marginRows,
                                                         List<Map<String, Object>> sectorFeatures,
                                                         List<MarketAnalysisDaily> tableRows) {
        SimpleDateFormat dayKey = new SimpleDateFormat("yyyy-MM-dd");
        Map<String, MarketAnalysisDaily> tableMap = new HashMap<>();
        if (tableRows != null) {
            for (MarketAnalysisDaily r : tableRows) {
                if (r.getMarketDate() != null) tableMap.put(dayKey.format(r.getMarketDate()), r);
            }
        }
        Map<String, Double> marginByDay = new HashMap<>();
        if (marginRows != null) {
            for (Map<String, Object> m : marginRows) marginByDay.put((String) m.get("date"), (Double) m.get("rzrqYi"));
        }
        List<Map<String, Object>> daily = new ArrayList<>();
        try {
            int size = shRaw.size();
            int from = Math.max(0, size - 10);
            for (int i = size - 1; i >= from; i--) { // 新→旧
                String[] p = shRaw.get(i);
                String date = p[0];
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("date", date);
                double close = Double.parseDouble(p[2]);
                double prev = i > 0 ? Double.parseDouble(shRaw.get(i - 1)[2]) : close;
                row.put("shPct", prev > 0 ? round2((close - prev) / prev * 100) : 0);
                MarketAnalysisDaily tr = tableMap.get(date);
                // 成交额：东财K线自带 → 熔断腾讯兜底无成交额时用表内分析时刻累计成交额兜底（收盘后记录≈全天）
                double amt = p.length > 6 && p[6] != null && !p[6].isEmpty() ? Double.parseDouble(p[6]) : 0;
                if (amt <= 0 && tr != null && tr.getTotalAmount() != null) {
                    amt = tr.getTotalAmount().doubleValue();
                }
                row.put("amountYi", amt > 0 ? round2(amt / 1e8) : null);
                row.put("marginYi", marginByDay.get(date));
                row.put("recorded", tr != null); // 当日是否有市场分析记录（前端淡化无记录行）
                row.put("limitUp", tr == null ? null : tr.getLimitUpCount());
                row.put("maxLianban", tr == null ? null : tr.getMaxLianban());
                row.put("sentiment", tr == null ? null : tr.getSentimentScore());
                row.put("mainLine", tr == null ? null : tr.getMainLine());
                daily.add(row);
            }
        } catch (Exception e) {
            logger.warn("每日特征构建失败", e);
        }
        return daily;
    }

    /** AI策略推荐：按市场类型给操作建议/板块推荐/龙头推荐，全部要求引用输入事实作依据 */
    private Map<String, Object> callCycleAi(Map<String, Object> cycle, Map<String, Object> realtime, List<Map<String, Object>> candidates) {
        Map<String, Object> ai = new LinkedHashMap<>();
        ai.put("operationAdvice", "AI策略推荐暂时不可用（AI服务过载或临时异常）。中期研判与每日特征数据已缓存有效，点击右上角【AI策略推荐】按钮即可重试。");
        ai.put("aiFailed", true);
        try {
            String cycleJson = JSON.toJSONString(cycle);
            // 当日实时盘面摘要（主线/龙头/板块/快讯）
            StringBuilder rt = new StringBuilder();
            rt.append("当日主线：").append(realtime.get("mainLine")).append("\n");
            rt.append("当日持续性初判：").append(realtime.get("mainLineSustain")).append("\n");
            rt.append("情绪：").append(realtime.get("sentimentScore")).append("分(").append(realtime.get("sentimentLevel")).append(")\n");
            Object leaders = realtime.get("leaders");
            if (leaders != null) {
                List<Map<String, Object>> ls = (List<Map<String, Object>>) leaders;
                StringBuilder lb = new StringBuilder();
                for (Map<String, Object> l : ls) {
                    lb.append(String.format("%s(%s,%s,连板%s,板块%s)；", l.get("name"), l.get("code"),
                            l.get("source"), l.get("lianban"), l.get("sector")));
                }
                rt.append("当日龙头股（连板/首板/领涨，含板块归属）：").append(lb).append("\n");
            }
            Object hot = realtime.get("hotSectors");
            if (hot != null) {
                List<Map<String, Object>> hs = (List<Map<String, Object>>) hot;
                StringBuilder hb = new StringBuilder();
                for (int i = 0; i < Math.min(10, hs.size()); i++) {
                    Map<String, Object> s = hs.get(i);
                    hb.append(String.format("%s(%s,+%s亿,%s%%领涨%s)；", s.get("name"), s.get("type"),
                            s.get("mainInflowYi"), s.get("changePct"), s.get("leaderName")));
                }
                rt.append("当日主力净流入TOP板块：").append(hb).append("\n");
            }
            Object news = realtime.get("news");
            if (news != null) {
                List<Map<String, Object>> ns = (List<Map<String, Object>>) news;
                StringBuilder nb = new StringBuilder();
                for (int i = 0; i < Math.min(12, ns.size()); i++) {
                    nb.append(ns.get(i).get("title")).append("；");
                }
                rt.append("最新快讯：").append(nb).append("\n");
            }

            String system = "你是资深A股策略分析师。需要输出两块互补的策略推荐（动量跟随+潜伏预测），全部引用输入数据中真实存在的板块名/股票名/资金数据/快讯事件作为依据，禁止编造。输出严格JSON，不要输出任何JSON以外的内容："
                    + "\n【第一块 momentum 动量跟随】回答'当前谁在走强、能否跟随'：基于【当日实时盘面】推荐当日已确立主线/最强轮动方向中的领涨龙头（连板梯队高位股+板块中军）。"
                    + "个股可以是当日涨停/领涨股，但reason必须明确标注追高风险（如：当日已涨停、位置较高，仅适合打板/情绪高手，追高需谨慎）；"
                    + "\n【第二块 predictive 潜伏预测】回答'接下来谁可能启动、如何提前布局'，预测未来1-3个交易日可能启动的板块与个股，规则："
                    + "a.板块从【近10日板块轮动明细】中找'曾强势（pct10或pct20居前）但近3日回踩（pct3明显低于pct10）、当日出现资金回流迹象'的板块，"
                    + "或从【资金异动候选池】统计板块聚集度（同一板块≥3只进入主力净流入前列=资金先行潜伏迹象），优先推荐'即将轮到'的板块；"
                    + "b.板块禁则：当日涨幅第一/净流入第一的板块属于已启动，禁止仅以此作为predictive推荐理由；"
                    + "c.个股必须是'当前未启动但具备启动条件'——当日涨幅<7%且未涨停（候选池已预过滤），依据至少一条可验证事实："
                    + "主力净流入及inflowPct占成交比（资金先行潜伏）、近期涨停基因（当日龙头休息回踩等待二次启动）、快讯催化、板块内比价补涨（同板块龙头已涨停而它仍在低位）；"
                    + "d.个股禁则：predictive中禁止推荐依据写'当日领涨股/当日涨停/当日涨幅最高'；"
                    + "e.predictive每只股reason必须包含'推荐依据+启动确认信号'（如：放量收复5日线/突破近期平台/板块再度领涨时率先封板）；"
                    + "\n【市场类型适配】主线明确市场：momentum推主线领涨龙头、predictive推主线板块内中军回踩位与低位补涨；"
                    + "轮动博弈市/震荡市：momentum推当日最强轮动方向龙头、predictive推轮动回踩板块；"
                    + "趋势上行市：momentum推量价齐升强势板块龙头、predictive推回踩低吸机会；熊市退潮期：两块均以防守为主，明确建议轻仓观望。"
                    + "\n【输出JSON格式】{\"momentum\":{\"operationAdvice\":\"150字内动量策略（仓位/跟随方式/风险控制）\","
                    + "\"sectors\":[{\"name\":\"板块名\",\"stage\":\"启动初期|主升期\",\"logic\":\"推荐逻辑50字内\","
                    + "\"evidenceNews\":\"消息面催化\",\"evidenceMoney\":\"资金面动作\",\"evidenceEmotion\":\"情绪面地位\","
                    + "\"leaders\":[{\"code\":\"6位代码\",\"name\":\"股票名\",\"price\":\"最新价或空\",\"reason\":\"推荐依据+追高风险提示\",\"beginner\":true}]}]},"
                    + "\"predictive\":{\"operationAdvice\":\"150字内潜伏策略（仓位/布局节奏/确认信号触发后加仓思路）\","
                    + "\"sectors\":[{\"name\":\"板块名\",\"stage\":\"潜伏期|启动初期|主升期\",\"logic\":\"推荐逻辑50字内（为何即将启动）\","
                    + "\"evidenceNews\":\"消息面催化\",\"evidenceMoney\":\"资金面动作（引用候选池净流入/板块资金/两融事实）\","
                    + "\"evidenceEmotion\":\"情绪面地位（引用涨停/连板/轮动明细事实）\","
                    + "\"leaders\":[{\"code\":\"6位代码\",\"name\":\"股票名\",\"price\":\"最新价或空\",\"reason\":\"推荐依据+启动确认信号\",\"beginner\":true}]}]}}"
                    + "（每块sectors给2-3个板块，每个板块leaders给2-4只，beginner=true仅限00/60开头主板股；"
                    + "predictive的stage语义：潜伏期=资金异动+催化但股价未启动；启动初期=盘中已现异动迹象；主升期仅限主线市场且必须给出回踩低吸思路）";

            String user = "【中期市场研判】\n" + cycleJson
                    + "\n\n【近10日每日特征】\n" + JSON.toJSONString(cycle.get("dailyFeatures"))
                    + "\n\n【近10日板块轮动明细】（todayPct/pct3/pct5/pct10/pct20=今日及近3/5/10/20日累计涨幅%，识别曾强势但近3日回踩的板块）\n"
                    + JSON.toJSONString(cycle.get("sectorFeatures"))
                    + "\n\n【资金异动候选池】（全市场主力净流入前列且当前未启动：涨幅-3%~7%、未涨停、非ST；"
                    + "mainInflowYi=主力净流入亿，inflowPct=净流入占成交比越高潜伏迹象越明显，turnover=换手%）\n"
                    + JSON.toJSONString(candidates)
                    + "\n\n【当日实时盘面】\n" + rt;

            String resp = aiCommonUtil.callWithSystem(system, user);
            if (resp != null && !resp.isEmpty()) {
                Map<String, Object> parsed = parseCycleAiJson(resp);
                if (!parsed.isEmpty()) {
                    return parsed;
                }
                ai.put("operationAdvice", trunc(resp, 3000));
                ai.put("parseFailed", true);
            }
        } catch (Exception e) {
            logger.error("AI中期策略推荐失败", e);
            ai.put("operationAdvice", "AI策略推荐异常：" + e.getMessage() + "。点击右上角【AI策略推荐】按钮可重试。");
            ai.put("aiFailed", true);
        }
        return ai;
    }

    /** 解析AI策略JSON：剥离```围栏容错；支持双块结构（momentum动量+predictive潜伏）与旧单块结构 */
    private Map<String, Object> parseCycleAiJson(String resp) {
        Map<String, Object> out = new LinkedHashMap<>();
        try {
            String text = resp.trim();
            int s = text.indexOf('{');
            int e = text.lastIndexOf('}');
            if (s < 0 || e <= s) return out;
            text = text.substring(s, e + 1);
            JSONObject jo = JSON.parseObject(text);
            // 双块结构：momentum（动量跟随）+ predictive（潜伏预测）
            if (jo.containsKey("momentum") || jo.containsKey("predictive")) {
                JSONObject mom = jo.getJSONObject("momentum");
                if (mom != null) out.put("momentum", parseSectorBlock(mom));
                JSONObject pre = jo.getJSONObject("predictive");
                if (pre != null) out.put("predictive", parseSectorBlock(pre));
                return out;
            }
            // 旧单块结构兼容
            out.put("operationAdvice", jo.getString("operationAdvice"));
            JSONArray sectors = jo.getJSONArray("sectors");
            if (sectors != null) {
                out.put("sectors", parseSectors(sectors));
            }
        } catch (Exception ex) {
            logger.warn("AI策略JSON解析失败: {}", ex.getMessage());
        }
        return out;
    }

    /** 单块策略解析：operationAdvice + sectors 列表 */
    private Map<String, Object> parseSectorBlock(JSONObject block) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("operationAdvice", block.getString("operationAdvice"));
        JSONArray sectors = block.getJSONArray("sectors");
        m.put("sectors", sectors != null ? parseSectors(sectors) : new ArrayList<>());
        return m;
    }

    /** sectors 数组解析（双块/单块共用） */
    private List<Map<String, Object>> parseSectors(JSONArray sectors) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (int i = 0; i < sectors.size() && i < 4; i++) {
            JSONObject sc = sectors.getJSONObject(i);
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("name", sc.getString("name"));
            m.put("stage", sc.getString("stage"));
            m.put("logic", sc.getString("logic"));
            m.put("evidenceNews", sc.getString("evidenceNews"));
            m.put("evidenceMoney", sc.getString("evidenceMoney"));
            m.put("evidenceEmotion", sc.getString("evidenceEmotion"));
            List<Map<String, Object>> leaders = new ArrayList<>();
            JSONArray larr = sc.getJSONArray("leaders");
            if (larr != null) {
                for (int j = 0; j < larr.size() && j < 4; j++) {
                    JSONObject l = larr.getJSONObject(j);
                    Map<String, Object> lm = new LinkedHashMap<>();
                    lm.put("code", l.getString("code"));
                    lm.put("name", l.getString("name"));
                    lm.put("price", l.getString("price"));
                    lm.put("reason", l.getString("reason"));
                    lm.put("beginner", l.getBooleanValue("beginner"));
                    leaders.add(lm);
                }
            }
            m.put("leaders", leaders);
            list.add(m);
        }
        return list;
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
                // 前2次只记消息不记全栈（拉黑期间重试会大量触发，避免刷屏）；最后一次保留全栈定位
                if (attempt >= 3) {
                    logger.warn("HTTP请求失败 attempt={} url={}", attempt, url, e);
                } else {
                    logger.warn("HTTP请求失败 attempt={} url={} err={}", attempt, url, e.getMessage());
                }
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
            logger.warn("HTTP请求失败 url={} err={}", url, e.getMessage());
        }
        return null;
    }

    /** 单次GET（不重试）：用于可降级的K线请求，配合熔断器避免拉黑期间重试放大封禁+日志刷屏 */
    private static String httpGetNoRetry(String url) {
        return httpGetHosts(url);
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
