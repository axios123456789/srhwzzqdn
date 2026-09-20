package com.xk.srhwzzqdn.manager.assetControlArea.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.xk.srhwzzqdn.manager.assetControlArea.mapper.ShortTermPickMapper;
import com.xk.srhwzzqdn.manager.assetControlArea.service.ShortTermPickService;
import com.xk.srhwzzqdn.manager.util.AiCommonUtil;
import com.xk.srhwzzqdn.model.entity.assetControl.ShortTermExperience;
import com.xk.srhwzzqdn.model.entity.assetControl.ShortTermPickDaily;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * 短线选股核心实现（独立服务，不改动原有选股/研判功能）
 * <p>
 * 候选池收敛漏斗（请求预算约85次/轮）：
 * 1) 涨停池+炸板池 → 类型1妖股梯队直接成形（连板高度+封板质量+涨停基因评分）
 * 2) 行情快照clist粗筛（流通市值20~500亿、换手3%~25%、当日涨幅-2%~8%、年初涨幅>-15%、非ST）
 *    → 按主力净流入占比降序取top70 → 60日K线合并判定：
 *    类型2低位潜伏（低位+缩量整理+涨停基因+平台未启动）/ 类型3趋势延续（近5日有涨停+均线多头+非低位）
 * 3) 行为判定矩阵（0增量请求，复用池/快照/K线数据）：
 *    pull_up继续拉升 / absorb吸筹 / organize强势整理 / distribute出货嫌疑（出货行为一票剔除）/ unknown无法判定
 * <p>
 * 时段归库状态机：tradeDate=最近交易日（指数K线判定，非交易日归上交易日）；
 * 非交易日→直读库close版；交易日盘中→先删后插intraday版；收盘后→库为close版直读，否则先删后插close版。
 * 正常结果缓存30分钟，熔断降级结果缓存5分钟。
 * <p>
 * 反爬安全：东财K线首次失败即熔断5分钟（期间直走腾讯兜底，单次不重试）；
 * clist快照双域名failover，全失败即熔断3分钟；池接口串行+200ms间隔。
 */
@Service
public class ShortTermPickServiceImpl implements ShortTermPickService {

    private static final Logger logger = LoggerFactory.getLogger(ShortTermPickServiceImpl.class);

    @Autowired
    private ShortTermPickMapper shortTermPickMapper;

    @Autowired
    private AiCommonUtil aiCommonUtil;

    // ===== 东财涨停/炸板池（与市场研判同源模板） =====
    private static final String ZT_POOL_URL_TPL =
            "http://push2ex.eastmoney.com/getTopicZTPool?ut=7eea3edcaed734bea9cbfc24409ed989&dpt=wz.ztzt"
                    + "&Pageindex=0&pagesize=600&sort=fbt%%3Aasc&date=%s";
    private static final String ZB_POOL_URL_TPL =
            "http://push2ex.eastmoney.com/getTopicZBPool?ut=7eea3edcaed734bea9cbfc24409ed989&dpt=wz.ztzt"
                    + "&Pageindex=0&pagesize=600&sort=fbt%%3Aasc&date=%s";

    // ===== 行情快照clist：沪深A股，按主力净流入占比(f184)降序（f62净流入额会被大市值股霸榜，过滤后候选为0） =====
    // 注意：clist单页pz上限实测100，需翻页pn=1..3覆盖f184 top300（宽口径粗筛基数）
    private static final String[] CLIST_HOSTS = {"https://push2delay.eastmoney.com", "https://push2.eastmoney.com"};
    private static final String CLIST_PATH =
            "/api/qt/clist/get?pn=%d&pz=100&po=1&np=1&fltt=2&invt=2&fid=f184"
                    + "&fs=m:0+t:6,m:0+t:80,m:1+t:2,m:1+t:23"
                    // f12代码 f13市场 f14名称 f2现价 f3涨幅 f8换手 f10量比 f20总市值 f21流通市值 f24年初涨幅
                    // f47内盘 f48外盘 f50量比 f62主力净流入 f100行业 f103概念 f184主力净占比
                    + "&fields=f12,f13,f14,f2,f3,f8,f10,f20,f21,f24,f47,f48,f50,f62,f100,f103,f184";
    private static final int CLIST_PAGES = 3;

    // ===== 池票批量快照（ulist按代码查询）：妖股来自涨停/炸板池，池接口无资金/换手/市值/概念，用ulist补齐 =====
    // ulist字段口径与clist不同：量比=f10（f50为成交额）；f47/f48内外盘返回负值不可靠，不采集
    private static final String ULIST_PATH =
            "/api/qt/ulist.np/get?fltt=2&invt=2&secids=%s"
                    + "&fields=f12,f14,f2,f3,f8,f10,f20,f21,f62,f100,f103,f184";
    private static final int ULIST_BATCH = 60;

    // ===== K线：60根日K（东财主源 + 腾讯兜底） =====
    private static final String EAST_KLINE_URL_TPL =
            "http://push2his.eastmoney.com/api/qt/stock/kline/get?secid=%s&klt=101&fqt=1&lmt=60&end=20500101"
                    + "&fields1=f1,f2,f3&fields2=f51,f52,f53,f54,f55,f56,f57";
    private static final String TX_KLINE_URL = "https://ifzq.gtimg.cn/appstock/app/fqkline/get"; // web.ifzq.gtimg.cn已被腾讯501废弃，裸域实测正常

    // ===== 熔断器（本服务独立计数，不影响其他模块自己的熔断状态） =====
    private static final long EAST_KLINE_BREAK_MS = 5 * 60 * 1000L;
    private static volatile long eastKlineBlockedUntil = 0L;
    private static final long CLIST_BREAK_MS = 3 * 60 * 1000L;
    private static volatile long clistBlockedUntil = 0L;

    // ===== 结果缓存：正常30分钟，降级5分钟 =====
    private static final long CACHE_TTL_OK = 30 * 60 * 1000L;
    private static final long CACHE_TTL_DEGRADED = 5 * 60 * 1000L;
    private final Map<String, Map<String, Object>> resultCache = new ConcurrentHashMap<>();

    /** 盘中推荐redis缓存（盘中数据不入库，纯缓存；正常30分钟/降级5分钟TTL，重启后盘中数据自然重算） */
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    private static final Object COMPUTE_LOCK = new Object();

    // 最近交易日解析缓存（按自然日失效）
    private static volatile String tradeDateCacheDay = "";
    private static volatile String tradeDateCacheValue = "";

    // ================================================================
    // 主入口：时段归库状态机
    // ================================================================
    @Override
    public Map<String, Object> getShortTermStocks() {
        String tradeDate = resolveTradeDate();
        String today = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        boolean tradingDay = today.equals(tradeDate);

        String phase;
        if (!tradingDay) {
            phase = "close"; // 非交易日按上个交易日收盘版
        } else {
            int hm = Integer.parseInt(new SimpleDateFormat("HHmm").format(new Date()));
            phase = hm >= 1500 ? "close" : "intraday";
        }

        String cacheKey = tradeDate + "|" + phase;
        Map<String, Object> cached = resultCache.get(cacheKey);
        if (cached != null) {
            long ttl = Boolean.TRUE.equals(cached.get("degraded")) ? CACHE_TTL_DEGRADED : CACHE_TTL_OK;
            if (System.currentTimeMillis() - (long) cached.get("_cacheTime") < ttl) {
                Map<String, Object> out = new LinkedHashMap<>(cached);
                out.put("fromCache", true);
                return out;
            }
            resultCache.remove(cacheKey);
        }

        // 盘中专用：redis二级缓存（盘中数据不入库，纯缓存；TTL即数据时效，过期自然重算）
        String redisKey = "shortTerm:pick:" + tradeDate + "|" + phase;
        if ("intraday".equals(phase)) {
            try {
                String json = stringRedisTemplate.opsForValue().get(redisKey);
                if (json != null) {
                    Map<String, Object> out = JSON.parseObject(json);
                    out.put("fromCache", true);
                    return out;
                }
            } catch (Exception e) {
                logger.warn("短线选股盘中redis缓存读取失败，改走重算 | key={}", redisKey, e);
            }
        }

        // 盘中不进行任何库读写（既不查询也不删除），数据只存在redis缓存里；
        // 仅非交易日/收盘后走库内直读（这些阶段数据落库，用于留存与复盘）
        if (!"intraday".equals(phase)) {
            List<ShortTermPickDaily> rows = shortTermPickMapper.selectByTradeDate(java.sql.Date.valueOf(tradeDate));
            if (!rows.isEmpty()) {
                boolean hasClose = rows.stream().anyMatch(r -> "close".equals(r.getPickPhase()));
                // 非交易日库有数据即直读（含盘中残留）；交易日收盘后直读close版
                if (hasClose || !tradingDay) {
                    Map<String, Object> out = assembleFromDb(tradeDate, rows);
                    out.put("_cacheTime", System.currentTimeMillis());
                    resultCache.put(cacheKey, out);
                    return out;
                }
            }
        }

        // 实时计算 + 先删后插入库（加锁防并发双算/双删）
        Map<String, Object> out;
        synchronized (COMPUTE_LOCK) {
            // 双重检查：等待期间别的线程可能已算完并缓存
            cached = resultCache.get(cacheKey);
            if (cached != null) {
                long ttl = Boolean.TRUE.equals(cached.get("degraded")) ? CACHE_TTL_DEGRADED : CACHE_TTL_OK;
                if (System.currentTimeMillis() - (long) cached.get("_cacheTime") < ttl) {
                    Map<String, Object> o = new LinkedHashMap<>(cached);
                    o.put("fromCache", true);
                    return o;
                }
            }
            out = computeAndPersist(tradeDate, phase);
        }
        out.put("_cacheTime", System.currentTimeMillis());
        resultCache.put(cacheKey, out);
        // 盘中：结果写redis缓存（不入库）；TTL正常30分钟/降级5分钟，与内存缓存语义一致
        if ("intraday".equals(phase)) {
            try {
                long ttlMin = Boolean.TRUE.equals(out.get("degraded")) ? 5 : 30;
                stringRedisTemplate.opsForValue().set(redisKey, JSON.toJSONString(out), ttlMin, TimeUnit.MINUTES);
            } catch (Exception e) {
                logger.warn("短线选股盘中推荐redis写入失败（不影响本次返回） | key={}", redisKey, e);
            }
        }
        // 清理其他key的旧缓存，防内存膨胀
        if (resultCache.size() > 4) {
            resultCache.keySet().removeIf(k -> !k.equals(cacheKey));
        }
        return out;
    }

    /** 最近交易日：指数K线最后一根的日期（东财→腾讯→本地日历兜底），按自然日缓存 */
    private String resolveTradeDate() {
        String todayKey = new SimpleDateFormat("yyyyMMdd").format(new Date());
        if (todayKey.equals(tradeDateCacheDay) && tradeDateCacheValue != null && !tradeDateCacheValue.isEmpty()) {
            return tradeDateCacheValue;
        }
        String td = null;
        // 1) 东财上证指数日K（单次不重试，失败即触发K线熔断走兜底）
        if (!eastKlineBlocked()) {
            try {
                String url = String.format(EAST_KLINE_URL_TPL, "1.000001");
                String resp = httpGetNoRetry(url);
                if (resp != null) {
                    JSONArray ks = JSON.parseObject(resp).getJSONObject("data").getJSONArray("klines");
                    if (ks != null && !ks.isEmpty()) {
                        td = ks.getString(ks.size() - 1).split(",")[0];
                    }
                } else {
                    markEastKlineFail();
                }
            } catch (Exception e) {
                markEastKlineFail();
            }
        }
        // 2) 腾讯指数兜底
        if (td == null) {
            try {
                String resp = httpGetNoRetry(TX_KLINE_URL + "?param=sh000001,day,,,5,qfq");
                if (resp != null) {
                    JSONObject node = JSON.parseObject(resp).getJSONObject("data").getJSONObject("sh000001");
                    JSONArray days = node == null ? null : (node.getJSONArray("qfqday") == null
                            ? node.getJSONArray("day") : node.getJSONArray("qfqday"));
                    if (days != null && !days.isEmpty()) {
                        td = days.getJSONArray(days.size() - 1).getString(0);
                    }
                }
            } catch (Exception ignore) {
                // 兜底失败走本地日历
            }
        }
        // 3) 本地日历（仅跳过周末，法定节假日无法识别——仅极端双兜底失败时使用）
        if (td == null) {
            Calendar c = Calendar.getInstance();
            while (c.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY || c.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
                c.add(Calendar.DAY_OF_MONTH, -1);
            }
            td = new SimpleDateFormat("yyyy-MM-dd").format(c.getTime());
            logger.warn("最近交易日判定双兜底失败，退化为本地日历（节假日可能误判）：{}", td);
        }
        tradeDateCacheDay = todayKey;
        tradeDateCacheValue = td;
        return td;
    }

    // ================================================================
    // 实时计算 + 归库
    // ================================================================
    private Map<String, Object> computeAndPersist(String tradeDate, String phase) {
        long start = System.currentTimeMillis();
        List<String> degradeReasons = new ArrayList<>();

        // 1) 涨停池 + 炸板池（按归属交易日查询，串行，间隔200ms）
        String tradeDateCompact = tradeDate.replace("-", "");
        JSONObject zt = fetchPool(ZT_POOL_URL_TPL, tradeDateCompact);
        JSONObject zb = fetchPool(ZB_POOL_URL_TPL, tradeDateCompact);
        List<Map<String, Object>> ztList = parsePoolStocks(zt.getJSONArray("pool"), false);
        List<Map<String, Object>> zbList = parsePoolStocks(zb.getJSONArray("pool"), true);
        if (zt.getIntValue("count") < 0 && zb.getIntValue("count") < 0) {
            degradeReasons.add("涨停/炸板池获取失败（接口异常或限流），妖股梯队本轮缺失");
        }

        // 2) 类型1候选：涨停池为主，炸板池未回封的并入（回封的补充炸板次数）
        Map<String, Map<String, Object>> type1 = new LinkedHashMap<>();
        for (Map<String, Object> s : ztList) {
            if (isBjCode((String) s.get("code"))) continue;
            s.put("limitUpNow", true);
            s.put("zhabanCount", 0);
            type1.put((String) s.get("code"), s);
        }
        for (Map<String, Object> s : zbList) {
            if (isBjCode((String) s.get("code"))) continue;
            String code = (String) s.get("code");
            Map<String, Object> exist = type1.get(code);
            if (exist != null) {
                exist.put("zhabanCount", s.get("zhabanCount")); // 回封但炸过板
            } else {
                s.put("limitUpNow", false);
                type1.put(code, s);
            }
        }

        // 2.5) 池票（妖股）批量补快照：池接口无主力资金/换手/量比/总市值/概念，ulist按代码补齐宽表字段
        enrichPoolSnapshot(type1.values(), degradeReasons);

        // 3) 行情快照粗筛 → 类型2/3候选
        List<Map<String, Object>> cand = fetchClistCandidates(degradeReasons);

        // 4) K线并发抓取（妖股前40 + 快照候选，去重后封顶120，控制请求预算）
        List<Map<String, Object>> type1ForKline = new ArrayList<>(type1.values());
        type1ForKline.sort((a, b) -> {
            int c = Integer.compare((int) b.getOrDefault("lianban", 0), (int) a.getOrDefault("lianban", 0));
            if (c != 0) return c;
            Double fa = asDouble(a.get("sealFundYi"));
            Double fb = asDouble(b.get("sealFundYi"));
            return Double.compare(fb == null ? 0 : fb, fa == null ? 0 : fa);
        });
        Map<String, Integer> targets = new LinkedHashMap<>();
        for (Map<String, Object> s : type1ForKline) {
            if (targets.size() >= 40) break;
            targets.put((String) s.get("code"), marketInt((String) s.get("code")));
        }
        for (Map<String, Object> s : cand) {
            if (targets.size() >= 120) break;
            targets.putIfAbsent((String) s.get("code"), marketInt((String) s.get("code")));
        }
        Map<String, List<Bar>> klines = fetchKlines(targets);
        logger.info("短线选股数据采集 | 涨停池={} 炸板池={} 快照候选={} K线成功={}/{} | 耗时={}ms",
                ztList.size(), zbList.size(), cand.size(), klines.size(), targets.size(),
                System.currentTimeMillis() - start);

        // 5) 妖股梯队：K线增强（涨停基因/位置）+ 行为判定 + 评分
        for (Map<String, Object> s : type1.values()) {
            enrichKlineFeatures(s, klines.get((String) s.get("code")));
            judgeBehavior(s);
        }

        // 6) 快照候选：K线特征 → 类型2/3归类
        List<Map<String, Object>> qianfuList = new ArrayList<>();
        List<Map<String, Object>> qushiList = new ArrayList<>();
        for (Map<String, Object> s : cand) {
            List<Bar> bars = klines.get((String) s.get("code"));
            if (bars == null || bars.size() < 25) continue; // K线不足（次新/停牌）无法判定趋势结构
            Map<String, Object> f = klineFeatures(bars);
            mergeFeatures(s, f);
            boolean qianfu = asDouble(f.get("lowDepth")) != null && asDouble(f.get("lowDepth")) <= 28
                    && (asDouble(f.get("volShrink")) == null || asDouble(f.get("volShrink")) <= 1.15)
                    && (Integer) f.getOrDefault("ztCount60", 0) >= 1
                    && asDouble(f.get("gain20")) != null && asDouble(f.get("gain20")) < 15
                    && (asDouble(f.get("gain5")) == null || asDouble(f.get("gain5")) < 10);
            boolean qushi = f.get("recentZtDaysAgo") != null && (Integer) f.get("recentZtDaysAgo") <= 4
                    && asDouble(f.get("lowDepth")) != null && asDouble(f.get("lowDepth")) >= 12
                    && (Boolean.TRUE.equals(f.get("trendMaOk"))
                    || ((asDouble(f.get("ma5")) != null && asDouble(f.get("ma10")) != null
                    && asDouble(f.get("ma5")) > asDouble(f.get("ma10"))
                    && asDouble(f.get("gain5")) != null && asDouble(f.get("gain5")) > 0)));
            if (qianfu && !qushi) {
                qianfuList.add(s);
            } else if (qushi) {
                qushiList.add(s);
            }
        }

        // 7) 行为判定 + 评分 + 排序 + 各类top10
        for (Map<String, Object> s : qianfuList) judgeBehavior(s);
        for (Map<String, Object> s : qushiList) judgeBehavior(s);
        List<Map<String, Object>> yaoguTop = scoreAndTop(type1.values(), "yaogu", 10);
        List<Map<String, Object>> qianfuTop = scoreAndTop(qianfuList, "qianfu", 10);
        List<Map<String, Object>> qushiTop = scoreAndTop(qushiList, "qushi", 10);

        // 8) 入库：仅收盘版先删后插（长期留存供复盘）；盘中不入库——数据只存redis缓存（每30分钟随缓存过期自然重算）
        boolean saved = false;
        if (!"intraday".equals(phase)) {
            List<ShortTermPickDaily> rows = new ArrayList<>();
            rows.addAll(toRows(yaoguTop, tradeDate, phase, "yaogu"));
            rows.addAll(toRows(qianfuTop, tradeDate, phase, "qianfu"));
            rows.addAll(toRows(qushiTop, tradeDate, phase, "qushi"));
            try {
                shortTermPickMapper.deleteByTradeDate(java.sql.Date.valueOf(tradeDate));
                if (!rows.isEmpty()) {
                    shortTermPickMapper.batchInsertPicks(rows);
                }
                saved = true;
            } catch (Exception e) {
                logger.error("短线推荐归库失败 tradeDate={} phase={}", tradeDate, phase, e);
            }
        }

        // 9) 组装输出
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("tradeDate", tradeDate);
        out.put("phase", phase);
        out.put("phaseText", "intraday".equals(phase) ? "盘中实时推荐" : "收盘后推荐");
        out.put("fromDb", false);
        out.put("fromCache", false);
        out.put("degraded", !degradeReasons.isEmpty());
        out.put("degradeReasons", degradeReasons);
        out.put("saved", saved);
        out.put("groups", Arrays.asList(
                groupOf("yaogu", "妖股梯队", yaoguTop),
                groupOf("qianfu", "低位潜伏", qianfuTop),
                groupOf("qushi", "趋势延续", qushiTop)));
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("ztCount", zt.getIntValue("count"));
        stats.put("zbCount", zb.getIntValue("count"));
        stats.put("candidateCount", cand.size());
        stats.put("klineFetched", klines.size());
        stats.put("costMs", System.currentTimeMillis() - start);
        out.put("stats", stats);
        return out;
    }

    // ================================================================
    // 数据源：池 / 快照 / K线
    // ================================================================
    /** 涨停/炸板池（date=归属交易日；返回tc总数与pool明细），串行+200ms间隔防限流；异常时count=-1供降级判定 */
    private JSONObject fetchPool(String urlTpl, String tradeDateCompact) {
        JSONObject out = new JSONObject();
        out.put("count", -1);
        out.put("pool", new JSONArray());
        try {
            String body = httpGet(String.format(urlTpl, tradeDateCompact), 3);
            JSONObject json = JSON.parseObject(body);
            if (json != null && json.getJSONObject("data") != null) {
                out.put("count", json.getJSONObject("data").getIntValue("tc"));
                out.put("pool", json.getJSONObject("data").getJSONArray("pool"));
            }
        } catch (Exception e) {
            logger.warn("短线选股获取涨跌停池失败 url={}", urlTpl, e);
        }
        sleep(200);
        return out;
    }

    /** 解析池明细 → 统一股票结构（炸板池标记炸板次数） */
    private List<Map<String, Object>> parsePoolStocks(JSONArray pool, boolean isZb) {
        List<Map<String, Object>> list = new ArrayList<>();
        if (pool == null) return list;
        for (int i = 0; i < pool.size(); i++) {
            JSONObject d = pool.getJSONObject(i);
            Map<String, Object> s = new LinkedHashMap<>();
            String code = d.getString("c");
            s.put("code", code);
            s.put("name", d.getString("n"));
            s.put("price", round2(d.getDoubleValue("p") / 1000.0));
            s.put("changePct", round2(d.getDoubleValue("zdp")));
            s.put("lianban", d.getIntValue("lbc"));
            s.put("industry", d.getString("hybk"));
            Double ltsz = asDouble(d.getDoubleValue("ltsz"));
            s.put("circCap", ltsz == null ? null : round2(ltsz / 1e8));
            Double fund = asDouble(d.getDoubleValue("fund"));
            s.put("sealFundYi", fund == null ? null : round2(fund / 1e8));
            // 封单坚决度：封单额/流通市值%
            s.put("sealRatio", (fund == null || ltsz == null || ltsz <= 0) ? null : round2(fund / ltsz * 100));
            s.put("zhabanCount", isZb ? d.getIntValue("zbc") : 0);
            s.put("ztTimeFirst", formatSealTime(d.getString("fbt")));
            JSONObject zttj = d.getJSONObject("zttj");
            s.put("ztStat", zttj == null ? "" : zttj.getIntValue("days") + "天" + zttj.getIntValue("ct") + "板");
            list.add(s);
        }
        list.sort((a, b) -> Integer.compare((int) b.getOrDefault("lianban", 0), (int) a.getOrDefault("lianban", 0)));
        return list;
    }

    /** 行情快照粗筛（clist翻3页覆盖f184 top300）：市值/换手/涨幅/年初涨幅/非ST过滤后按主力净占比取top70 */
    private List<Map<String, Object>> fetchClistCandidates(List<String> degradeReasons) {
        if (clistBlocked()) {
            degradeReasons.add("行情快照接口熔断中（3分钟），低位潜伏/趋势延续本轮降级");
            return new ArrayList<>();
        }
        boolean ok = false;
        List<Map<String, Object>> list = new ArrayList<>();
        for (String host : CLIST_HOSTS) {
            List<Map<String, Object>> merged = new ArrayList<>();
            boolean allPagesOk = true;
            for (int pn = 1; pn <= CLIST_PAGES; pn++) {
                String body = httpGetNoRetry(host + String.format(CLIST_PATH, pn));
                if (body == null) {
                    allPagesOk = false;
                    break;
                }
                try {
                    JSONObject data = JSON.parseObject(body).getJSONObject("data");
                    JSONArray diff = toDiffArray(data == null ? null : data.get("diff"));
                    if (diff == null || diff.isEmpty()) {
                        allPagesOk = false;
                        break;
                    }
                    parseClistRows(diff, merged);
                    if (pn < CLIST_PAGES) Thread.sleep(200); // 页间限速防反爬
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    allPagesOk = false;
                    break;
                } catch (Exception e) {
                    logger.warn("短线选股行情快照解析失败 host={} pn={}", host, pn, e);
                    allPagesOk = false;
                    break;
                }
            }
            if (allPagesOk) {
                list = merged;
                ok = true;
                break;
            }
        }
        if (!ok) {
            markClistFail();
            degradeReasons.add("行情快照接口双域名均失败，已熔断3分钟，低位潜伏/趋势延续本轮降级");
            return new ArrayList<>();
        }
        // 按主力净流入占比降序取top70
        list.sort((a, b) -> {
            Double pa = asDouble(a.get("mainInflowPct"));
            Double pb = asDouble(b.get("mainInflowPct"));
            return Double.compare(pb == null ? -999 : pb, pa == null ? -999 : pa);
        });
        return new ArrayList<>(list.subList(0, Math.min(70, list.size())));
    }

    /** clist行解析：非ST/仙股过滤 + 宽口径粗筛（换手2%~25%、涨幅-2%~11%，兼顾趋势票与低位缩量票）+ 概念截断 */
    private void parseClistRows(JSONArray diff, List<Map<String, Object>> out) {
        for (int i = 0; i < diff.size(); i++) {
            JSONObject d = diff.getJSONObject(i);
            String code = d.getString("f12");
            String name = d.getString("f14");
            if (code == null || name == null) continue;
            if (name.contains("ST") || name.contains("退")) continue;
            Double price = asDouble(d.get("f2"));
            Double pct = asDouble(d.get("f3"));
            Double turnover = asDouble(d.get("f8"));
            Double circCapRaw = asDouble(d.get("f21"));
            Double circCap = circCapRaw == null ? null : circCapRaw / 1e8; // f21原始单位为元，转亿后再过滤
            Double ytdPct = asDouble(d.get("f24"));
            Double mainInflow = asDouble(d.get("f62"));
            Double mainPct = asDouble(d.get("f184"));
            if (price == null || price < 2) continue;                       // 剔除仙股
            if (circCap == null || circCap < 20 || circCap > 500) continue; // 流通市值20~500亿
            if (turnover == null || turnover < 2 || turnover > 25) continue;// 换手2%~25%（下限3%会误杀低位缩量票）
            if (pct == null || pct < -2 || pct > 11) continue;              // 涨幅-2%~11%（上限8%会误杀当日强势的趋势票）
            if (ytdPct == null || ytdPct <= -15) continue;                  // 年初涨幅>-15%
            Double vr = asDouble(d.get("f10"));                             // clist字段语义：f10=量比（f50是成交额元，误用会越界炸库）
            if (vr != null && (vr < 0 || vr > 100)) vr = null;              // 量比合理性兜底，防脏数据炸整批归库
            Map<String, Object> s = new LinkedHashMap<>();
            s.put("code", code);
            s.put("name", name);
            s.put("marketFlag", d.getIntValue("f13"));
            s.put("price", round2(price));
            s.put("changePct", round2(pct));
            s.put("industry", d.getString("f100"));
            s.put("circCap", round2(circCap));
            Double totalCap = asDouble(d.get("f20"));
            s.put("totalCap", totalCap == null ? null : round2(totalCap / 1e8));
            s.put("turnoverRate", round2(turnover));
            s.put("volumeRatio", vr == null ? null : round2(vr));
            s.put("mainInflow", mainInflow == null ? null : round2(mainInflow / 1e8));
            s.put("mainInflowPct", mainPct == null ? null : round2(mainPct));
            Double inner = asDouble(d.get("f47"));
            Double outer = asDouble(d.get("f48"));
            s.put("inner", inner);
            s.put("outer", outer);
            s.put("outerInnerRatio", (inner == null || outer == null || inner <= 0) ? null
                    : round2(outer / inner));
            s.put("ytdPct", round2(ytdPct));
            s.put("concept", trimConcept(d.getString("f103")));
            out.add(s);
        }
    }

    /** 概念串截断：取前5个概念，总长不超200字符（对应宽表concept列varchar(200)） */
    private String trimConcept(String raw) {
        if (raw == null || raw.isEmpty() || "-".equals(raw)) return null;
        String[] arr = raw.split(",");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < arr.length && i < 5; i++) {
            if (sb.length() > 0) sb.append(',');
            sb.append(arr[i]);
            if (sb.length() >= 200) break;
        }
        return sb.length() > 200 ? sb.substring(0, 200) : sb.toString();
    }

    /**
     * 池票（妖股）批量补快照：涨停/炸板池接口无主力资金/换手/量比/总市值/概念，
     * 用 ulist.np/get 按代码批量查询（每批60只，一次请求）补齐宽表字段。
     * 失败仅告警降级（字段留空），不影响主流程，也不触发clist熔断（不同接口）。
     */
    private void enrichPoolSnapshot(Collection<Map<String, Object>> poolStocks, List<String> degradeReasons) {
        List<Map<String, Object>> stocks = new ArrayList<>();
        for (Map<String, Object> s : poolStocks) {
            if (s.get("mainInflowPct") == null) stocks.add(s); // 只补缺失的
        }
        if (stocks.isEmpty()) return;
        for (int from = 0; from < stocks.size(); from += ULIST_BATCH) {
            List<Map<String, Object>> batch = stocks.subList(from, Math.min(from + ULIST_BATCH, stocks.size()));
            StringBuilder secids = new StringBuilder();
            for (Map<String, Object> s : batch) {
                if (secids.length() > 0) secids.append(',');
                secids.append(marketInt((String) s.get("code"))).append('.').append((String) s.get("code"));
            }
            boolean ok = false;
            for (String host : CLIST_HOSTS) {
                String body = httpGetNoRetry(host + String.format(ULIST_PATH, secids));
                if (body == null) continue;
                try {
                    JSONObject data = JSON.parseObject(body).getJSONObject("data");
                    JSONArray diff = toDiffArray(data == null ? null : data.get("diff"));
                    if (diff == null || diff.isEmpty()) continue;
                    Map<String, JSONObject> byCode = new HashMap<>();
                    for (int i = 0; i < diff.size(); i++) {
                        JSONObject d = diff.getJSONObject(i);
                        String code = d.getString("f12");
                        if (code != null) byCode.put(code, d);
                    }
                    for (Map<String, Object> s : batch) {
                        JSONObject d = byCode.get((String) s.get("code"));
                        if (d == null) continue;
                        Double mainInflow = asDouble(d.get("f62"));
                        Double mainPct = asDouble(d.get("f184"));
                        Double totalCap = asDouble(d.get("f20"));
                        Double vr = asDouble(d.get("f10")); // ulist口径：量比=f10
                        s.put("mainInflow", mainInflow == null ? null : round2(mainInflow / 1e8));
                        s.put("mainInflowPct", mainPct);
                        s.put("totalCap", totalCap == null ? null : round2(totalCap / 1e8));
                        if (asDouble(s.get("turnoverRate")) == null) s.put("turnoverRate", asDouble(d.get("f8")));
                        if (asDouble(s.get("volumeRatio")) == null) s.put("volumeRatio", vr == null ? null : round2(vr));
                        if (s.get("industry") == null || "-".equals(s.get("industry"))) s.put("industry", d.getString("f100"));
                        if (s.get("concept") == null) s.put("concept", trimConcept(d.getString("f103")));
                    }
                    ok = true;
                    break;
                } catch (Exception e) {
                    logger.warn("池票快照补充解析失败 host={}", host, e);
                }
            }
            if (!ok) {
                degradeReasons.add("妖股快照补充失败（ulist异常），部分妖股资金/换手字段缺失");
            }
            try {
                Thread.sleep(200); // 批间限速防反爬
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    // ================================================================
    // K线抓取（东财单次→熔断→腾讯兜底）
    // ================================================================
    /** 单根日K：date + open,close,high,low,volume */
    static class Bar {
        String date;
        double open, close, high, low, volume;
    }

    /** 并发拉取60日日K（4线程×每任务150ms），东财失败即熔断并改走腾讯兜底 */
    private Map<String, List<Bar>> fetchKlines(Map<String, Integer> targets) {
        Map<String, List<Bar>> map = new ConcurrentHashMap<>();
        ExecutorService es = Executors.newFixedThreadPool(4);
        try {
            List<Callable<Void>> tasks = new ArrayList<>();
            for (Map.Entry<String, Integer> e : targets.entrySet()) {
                tasks.add(() -> {
                    try {
                        List<Bar> bars = fetchKlineEast(e.getKey(), e.getValue());
                        if (bars == null) bars = fetchKlineTx(e.getKey(), e.getValue());
                        if (bars != null && !bars.isEmpty()) map.put(e.getKey(), bars);
                    } catch (Exception ignore) {
                        // 单股失败不影响整体，该股按K线缺失处理
                    }
                    Thread.sleep(150);
                    return null;
                });
            }
            es.invokeAll(tasks);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            es.shutdown();
        }
        return map;
    }

    /** 东财K线（单次不重试，防延长拉黑时长）；失败/空数据即触发5分钟熔断 */
    private List<Bar> fetchKlineEast(String code, int market) {
        if (eastKlineBlocked()) return null;
        try {
            String resp = httpGetNoRetry(String.format(EAST_KLINE_URL_TPL, market + "." + code));
            if (resp == null) {
                markEastKlineFail();
                return null;
            }
            JSONObject data = JSON.parseObject(resp).getJSONObject("data");
            JSONArray ks = data == null ? null : data.getJSONArray("klines");
            if (ks == null || ks.isEmpty()) {
                markEastKlineFail();
                return null;
            }
            return parseEastBars(ks);
        } catch (Exception e) {
            markEastKlineFail();
            return null;
        }
    }

    private List<Bar> parseEastBars(JSONArray ks) {
        List<Bar> bars = new ArrayList<>(ks.size());
        for (int i = 0; i < ks.size(); i++) {
            String[] parts = ks.getString(i).split(",");
            Bar b = new Bar();
            b.date = parts[0];
            b.open = Double.parseDouble(parts[1]);
            b.close = Double.parseDouble(parts[2]);
            b.high = Double.parseDouble(parts[3]);
            b.low = Double.parseDouble(parts[4]);
            b.volume = Double.parseDouble(parts[5]);
            bars.add(b);
        }
        return bars;
    }

    /** 腾讯日K兜底（前复权，口径与东财一致） */
    private List<Bar> fetchKlineTx(String code, int market) {
        try {
            String mkt = market == 1 ? "sh" : "sz";
            String resp = httpGetNoRetry(TX_KLINE_URL + "?param=" + mkt + code + ",day,,,60,qfq");
            if (resp == null) return null;
            JSONObject node = JSON.parseObject(resp).getJSONObject("data").getJSONObject(mkt + code);
            if (node == null) return null;
            JSONArray days = node.getJSONArray("qfqday");
            if (days == null) days = node.getJSONArray("day");
            if (days == null || days.isEmpty()) return null;
            List<Bar> bars = new ArrayList<>(days.size());
            for (int i = 0; i < days.size(); i++) {
                JSONArray a = days.getJSONArray(i);
                Bar b = new Bar();
                b.date = a.getString(0);
                b.open = Double.parseDouble(a.getString(1));
                b.close = Double.parseDouble(a.getString(2));
                b.high = Double.parseDouble(a.getString(3));
                b.low = Double.parseDouble(a.getString(4));
                b.volume = a.size() > 5 ? Double.parseDouble(a.getString(5)) : 0;
                bars.add(b);
            }
            return bars;
        } catch (Exception e) {
            logger.warn("腾讯K线兜底失败 code={}", code);
            return null;
        }
    }

    // ================================================================
    // K线特征 / 行为判定 / 评分
    // ================================================================
    /** 计算60日K线特征：均线/量能/涨停基因/位置/区间涨幅 */
    private Map<String, Object> klineFeatures(List<Bar> bars) {
        Map<String, Object> f = new LinkedHashMap<>();
        int n = bars.size();
        double lastClose = bars.get(n - 1).close;
        f.put("ma5", n >= 5 ? round2(avgClose(bars, n - 5, n)) : null);
        f.put("ma10", n >= 10 ? round2(avgClose(bars, n - 10, n)) : null);
        f.put("ma20", n >= 20 ? round2(avgClose(bars, n - 20, n)) : null);
        boolean maBull = asDouble(f.get("ma5")) != null && asDouble(f.get("ma10")) != null && asDouble(f.get("ma20")) != null
                && asDouble(f.get("ma5")) > asDouble(f.get("ma10")) && asDouble(f.get("ma10")) > asDouble(f.get("ma20"));
        f.put("trendMaOk", maBull);
        double vol5 = avgVolume(bars, Math.max(0, n - 5), n);
        double vol20 = n >= 20 ? avgVolume(bars, n - 20, n) : vol5;
        f.put("volShrink", vol20 > 0 ? round2(vol5 / vol20) : null); // 5日均量/20日均量，<1为缩量
        // 涨停基因：60日内涨幅>=9.5%的天数（主板10%/创业科创20%口径下均保守有效）
        int ztCount = 0;
        Integer recentZtDaysAgo = null;
        for (int i = 1; i < n; i++) {
            double pct = (bars.get(i).close / bars.get(i - 1).close - 1) * 100;
            if (pct >= 9.5) {
                ztCount++;
                int daysAgo = n - 1 - i;
                if (recentZtDaysAgo == null || daysAgo < recentZtDaysAgo) recentZtDaysAgo = daysAgo;
            }
        }
        f.put("ztCount60", ztCount);
        f.put("recentZtDaysAgo", (recentZtDaysAgo != null && recentZtDaysAgo <= 4) ? recentZtDaysAgo : null);
        // 低位深度：距60日最低点涨幅%
        double minLow = bars.get(0).low;
        for (Bar b : bars) minLow = Math.min(minLow, b.low);
        f.put("lowDepth", minLow > 0 ? round2((lastClose / minLow - 1) * 100) : null);
        // 区间涨幅
        f.put("gain5", n >= 6 ? round2((lastClose / bars.get(n - 6).close - 1) * 100) : null);
        f.put("gain20", n >= 21 ? round2((lastClose / bars.get(n - 21).close - 1) * 100) : null);
        // 近20日平台高点（潜伏突破确认位）
        if (n >= 21) {
            double platformHigh = 0;
            for (int i = n - 20; i < n; i++) platformHigh = Math.max(platformHigh, bars.get(i).high);
            f.put("platformHigh", round2(platformHigh));
        } else {
            f.put("platformHigh", null);
        }
        return f;
    }

    /** 把K线特征合并进股票结构（供展示与评分） */
    private void mergeFeatures(Map<String, Object> s, Map<String, Object> f) {
        s.put("ma5", f.get("ma5"));
        s.put("ma10", f.get("ma10"));
        s.put("ma20", f.get("ma20"));
        s.put("volShrink", f.get("volShrink"));
        s.put("ztCount60", f.get("ztCount60"));
        s.put("recentZtDaysAgo", f.get("recentZtDaysAgo"));
        s.put("lowDepth", f.get("lowDepth"));
        s.put("gain5", f.get("gain5"));
        s.put("gain20", f.get("gain20"));
        s.put("platformHigh", f.get("platformHigh"));
        s.put("trendMaOk", Boolean.TRUE.equals(f.get("trendMaOk")) ? 1 : 0);
    }

    /** 妖股池股票的K线增强（不改变池内原始字段，仅补充特征） */
    private void enrichKlineFeatures(Map<String, Object> s, List<Bar> bars) {
        if (bars == null || bars.size() < 25) {
            s.put("ztCount60", 0);
            s.put("trendMaOk", 0);
            return;
        }
        mergeFeatures(s, klineFeatures(bars));
    }

    /**
     * 行为判定矩阵（0增量请求，全部来自池/快照/K线已采集事实）：
     * pull_up继续拉升 / absorb吸筹 / organize强势整理 / distribute出货嫌疑 / unknown无法判定
     */
    private void judgeBehavior(Map<String, Object> s) {
        boolean limitUpNow = Boolean.TRUE.equals(s.get("limitUpNow"));
        int zhaban = (int) s.getOrDefault("zhabanCount", 0);
        Double sealRatio = asDouble(s.get("sealRatio"));
        Double mainPct = asDouble(s.get("mainInflowPct"));
        Double mainIn = asDouble(s.get("mainInflow"));
        Double ratio = asDouble(s.get("outerInnerRatio"));
        Double pct = asDouble(s.get("changePct"));
        Double volShrink = asDouble(s.get("volShrink"));
        Double volumeRatio = asDouble(s.get("volumeRatio"));
        boolean outerDominant = ratio != null && ratio > 1.1;
        boolean innerDominant = ratio != null && ratio < 0.9;
        boolean mainPositive = mainIn != null && mainIn > 0;
        boolean shrink = (volShrink != null && volShrink < 0.9) || (volumeRatio != null && volumeRatio < 0.9);
        boolean maBull = Integer.valueOf(1).equals(s.get("trendMaOk"));

        String behavior;
        List<String> ev = new ArrayList<>();

        if (limitUpNow) {
            if (zhaban >= 2 || (innerDominant && mainPct != null && mainPct < 0)) {
                behavior = "distribute";
                ev.add("涨停但炸板" + zhaban + "次");
                if (innerDominant) ev.add("内盘>外盘");
                if (mainPct != null && mainPct < 0) ev.add(String.format("主力净流出占成交%.1f%%", mainPct));
            } else if (zhaban == 0 && (sealRatio != null && sealRatio >= 2 || outerDominant) && mainPositive) {
                behavior = "pull_up";
                ev.add("涨停封死一次未开");
                if (sealRatio != null) ev.add(String.format("封单占流通市值%.2f%%", sealRatio));
                if (outerDominant) ev.add(String.format("外盘/内盘%.2f", ratio));
                if (mainIn != null) ev.add(String.format("主力净流入%.2f亿", mainIn));
            } else {
                behavior = "pull_up";
                ev.add("涨停封板");
                if (zhaban > 0) ev.add("炸板" + zhaban + "次后回封");
                if (sealRatio != null) ev.add(String.format("封单占流通%.2f%%（偏弱）", sealRatio));
            }
        } else if (zhaban > 0) {
            if (innerDominant && mainPct != null && mainPct < 0) {
                behavior = "distribute";
                ev.add("炸板" + zhaban + "次未回封");
                ev.add("内盘>外盘");
                ev.add(String.format("主力净流出占成交%.1f%%", mainPct));
            } else {
                behavior = "organize";
                ev.add("炸板" + zhaban + "次（分歧）");
                if (mainPositive) ev.add(String.format("主力仍净流入%.2f亿", mainIn));
                ev.add("待分歧转一致确认");
            }
        } else {
            if (mainPositive && outerDominant && (shrink || (pct != null && pct < 5))) {
                behavior = "absorb";
                ev.add(String.format("主力净流入%.2f亿占成交%.1f%%", mainIn, mainPct == null ? 0 : mainPct));
                if (outerDominant) ev.add(String.format("外盘/内盘%.2f主动买占优", ratio));
                if (shrink) ev.add("缩量");
            } else if (mainPct != null && mainPct <= -3 && (innerDominant || mainIn == null || mainIn < 0)) {
                behavior = "distribute";
                ev.add(String.format("主力净流出占成交%.1f%%", mainPct));
                if (innerDominant) ev.add("内盘>外盘");
            } else if (maBull && pct != null && Math.abs(pct) <= 4
                    && (shrink || (volumeRatio != null && volumeRatio < 1.2))) {
                behavior = "organize";
                ev.add("均线多头窄幅整理");
                ev.add(String.format("涨幅%.1f%%", pct));
                ev.add("量能温和");
            } else {
                behavior = "unknown";
                ev.add("量价信号不明确，无法判定");
            }
        }
        s.put("behavior", behavior);
        s.put("behaviorEvidence", String.join("；", ev));
    }

    /** 出货行为一票剔除 + 评分排序 + topN + 优先级理由/确认信号/风险提示 */
    private List<Map<String, Object>> scoreAndTop(Collection<Map<String, Object>> pool, String type, int topN) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (Map<String, Object> s : pool) {
            if ("distribute".equals(s.get("behavior"))) continue; // 出货嫌疑不入选
            s.put("score", round2(calcScore(s, type)));
            list.add(s);
        }
        list.sort((a, b) -> {
            Double sa = asDouble(a.get("score"));
            Double sb = asDouble(b.get("score"));
            return Double.compare(sb == null ? 0 : sb, sa == null ? 0 : sa);
        });
        List<Map<String, Object>> top = new ArrayList<>(list.subList(0, Math.min(topN, list.size())));
        int pri = 1;
        for (Map<String, Object> s : top) {
            s.put("priority", pri++);
            s.put("priorityReason", buildReason(s, type));
            s.put("confirmSignal", buildConfirm(s, type));
            s.put("riskNote", buildRisk(type));
        }
        return top;
    }

    private double calcScore(Map<String, Object> s, String type) {
        Integer ztCount60 = (Integer) s.getOrDefault("ztCount60", 0);
        Double sealRatio = asDouble(s.get("sealRatio"));
        Double mainPct = asDouble(s.get("mainInflowPct"));
        Double volShrink = asDouble(s.get("volShrink"));
        Double lowDepth = asDouble(s.get("lowDepth"));
        Double gain20 = asDouble(s.get("gain20"));
        Double circCap = asDouble(s.get("circCap"));
        String behavior = (String) s.get("behavior");
        double score = 0;

        if ("yaogu".equals(type)) {
            Integer lianban = (Integer) s.getOrDefault("lianban", 0);
            score = lianban * 3.0
                    + (sealRatio == null ? 0 : Math.min(sealRatio, 5))
                    + Math.min(ztCount60, 6) * 0.5;
            if (circCap != null && circCap >= 20 && circCap <= 200) score += 2;     // 中小盘弹性
            else if (circCap != null && circCap <= 500) score += 1;
            if ("pull_up".equals(behavior)) score += 3;
            else if ("organize".equals(behavior)) score += 1;
        } else if ("qianfu".equals(type)) {
            if (lowDepth != null) score += Math.max(0, 28 - lowDepth) * 0.25;       // 低位深度 0~7
            if (volShrink != null) score += Math.max(0, 1 - volShrink) * 6;         // 缩量程度 0~6
            score += Math.min(ztCount60, 4) * 1.2;                                  // 涨停基因 0~4.8
            if (mainPct != null) score += clamp(mainPct, 0, 10) * 0.3;              // 主力流入占比 0~3
            if (gain20 != null && gain20 >= -8 && gain20 <= 12) score += 2;         // 平台未启动
            if ("absorb".equals(behavior)) score += 4;
            else if ("organize".equals(behavior)) score += 2;
            else if ("pull_up".equals(behavior)) score += 1;
        } else { // qushi
            Integer recentZt = (Integer) s.get("recentZtDaysAgo");
            boolean maBull = Integer.valueOf(1).equals(s.get("trendMaOk"));
            score = maBull ? 3 : 0;
            if (recentZt != null) {
                double[] ztScore = {4, 3.2, 2.4, 1.6, 0.8};
                score += recentZt < 5 ? ztScore[recentZt] : 0;                      // 涨停新近度
            }
            if (volShrink != null && volShrink >= 0.6 && volShrink <= 1.0) score += 2; // 缩量健康
            if (lowDepth != null && lowDepth >= 15 && lowDepth <= 60) score += 1.5; // 空间位置
            if (gain20 != null) score += clamp(gain20, 0, 25) * 0.1;
            if (mainPct != null) score += clamp(mainPct, 0, 8) * 0.25;
            if ("pull_up".equals(behavior)) score += 3;
            else if ("organize".equals(behavior)) score += 2;
            else if ("absorb".equals(behavior)) score += 1;
        }
        return score;
    }

    private String buildReason(Map<String, Object> s, String type) {
        String behaviorText = behaviorText((String) s.get("behavior"));
        if ("yaogu".equals(type)) {
            return String.format("连板高度%d板，60日涨停%d次具备股性，%s，行为判定=%s",
                    (int) s.getOrDefault("lianban", 0),
                    (int) s.getOrDefault("ztCount60", 0),
                    asDouble(s.get("sealRatio")) == null ? "封单数据缺失" : String.format("封单占流通%.2f%%", asDouble(s.get("sealRatio"))),
                    behaviorText);
        } else if ("qianfu".equals(type)) {
            return String.format("距60日低点仅%.1f%%处缩量整理（5日/20日均量比%s），60日涨停%d次股性活跃，主力净占比%s%%，行为判定=%s",
                    asDouble(s.get("lowDepth")) == null ? 0 : asDouble(s.get("lowDepth")),
                    asDouble(s.get("volShrink")) == null ? "-" : String.format("%.2f", asDouble(s.get("volShrink"))),
                    (int) s.getOrDefault("ztCount60", 0),
                    asDouble(s.get("mainInflowPct")) == null ? "-" : String.format("%.1f", asDouble(s.get("mainInflowPct"))),
                    behaviorText);
        }
        return String.format("近5日出现涨停（距今%s日），%s，近20日涨%.1f%%趋势向上，行为判定=%s",
                s.get("recentZtDaysAgo") == null ? "-" : s.get("recentZtDaysAgo"),
                Integer.valueOf(1).equals(s.get("trendMaOk")) ? "MA5>MA10>MA20多头排列" : "短中期均线向上",
                asDouble(s.get("gain20")) == null ? 0 : asDouble(s.get("gain20")),
                behaviorText);
    }

    private String buildConfirm(Map<String, Object> s, String type) {
        if ("yaogu".equals(type)) {
            return asDouble(s.get("ma5")) == null
                    ? "次日分时不破均价线可持有，破5日线止损"
                    : String.format("次日分时不破均价线可持有，跌破5日线(%.2f元)止损", asDouble(s.get("ma5")));
        } else if ("qianfu".equals(type)) {
            return asDouble(s.get("platformHigh")) == null
                    ? "放量突破近期平台高点后介入，缩量回踩止跌为观察点"
                    : String.format("放量突破近20日平台高点%.2f元后介入，缩量回踩止跌可轻仓潜伏", asDouble(s.get("platformHigh")));
        }
        return asDouble(s.get("ma5")) == null
                ? "回踩5日线缩量企稳介入，跌破10日线离场"
                : String.format("回踩5日线(%.2f元)缩量企稳介入，跌破10日线离场", asDouble(s.get("ma5")));
    }

    private String buildRisk(String type) {
        if ("yaogu".equals(type)) return "高位连板股波动极大，断板/炸板/大幅高开回落需立即执行离场纪律";
        if ("qianfu".equals(type)) return "潜伏等待启动时间不确定，跌破60日低点或放量大阴需止损";
        return "趋势破位（跌破10日线或放量长阴）需及时离场，避免追高重仓";
    }

    private String behaviorText(String b) {
        if (b == null) return "无法判定";
        switch (b) {
            case "pull_up": return "继续拉升";
            case "absorb": return "吸筹";
            case "organize": return "强势整理";
            case "distribute": return "出货嫌疑";
            default: return "无法判定";
        }
    }

    // ================================================================
    // 落库与输出组装
    // ================================================================
    private List<ShortTermPickDaily> toRows(List<Map<String, Object>> top, String tradeDate, String phase, String type) {
        List<ShortTermPickDaily> rows = new ArrayList<>();
        for (Map<String, Object> s : top) {
            ShortTermPickDaily r = new ShortTermPickDaily();
            r.setTradeDate(java.sql.Date.valueOf(tradeDate));
            r.setPickPhase(phase);
            r.setPickType(type);
            r.setPriority((Integer) s.get("priority"));
            r.setStockCode((String) s.get("code"));
            r.setStockName((String) s.get("name"));
            r.setMarket(marketText((String) s.get("code")));
            r.setIndustry((String) s.get("industry"));
            r.setConcept((String) s.get("concept"));
            r.setPrice(toBd(s.get("price")));
            r.setChangePct(toBd(s.get("changePct")));
            r.setTotalCap(toBd(s.get("totalCap")));
            r.setCircCap(toBd(s.get("circCap")));
            r.setTurnoverRate(toBd(s.get("turnoverRate")));
            r.setVolumeRatio(toBd(s.get("volumeRatio")));
            r.setMainInflow(toBd(s.get("mainInflow")));
            r.setMainInflowPct(toBd(s.get("mainInflowPct")));
            r.setOuterInnerRatio(toBd(s.get("outerInnerRatio")));
            r.setLianban((Integer) s.get("lianban"));
            r.setSealRatio(toBd(s.get("sealRatio")));
            r.setZhabanCount((Integer) s.get("zhabanCount"));
            r.setZtTimeFirst((String) s.get("ztTimeFirst"));
            r.setZtCount60d((Integer) s.get("ztCount60"));
            r.setLowDepth(toBd(s.get("lowDepth")));
            r.setVolShrink(toBd(s.get("volShrink")));
            r.setTrendMaOk((Integer) s.get("trendMaOk"));
            r.setGain5d(toBd(s.get("gain5")));
            r.setGain20d(toBd(s.get("gain20")));
            r.setScore(toBd(s.get("score")));
            r.setBehavior((String) s.get("behavior"));
            r.setBehaviorEvidence((String) s.get("behaviorEvidence"));
            r.setPriorityReason((String) s.get("priorityReason"));
            r.setConfirmSignal((String) s.get("confirmSignal"));
            r.setRiskNote((String) s.get("riskNote"));
            rows.add(r);
        }
        return rows;
    }

    private Map<String, Object> groupOf(String type, String typeText, List<Map<String, Object>> stocks) {
        Map<String, Object> g = new LinkedHashMap<>();
        g.put("type", type);
        g.put("typeText", typeText);
        g.put("stocks", stocks);
        return g;
    }

    /** 库直读组装：宽表行 → 前端结构（与实时计算字段对齐） */
    private Map<String, Object> assembleFromDb(String tradeDate, List<ShortTermPickDaily> rows) {
        List<Map<String, Object>> yaogu = new ArrayList<>();
        List<Map<String, Object>> qianfu = new ArrayList<>();
        List<Map<String, Object>> qushi = new ArrayList<>();
        String phase = "close";
        for (ShortTermPickDaily r : rows) {
            phase = r.getPickPhase() == null ? phase : r.getPickPhase();
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("code", r.getStockCode());
            m.put("name", r.getStockName());
            m.put("industry", r.getIndustry());
            m.put("market", r.getMarket());
            m.put("price", r.getPrice());
            m.put("changePct", r.getChangePct());
            m.put("totalCap", r.getTotalCap());
            m.put("circCap", r.getCircCap());
            m.put("turnoverRate", r.getTurnoverRate());
            m.put("volumeRatio", r.getVolumeRatio());
            m.put("mainInflow", r.getMainInflow());
            m.put("mainInflowPct", r.getMainInflowPct());
            m.put("outerInnerRatio", r.getOuterInnerRatio());
            m.put("lianban", r.getLianban());
            m.put("sealRatio", r.getSealRatio());
            m.put("zhabanCount", r.getZhabanCount());
            m.put("ztTimeFirst", r.getZtTimeFirst());
            m.put("ztCount60", r.getZtCount60d());
            m.put("lowDepth", r.getLowDepth());
            m.put("volShrink", r.getVolShrink());
            m.put("trendMaOk", r.getTrendMaOk());
            m.put("gain5", r.getGain5d());
            m.put("gain20", r.getGain20d());
            m.put("score", r.getScore());
            m.put("behavior", r.getBehavior());
            m.put("behaviorEvidence", r.getBehaviorEvidence());
            m.put("priority", r.getPriority());
            m.put("priorityReason", r.getPriorityReason());
            m.put("confirmSignal", r.getConfirmSignal());
            m.put("riskNote", r.getRiskNote());
            if ("yaogu".equals(r.getPickType())) yaogu.add(m);
            else if ("qianfu".equals(r.getPickType())) qianfu.add(m);
            else if ("qushi".equals(r.getPickType())) qushi.add(m);
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("tradeDate", tradeDate);
        out.put("phase", phase);
        out.put("phaseText", "intraday".equals(phase) ? "盘中实时推荐" : "收盘后推荐");
        out.put("fromDb", true);
        out.put("fromCache", false);
        out.put("degraded", false);
        out.put("degradeReasons", new ArrayList<>());
        out.put("saved", true);
        out.put("groups", Arrays.asList(
                groupOf("yaogu", "妖股梯队", yaogu),
                groupOf("qianfu", "低位潜伏", qianfu),
                groupOf("qushi", "趋势延续", qushi)));
        out.put("stats", Collections.singletonMap("source", "db"));
        return out;
    }

    // ================================================================
    // 经验闭环：T+3/T+5 K线验证 + AI复盘入库
    // ================================================================
    @Override
    public Map<String, Object> reviewShortTermPicks() {
        Map<String, Object> out = new LinkedHashMap<>();
        Date today = dayStart(new Date());
        Date maxDate = addDays(today, -3);
        Date minDate = addDays(today, -10);
        List<ShortTermPickDaily> picks = shortTermPickMapper.selectPicksForReview(minDate, maxDate);
        if (picks.isEmpty()) {
            out.put("reviewed", 0);
            out.put("message", "暂无待复盘推荐（推荐3个交易日后自动可复盘，历史推荐均已生成经验）");
            return out;
        }
        // 控制K线请求预算：最多复盘30只
        List<ShortTermPickDaily> targets = picks.subList(0, Math.min(30, picks.size()));
        Map<String, Integer> klineTargets = new LinkedHashMap<>();
        for (ShortTermPickDaily p : targets) {
            klineTargets.putIfAbsent(p.getStockCode(), marketInt(p.getStockCode()));
        }
        Map<String, List<Bar>> klines = fetchKlines(klineTargets);

        SimpleDateFormat dayFmt = new SimpleDateFormat("yyyy-MM-dd");
        List<ShortTermExperience> exps = new ArrayList<>();
        List<Map<String, Object>> details = new ArrayList<>();
        for (ShortTermPickDaily p : targets) {
            Map<String, Object> d = new LinkedHashMap<>();
            d.put("code", p.getStockCode());
            d.put("name", p.getStockName());
            d.put("pickType", p.getPickType());
            d.put("tradeDate", dayFmt.format(p.getTradeDate()));
            List<Bar> bars = klines.get(p.getStockCode());
            if (bars == null || bars.size() < 5) {
                d.put("skipped", "K线数据缺失");
                details.add(d);
                continue;
            }
            // 推荐日之后的bar窗口
            String td = dayFmt.format(p.getTradeDate());
            int idx = -1;
            for (int i = 0; i < bars.size(); i++) {
                if (bars.get(i).date.compareTo(td) > 0) {
                    idx = i;
                    break;
                }
            }
            if (idx < 0 || idx + 3 > bars.size()) {
                d.put("skipped", "推荐后K线不足3根，尚未到T+3");
                details.add(d);
                continue;
            }
            double price0 = p.getPrice() != null ? p.getPrice().doubleValue()
                    : (idx > 0 ? bars.get(idx - 1).close : bars.get(idx).open);
            int window = 3;
            Map<String, Object> fact = windowFact(bars, idx, window, price0);
            String expType = classifyExp(fact);
            if (expType == null && idx + 5 <= bars.size()) {
                window = 5;
                fact = windowFact(bars, idx, window, price0);
                expType = classifyExp(fact);
            }
            if (expType == null) {
                d.put("skipped", "走势中性，不生成经验");
                details.add(d);
                continue;
            }
            ShortTermExperience exp = new ShortTermExperience();
            exp.setExpDate(today);
            exp.setExpType(expType);
            exp.setPickType(p.getPickType());
            exp.setTradeDate(p.getTradeDate());
            exp.setStockCode(p.getStockCode());
            exp.setStockName(p.getStockName());
            exp.setResultBrief(truncate(String.format("推荐价%.2f元，推荐后%d个交易日最高%+.1f%%、最低%+.1f%%、期末%+.1f%%，期间涨停%d次",
                    price0, window,
                    asDouble(fact.get("maxHighPct")) == null ? 0 : asDouble(fact.get("maxHighPct")),
                    asDouble(fact.get("minLowPct")) == null ? 0 : asDouble(fact.get("minLowPct")),
                    asDouble(fact.get("endPct")) == null ? 0 : asDouble(fact.get("endPct")),
                    (int) fact.getOrDefault("ztCount", 0)), 500));
            exps.add(exp);
            d.put("expType", expType);
            d.put("resultBrief", exp.getResultBrief());
            details.add(d);
        }

        if (exps.isEmpty()) {
            out.put("reviewed", 0);
            out.put("details", details);
            out.put("message", "本轮无满足归类条件的推荐（其余为K线缺失/未到T+3/走势中性）");
            return out;
        }

        // AI批量复盘（一次调用）；失败时规则模板兜底
        String aiRaw = callReviewAi(exps, dayFmt);
        for (ShortTermExperience exp : exps) {
            if (aiRaw != null && !aiRaw.isEmpty()) {
                exp.setAiRaw(truncate(aiRaw, 2000));
            }
            String[] fallback = fallbackExp(exp.getExpType());
            exp.setSummary(fallback[0]);
            exp.setRuleHint(fallback[1]);
        }
        if (aiRaw != null && !aiRaw.isEmpty()) {
            mergeAiReview(exps, aiRaw, dayFmt);
        }
        int saved = 0;
        try {
            for (ShortTermExperience exp : exps) {
                shortTermPickMapper.insertExperience(exp);
                saved++;
            }
        } catch (Exception e) {
            logger.error("复盘经验入库失败", e);
        }
        out.put("reviewed", saved);
        out.put("aiUsed", aiRaw != null && !aiRaw.isEmpty());
        out.put("details", details);
        return out;
    }

    /** 推荐后window根bar的事实统计（相对推荐价） */
    private Map<String, Object> windowFact(List<Bar> bars, int idx, int window, double price0) {
        double maxHigh = -Double.MAX_VALUE, minLow = Double.MAX_VALUE;
        int ztCount = 0;
        double prevClose = idx > 0 ? bars.get(idx - 1).close : price0;
        for (int i = idx; i < idx + window && i < bars.size(); i++) {
            Bar b = bars.get(i);
            maxHigh = Math.max(maxHigh, b.high);
            minLow = Math.min(minLow, b.low);
            if (prevClose > 0 && (b.close / prevClose - 1) * 100 >= 9.5) ztCount++;
            prevClose = b.close;
        }
        double endClose = bars.get(Math.min(idx + window, bars.size()) - 1).close;
        Map<String, Object> f = new LinkedHashMap<>();
        f.put("maxHighPct", round2((maxHigh / price0 - 1) * 100));
        f.put("minLowPct", round2((minLow / price0 - 1) * 100));
        f.put("endPct", round2((endClose / price0 - 1) * 100));
        f.put("ztCount", ztCount);
        return f;
    }

    /** 事实归类：limit_up > success > crash > fake_out > no_start */
    private String classifyExp(Map<String, Object> fact) {
        Double maxHigh = asDouble(fact.get("maxHighPct"));
        Double minLow = asDouble(fact.get("minLowPct"));
        Double endPct = asDouble(fact.get("endPct"));
        int ztCount = (int) fact.getOrDefault("ztCount", 0);
        if (maxHigh == null || minLow == null || endPct == null) return null;
        if (ztCount >= 1) return "limit_up";
        if (maxHigh >= 8 && endPct >= 3) return "success";
        if (minLow <= -8) return "crash";
        if (maxHigh >= 5 && endPct < 0) return "fake_out";
        if (maxHigh < 3 && endPct < 1) return "no_start";
        return null;
    }

    /** AI批量复盘：事实列表 → 每条summary+ruleHint */
    private String callReviewAi(List<ShortTermExperience> exps, SimpleDateFormat dayFmt) {
        try {
            JSONArray arr = new JSONArray();
            for (ShortTermExperience e : exps) {
                JSONObject o = new JSONObject();
                o.put("code", e.getStockCode());
                o.put("name", e.getStockName());
                o.put("pickType", e.getPickType());
                o.put("tradeDate", dayFmt.format(e.getTradeDate()));
                o.put("expType", e.getExpType());
                o.put("facts", e.getResultBrief());
                arr.add(o);
            }
            String sys = "你是A股短线交易复盘教练。基于给定的事实数据做经验沉淀，语气客观克制，不夸大、不臆测未提供的信息。输出必须是严格的JSON数组，不要输出任何其他文字或markdown代码块标记。";
            String user = "以下是若干条\"历史短线推荐→实际走势\"的事实记录（expType为程序判定归类：limit_up晋级/success按信号启动/crash崩车/fake_out假强/no_start不启动）。\n"
                    + "请对每一条给出经验总结summary（60字内）与可执行的修正规则ruleHint（40字内，供后续推荐算法参考，如\"低位潜伏需等放量突破再纳入\"\"封单弱的高位板次日谨慎\"）。\n"
                    + "仅输出JSON数组：[{\"code\":\"...\",\"tradeDate\":\"yyyy-MM-dd\",\"summary\":\"...\",\"ruleHint\":\"...\"}]\n"
                    + "输入数据：\n" + arr.toJSONString();
            String resp = aiCommonUtil.callWithSystem(sys, user);
            if (resp == null || resp.trim().isEmpty()) {
                logger.warn("短线复盘AI返回为空，使用规则模板兜底");
                return null;
            }
            return resp;
        } catch (Exception e) {
            logger.warn("短线复盘AI调用失败，使用规则模板兜底：{}", e.getMessage());
            return null;
        }
    }

    /** 解析AI返回并合并到经验（仅覆盖能对上的条目） */
    private void mergeAiReview(List<ShortTermExperience> exps, String aiRaw, SimpleDateFormat dayFmt) {
        try {
            int start = aiRaw.indexOf('[');
            int end = aiRaw.lastIndexOf(']');
            if (start < 0 || end <= start) return;
            JSONArray arr = JSON.parseArray(aiRaw.substring(start, end + 1));
            Map<String, JSONObject> byKey = new HashMap<>();
            for (int i = 0; i < arr.size(); i++) {
                JSONObject o = arr.getJSONObject(i);
                byKey.put(o.getString("code") + "|" + o.getString("tradeDate"), o);
            }
            for (ShortTermExperience e : exps) {
                JSONObject o = byKey.get(e.getStockCode() + "|" + dayFmt.format(e.getTradeDate()));
                if (o == null) continue;
                if (o.getString("summary") != null && !o.getString("summary").isEmpty()) {
                    e.setSummary(truncate(o.getString("summary"), 1000));
                }
                if (o.getString("ruleHint") != null && !o.getString("ruleHint").isEmpty()) {
                    e.setRuleHint(truncate(o.getString("ruleHint"), 500));
                }
            }
        } catch (Exception e) {
            logger.warn("AI复盘结果解析失败，保留规则模板：{}", e.getMessage());
        }
    }

    /** AI失败时的规则模板兜底 */
    private String[] fallbackExp(String expType) {
        switch (expType) {
            case "limit_up":
                return new String[]{"推荐后晋级涨停，妖股/趋势逻辑兑现，确认信号有效。",
                        "该类特征（封板质量/涨停基因/题材联动）后续可加权。"};
            case "success":
                return new String[]{"推荐后按确认信号启动，走势符合预期。",
                        "保留该类特征权重，确认信号（放量突破/回踩企稳）可延续使用。"};
            case "crash":
                return new String[]{"推荐后大幅下跌，趋势或情绪反转，风控未兜住。",
                        "加入大盘情绪过滤与硬止损位，弱势环境减少推荐频次。"};
            case "fake_out":
                return new String[]{"推荐后冲高回落，假强势拉升未获承接。",
                        "冲高5%%以上需次日不回落确认后再介入，重点看量能持续性。"};
            default:
                return new String[]{"推荐后未启动，横盘或阴跌，潜伏逻辑未获市场认可。",
                        "同类低位标的需等待放量信号出现再纳入，减少纯缩量潜伏。"};
        }
    }

    // ================================================================
    // 经验查询
    // ================================================================
    @Override
    public Map<String, Object> getExperience(Integer limit) {
        int n = (limit == null || limit <= 0) ? 20 : Math.min(limit, 100);
        List<ShortTermExperience> rows = shortTermPickMapper.selectRecentExperience(n);
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("list", rows);
        out.put("total", rows.size());
        return out;
    }

    // ================================================================
    // 通用工具
    // ================================================================
    private static String httpGetNoRetry(String url) {
        return httpGet(url, 1);
    }

    /** 带超时与重试的GET请求（K线/快照类调用方传1次，池类传3次） */
    private static String httpGet(String url, int maxAttempts) {
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try (CloseableHttpClient client = HttpClients.createDefault()) {
                HttpGet request = new HttpGet(url);
                request.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)");
                request.setConfig(RequestConfig.custom()
                        .setConnectTimeout(10000)
                        .setSocketTimeout(15000)
                        .build());
                try (CloseableHttpResponse response = client.execute(request)) {
                    if (response.getStatusLine().getStatusCode() == 200) {
                        return EntityUtils.toString(response.getEntity(), "UTF-8");
                    }
                    logger.warn("短线选股HTTP非200 | 状态={} | 尝试={}/{} | url={}",
                            response.getStatusLine().getStatusCode(), attempt, maxAttempts, url);
                }
            } catch (Exception e) {
                logger.warn("短线选股HTTP失败 | 尝试={}/{} | url={} | 原因={}", attempt, maxAttempts, url, e.getMessage());
            }
            if (attempt < maxAttempts) {
                sleep(500L * attempt);
            }
        }
        return null;
    }

    private static void markEastKlineFail() {
        eastKlineBlockedUntil = System.currentTimeMillis() + EAST_KLINE_BREAK_MS;
        logger.warn("短线选股东财K线首次失败，熔断5分钟：期间K线全部直走腾讯兜底");
    }

    private static boolean eastKlineBlocked() {
        return System.currentTimeMillis() < eastKlineBlockedUntil;
    }

    private static void markClistFail() {
        clistBlockedUntil = System.currentTimeMillis() + CLIST_BREAK_MS;
        logger.warn("短线选股行情快照双域名失败，熔断3分钟：期间clist请求跳过防延长封禁");
    }

    private static boolean clistBlocked() {
        return System.currentTimeMillis() < clistBlockedUntil;
    }

    /** clist diff兼容两种返回格式：数组 或 数字键对象 {"0":{...}} */
    private static JSONArray toDiffArray(Object diffObj) {
        if (diffObj instanceof JSONArray) return (JSONArray) diffObj;
        if (diffObj instanceof JSONObject) {
            JSONObject o = (JSONObject) diffObj;
            JSONArray arr = new JSONArray();
            for (int i = 0; i < o.size(); i++) {
                Object v = o.get(String.valueOf(i));
                if (v != null) arr.add(v);
            }
            return arr.isEmpty() ? null : arr;
        }
        return null;
    }

    /** 首次封板时间（push2ex fbt为HHMMSS数字串）→ HH:MM */
    private static String formatSealTime(String fbt) {
        if (fbt == null) return null;
        String digits = fbt.replaceAll("\\D", "");
        if (digits.isEmpty()) return null;
        while (digits.length() < 6) digits = "0" + digits;
        return digits.substring(0, 2) + ":" + digits.substring(2, 4);
    }

    private static boolean isBjCode(String code) {
        return code != null && (code.startsWith("83") || code.startsWith("87") || code.startsWith("88")
                || code.startsWith("43") || code.startsWith("92") || code.startsWith("4"));
    }

    private static int marketInt(String code) {
        return code != null && code.startsWith("6") ? 1 : 0;
    }

    private static String marketText(String code) {
        return code != null && code.startsWith("6") ? "sh" : "sz";
    }

    private static double avgClose(List<Bar> bars, int from, int to) {
        double sum = 0;
        for (int i = from; i < to; i++) sum += bars.get(i).close;
        return sum / (to - from);
    }

    private static double avgVolume(List<Bar> bars, int from, int to) {
        double sum = 0;
        for (int i = from; i < to; i++) sum += bars.get(i).volume;
        return sum / Math.max(1, to - from);
    }

    private static Double asDouble(Object v) {
        if (v == null) return null;
        if (v instanceof Number) return ((Number) v).doubleValue();
        try {
            String t = v.toString().trim();
            if (t.isEmpty() || "-".equals(t) || "null".equalsIgnoreCase(t)) return null;
            return Double.parseDouble(t);
        } catch (Exception e) {
            return null;
        }
    }

    private static double clamp(double v, double min, double max) {
        return Math.max(min, Math.min(max, v));
    }

    private static Double round2(Double v) {
        return v == null ? null : BigDecimal.valueOf(v).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    private static BigDecimal toBd(Object v) {
        Double d = asDouble(v);
        return d == null ? null : BigDecimal.valueOf(d).setScale(4, RoundingMode.HALF_UP);
    }

    private static String truncate(String s, int max) {
        if (s == null || s.length() <= max) return s;
        return s.substring(0, max);
    }

    private static Date dayStart(Date d) {
        Calendar c = Calendar.getInstance();
        c.setTime(d);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTime();
    }

    private static Date addDays(Date d, int days) {
        Calendar c = Calendar.getInstance();
        c.setTime(d);
        c.add(Calendar.DAY_OF_MONTH, days);
        return c.getTime();
    }

    private static void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
