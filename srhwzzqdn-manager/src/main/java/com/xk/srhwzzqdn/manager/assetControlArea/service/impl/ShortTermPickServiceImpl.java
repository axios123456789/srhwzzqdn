package com.xk.srhwzzqdn.manager.assetControlArea.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.xk.srhwzzqdn.manager.assetControlArea.mapper.ShortTermPickMapper;
import com.xk.srhwzzqdn.manager.assetControlArea.service.ShortTermPickService;
import com.xk.srhwzzqdn.manager.util.AiCommonUtil;
import com.xk.srhwzzqdn.manager.util.InterfaceConfigUtil;
import com.xk.srhwzzqdn.model.entity.assetControl.ShortTermExperience;
import com.xk.srhwzzqdn.model.entity.assetControl.ShortTermPickDaily;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

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

    // ===== 东财涨停/炸板池（与市场研判同源模板，URL走配置表 t_sys_comm_config） =====
    private static String getZtPoolUrlTpl() {
        String base = InterfaceConfigUtil.getUrl("market_zt_pool_url", "http://push2ex.eastmoney.com/getTopicZTPool");
        return base + "?ut=7eea3edcaed734bea9cbfc24409ed989&dpt=wz.ztzt&Pageindex=0&pagesize=600&sort=fbt%%3Aasc&date=%s";
    }
    private static String getZbPoolUrlTpl() {
        String base = InterfaceConfigUtil.getUrl("market_zb_pool_url", "http://push2ex.eastmoney.com/getTopicZBPool");
        return base + "?ut=7eea3edcaed734bea9cbfc24409ed989&dpt=wz.ztzt&Pageindex=0&pagesize=600&sort=fbt%%3Aasc&date=%s";
    }

    // ===== 行情快照clist：沪深A股，按主力净流入占比(f184)降序（f62净流入额会被大市值股霸榜，过滤后候选为0） =====
    // 注意：clist单页pz上限实测100，需翻页pn=1..3覆盖f184 top300（宽口径粗筛基数）
    // host走配置表（stock_clist_host_delay/stock_clist_host_main），DB异常用原写死值兜底
    private static String[] getClistHosts() {
        String delay = InterfaceConfigUtil.getUrl("stock_clist_host_delay", "https://push2delay.eastmoney.com");
        String main = InterfaceConfigUtil.getUrl("stock_clist_host_main", "https://push2.eastmoney.com");
        return new String[]{delay, main};
    }
    // 新浪全市场快照兜底URL（东财clist双域名均失败时使用）：提供换手率/流通市值/涨跌幅，无主力净流入/量比/行业/概念
    private static String getSinaHqNodeUrl() {
        return InterfaceConfigUtil.getUrl("stock_sina_hqnode_url",
                "https://vip.stock.finance.sina.com.cn/quotes_service/api/json_v2.php/Market_Center.getHQNodeData");
    }
    // 新浪批量行情兜底URL（东财ulist双域名均失败时使用）：提供价格/涨跌幅，无主力净流入/量比/行业/概念
    private static String getSinaQuoteUrl() {
        return InterfaceConfigUtil.getUrl("stock_sina_quote_url", "https://hq.sinajs.cn/list=");
    }
    // 腾讯批量行情兜底URL（东财ulist失败→腾讯→新浪三级兜底）：提供价格/涨跌幅/换手率/量比/总市值，无主力资金/行业/概念
    // 注意与stock_tx_quote_url（纯host，股票分析用base+"/q="+code）口径不同：本键直接拼代码，必须带/q=后缀
    private static String getTxQuoteUrl() {
        return InterfaceConfigUtil.getUrl("stock_tx_quote_batch_url", "https://qt.gtimg.cn/q=");
    }
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

    // ===== K线：60根日K（东财主源 + 腾讯兜底 + 新浪兜底，URL走配置表） =====
    private static String getEastKlineUrlTpl() {
        String base = InterfaceConfigUtil.getUrl("stock_kline_url", "http://push2his.eastmoney.com/api/qt/stock/kline/get");
        return base + "?secid=%s&klt=101&fqt=1&lmt=60&end=20500101&fields1=f1,f2,f3&fields2=f51,f52,f53,f54,f55,f56,f57";
    }
    private static String getTxKlineUrl() {
        return InterfaceConfigUtil.getUrl("stock_tx_kline_url", "https://ifzq.gtimg.cn/appstock/app/fqkline/get");
    }
    // 资金流日K兜底URL（push2his fflow，与K线同host家族共享GROUP_KLINE熔断视图）：当日主力净流入/净占比兜底用
    private static String getFflowDayUrlTpl() {
        String base = InterfaceConfigUtil.getUrl("stock_fflow_day_url", "https://push2his.eastmoney.com/api/qt/stock/fflow/daykline/get");
        return base + "?lmt=2&klt=101&fields1=f1,f2,f3,f7&fields2=f51,f52,f57&secid=%s";
    }
    // 新浪日K线兜底URL（东财/腾讯均失败时使用）：scale=240日K，datalen上限1023，volume单位是"股"
    private static String getSinaKlineUrl() {
        return InterfaceConfigUtil.getUrl("stock_sina_minute_url", "https://quotes.sina.cn/cn/api/jsonp_v2.php/var%20srhwTrend=");
    }

    // ===== 熔断器：全局统一存于 StockDataFetcher（GROUP_KLINE/GROUP_CLIST），与股票分析/市场分析共享同一份拉黑视图 =====
    // （拉黑期间该组请求全部跳过防延长封禁；K线5分钟/clist快照3分钟，到期自动恢复探测）

    // ===== 结果缓存：盘前/盘后30分钟，盘中10分钟（贴实时），熔断降级统一5分钟 =====
    private static final long CACHE_TTL_OK = 30 * 60 * 1000L;
    private static final long CACHE_TTL_INTRADAY = 10 * 60 * 1000L;
    private static final long CACHE_TTL_DEGRADED = 5 * 60 * 1000L;
    private final Map<String, Map<String, Object>> resultCache = new ConcurrentHashMap<>();

    /** 盘中推荐redis缓存（盘中数据不入库，纯缓存；正常30分钟/降级5分钟TTL，重启后盘中数据自然重算） */
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    private static final Object COMPUTE_LOCK = new Object();

    // 最近交易日解析缓存（按自然日失效；值≠今日且工作日时每5分钟重解析，防盘前解析出的"昨日"缓存卡住盘中判定）
    private static volatile String tradeDateCacheDay = "";
    private static volatile String tradeDateCacheValue = "";   // 最新bar日（盘中/盘后=今日，盘前/节假日=上一交易日）
    private static volatile String tradeDateCachePrev = "";    // 上一交易日（盘前bar提前生成时用）
    private static volatile long tradeDateCacheAt = 0L;

    // ================================================================
    // 主入口：三态时段状态机（盘前pre/盘中intraday/盘后close）
    // 盘前：直读上一交易日收盘版，没有则重算昨日收盘版并入库
    // 盘中：实时算法选股（不读库不写库，仅缓存10分钟）
    // 盘后：库有今日收盘版直读，没有则实时算法选今日股并入库
    // ================================================================
    @Override
    public Map<String, Object> getShortTermStocks() {
        String today = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        int hm = Integer.parseInt(new SimpleDateFormat("HHmm").format(new Date()));
        String[] td = resolveTradeDates();
        String latest = td[0];

        String tradeDate;
        String phase;
        if (!today.equals(latest)) {
            // 今日尚无K线bar：节假日/周末 或 交易日盘前（bar未生成）→ 沿用上一交易日
            tradeDate = latest;
            phase = "pre";
        } else if (isWeekday() && hm < 930) {
            // bar提前生成（集合竞价）但未开盘 → 仍按盘前，目标日=上一交易日
            tradeDate = td[1];
            phase = "pre";
        } else if (hm < 1500) {
            tradeDate = today;
            phase = "intraday";
        } else {
            tradeDate = today;
            phase = "close";
        }

        String cacheKey = tradeDate + "|" + phase;
        Map<String, Object> cached = resultCache.get(cacheKey);
        if (cached != null) {
            if (System.currentTimeMillis() - (long) cached.get("_cacheTime") < cacheTtl(phase, cached)) {
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

        // 盘中不进行任何库读写（既不查询也不删除），数据只存缓存里；
        // 盘前直读上一交易日已归库版本（没有则下方重算昨日收盘版并入库）；
        // 盘后仅直读今日收盘版（没有则重算今日），不会拿昨日数据顶替今日
        if (!"intraday".equals(phase)) {
            List<ShortTermPickDaily> rows = shortTermPickMapper.selectByTradeDate(java.sql.Date.valueOf(tradeDate));
            if (!rows.isEmpty()) {
                boolean hasClose = rows.stream().anyMatch(r -> "close".equals(r.getPickPhase()));
                // 盘前：上一交易日任意版本即沿用；盘后：必须有收盘版才直读
                if (hasClose || "pre".equals(phase)) {
                    Map<String, Object> out = assembleFromDb(tradeDate, rows);
                    if ("pre".equals(phase)) {
                        out.put("phase", "pre");
                        out.put("phaseText", "盘前·沿用上一交易日收盘版");
                    }
                    out.put("_cacheTime", System.currentTimeMillis());
                    resultCache.put(cacheKey, out);
                    return out;
                }
            }
        }

        // 实时计算（加锁防并发双算/双删）；pre态生成上一交易日收盘版并归库
        Map<String, Object> out;
        synchronized (COMPUTE_LOCK) {
            // 双重检查：等待期间别的线程可能已算完并缓存
            cached = resultCache.get(cacheKey);
            if (cached != null) {
                if (System.currentTimeMillis() - (long) cached.get("_cacheTime") < cacheTtl(phase, cached)) {
                    Map<String, Object> o = new LinkedHashMap<>(cached);
                    o.put("fromCache", true);
                    return o;
                }
            }
            out = computeAndPersist(tradeDate, "pre".equals(phase) ? "close" : phase);
            if ("pre".equals(phase)) {
                out.put("phase", "pre");
                out.put("phaseText", "盘前·沿用上一交易日收盘版");
            }
        }
        out.put("_cacheTime", System.currentTimeMillis());
        resultCache.put(cacheKey, out);
        // 盘中：结果写redis缓存（不入库）；TTL正常10分钟/降级5分钟，与内存缓存语义一致
        if ("intraday".equals(phase)) {
            try {
                long ttlMin = Boolean.TRUE.equals(out.get("degraded")) ? 5 : 10;
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

    /** 结果缓存TTL：熔断降级统一5分钟；盘中10分钟（贴实时），盘前/盘后30分钟 */
    private long cacheTtl(String phase, Map<String, Object> cached) {
        return Boolean.TRUE.equals(cached.get("degraded")) ? CACHE_TTL_DEGRADED
                : ("intraday".equals(phase) ? CACHE_TTL_INTRADAY : CACHE_TTL_OK);
    }

    /**
     * 刷新精选（按钮专用）：不管什么时段，直接走 computeAndPersist 获取一遍并先删后入库。
     * 与 getShortTermStocks 不同：不走缓存/不查库直读，强制实时采集+入库。
     */
    @Override
    public Map<String, Object> refreshShortTermStocks() {
        String tradeDate = resolveTradeDate();
        String phase = "close"; // 传 close 使 computeAndPersist 内部走先删后入库逻辑
        Map<String, Object> out;
        synchronized (COMPUTE_LOCK) {
            out = computeAndPersist(tradeDate, phase);
        }
        String cacheKey = tradeDate + "|" + phase;
        out.put("_cacheTime", System.currentTimeMillis());
        resultCache.put(cacheKey, out);
        if (resultCache.size() > 4) {
            resultCache.keySet().removeIf(k -> !k.equals(cacheKey));
        }
        return out;
    }

    /**
     * 最近交易日解析：一次K线请求同时取最后两根bar → [最新bar日, 上一交易日]（东财→腾讯→本地日历三级）。
     * 缓存按自然日失效；缓存值≠今日且为工作日时每5分钟重解析——盘前8点解析出的"昨日"不能缓存一整天卡住盘中判定；
     * 节假日重解析成本仅1次K线请求（有熔断保护），可接受。
     */
    private String[] resolveTradeDates() {
        String todayKey = new SimpleDateFormat("yyyyMMdd").format(new Date());
        String today = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        if (todayKey.equals(tradeDateCacheDay) && tradeDateCacheValue != null && !tradeDateCacheValue.isEmpty()) {
            boolean stale = !today.equals(tradeDateCacheValue) && isWeekday()
                    && System.currentTimeMillis() - tradeDateCacheAt > 5 * 60 * 1000L;
            if (!stale) {
                return new String[]{tradeDateCacheValue, tradeDateCachePrev};
            }
        }
        String latest = null;
        String prev = null;
        // 1) 东财上证指数日K（单次不重试，失败即触发K线熔断走兜底）：一次取最后两根bar日期
        if (!eastKlineBlocked()) {
            try {
                String url = String.format(getEastKlineUrlTpl(), "1.000001");
                String resp = httpGetNoRetry(url);
                if (resp != null) {
                    JSONArray ks = JSON.parseObject(resp).getJSONObject("data").getJSONArray("klines");
                    if (ks != null && !ks.isEmpty()) {
                        latest = ks.getString(ks.size() - 1).split(",")[0];
                        if (ks.size() >= 2) {
                            prev = ks.getString(ks.size() - 2).split(",")[0];
                        }
                    }
                } else {
                    markEastKlineFail();
                }
            } catch (Exception e) {
                markEastKlineFail();
            }
        }
        // 2) 腾讯指数兜底
        if (latest == null) {
            try {
                String resp = httpGetNoRetry(getTxKlineUrl() + "?param=sh000001,day,,,5,qfq");
                if (resp != null) {
                    JSONObject node = JSON.parseObject(resp).getJSONObject("data").getJSONObject("sh000001");
                    JSONArray days = node == null ? null : (node.getJSONArray("qfqday") == null
                            ? node.getJSONArray("day") : node.getJSONArray("qfqday"));
                    if (days != null && !days.isEmpty()) {
                        latest = days.getJSONArray(days.size() - 1).getString(0);
                        if (days.size() >= 2) {
                            prev = days.getJSONArray(days.size() - 2).getString(0);
                        }
                    }
                }
            } catch (Exception ignore) {
                // 兜底失败走本地日历
            }
        }
        // 3) 本地日历（仅跳过周末，法定节假日无法识别——仅极端双兜底失败时使用）
        if (latest == null) {
            Calendar c = Calendar.getInstance();
            while (c.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY || c.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
                c.add(Calendar.DAY_OF_MONTH, -1);
            }
            latest = new SimpleDateFormat("yyyy-MM-dd").format(c.getTime());
            c.add(Calendar.DAY_OF_MONTH, -1);
            while (c.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY || c.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
                c.add(Calendar.DAY_OF_MONTH, -1);
            }
            prev = new SimpleDateFormat("yyyy-MM-dd").format(c.getTime());
            logger.warn("最近交易日判定双兜底失败，退化为本地日历（节假日可能误判）：latest={} prev={}", latest, prev);
        }
        tradeDateCacheDay = todayKey;
        tradeDateCacheValue = latest;
        tradeDateCachePrev = prev == null ? latest : prev;
        tradeDateCacheAt = System.currentTimeMillis();
        return new String[]{latest, tradeDateCachePrev};
    }

    /** 兼容旧调用：仅取最新bar日（盘中/盘后=今日，盘前/节假日=上一交易日） */
    private String resolveTradeDate() {
        return resolveTradeDates()[0];
    }

    /** 今天是否周一~周五（本地日历兜底/盘前判定的粗粒度辅助，法定节假日不识别） */
    private static boolean isWeekday() {
        int dow = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
        return dow >= Calendar.MONDAY && dow <= Calendar.FRIDAY;
    }

    // ================================================================
    // 实时计算 + 归库
    // ================================================================
    private Map<String, Object> computeAndPersist(String tradeDate, String phase) {
        long start = System.currentTimeMillis();
        // 快照候选与池获取并行执行，降级原因跨线程收集 → 线程安全列表
        List<String> degradeReasons = Collections.synchronizedList(new ArrayList<>());

        // 0) 先提交快照候选抓取（独立线程）：与后续"池获取+妖股快照补充"并行，任一方不拖累另一方
        ExecutorService clistExecutor = Executors.newSingleThreadExecutor();
        Future<List<Map<String, Object>>> candFuture = clistExecutor.submit(() -> fetchClistCandidates(degradeReasons));
        clistExecutor.shutdown();

        // 1) 涨停池 + 炸板池（按归属交易日查询，串行，间隔200ms）
        String tradeDateCompact = tradeDate.replace("-", "");
        JSONObject zt = fetchPool(getZtPoolUrlTpl(), tradeDateCompact);
        JSONObject zb = fetchPool(getZbPoolUrlTpl(), tradeDateCompact);
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

        // 3) 收取并行执行的快照候选结果（池获取与妖股快照补充已在上方同步完成）
        List<Map<String, Object>> cand;
        try {
            cand = candFuture.get(150, TimeUnit.SECONDS);
        } catch (TimeoutException te) {
            candFuture.cancel(true);
            degradeReasons.add("快照候选获取超时，低位潜伏/趋势延续本轮降级");
            cand = new ArrayList<>();
        } catch (ExecutionException ee) {
            logger.warn("快照候选并行获取异常", ee.getCause());
            degradeReasons.add("快照候选获取异常，低位潜伏/趋势延续本轮降级");
            cand = new ArrayList<>();
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            candFuture.cancel(true);
            cand = new ArrayList<>();
        }

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

        // 4.5) 主力净流入兜底：池票在东财快照失败/缺失时经资金流日K补齐（行为判定与妖股评分依赖，仅补缺失不覆盖）
        backfillMainInflowFromFflow(type1.values(), tradeDate, degradeReasons);

        // 5) 妖股梯队：K线增强（涨停基因/位置）+ 行为判定 + 评分
        for (Map<String, Object> s : type1.values()) {
            enrichKlineFeatures(s, klines.get((String) s.get("code")));
            judgeBehavior(s);
        }

        // 6) 快照候选：K线特征 → 类型2/3归类
        List<Map<String, Object>> qianfuList = new ArrayList<>();
        List<Map<String, Object>> qushiList = new ArrayList<>();
        int klineMiss = 0, klineShort = 0;
        for (Map<String, Object> s : cand) {
            String code = (String) s.get("code");
            List<Bar> bars = klines.get(code);
            if (bars == null) { klineMiss++; continue; }
            if (bars.size() < 25) { klineShort++; continue; }
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
            logger.info("候选判定 code={} K线{}根 lowDepth={} volShrink={} ztCount60={} recentZt={} gain5={} gain20={} maOk={} → qianfu={} qushi={}",
                    code, bars.size(), f.get("lowDepth"), f.get("volShrink"), f.get("ztCount60"), f.get("recentZtDaysAgo"),
                    f.get("gain5"), f.get("gain20"), f.get("trendMaOk"), qianfu, qushi);
            if (qianfu && !qushi) {
                qianfuList.add(s);
            } else if (qushi) {
                qushiList.add(s);
            }
        }
        logger.info("候选归类统计 总{}只 K线缺失{}只 K线不足25根{}只 低位潜伏{}只 趋势延续{}只",
                cand.size(), klineMiss, klineShort, qianfuList.size(), qushiList.size());

        // 6.5) 主力净流入兜底：潜伏/趋势候选同上（评分"主力流入占比0~10分"因子需要，仅补缺失不覆盖）
        List<Map<String, Object>> scoredCand = new ArrayList<>(qianfuList);
        scoredCand.addAll(qushiList);
        backfillMainInflowFromFflow(scoredCand, tradeDate, degradeReasons);

        // 7) 行为判定 + 评分 + 排序 + 各类top10
        for (Map<String, Object> s : qianfuList) judgeBehavior(s);
        for (Map<String, Object> s : qushiList) judgeBehavior(s);
        // 经验闭环：读取历史复盘经验构建索引，反哺选股评分
        Map<String, List<String>> expIndex = buildExpIndex();
        List<Map<String, Object>> yaoguTop = scoreAndTop(type1.values(), "yaogu", 10, expIndex);
        List<Map<String, Object>> qianfuTop = scoreAndTop(qianfuList, "qianfu", 10, expIndex);
        List<Map<String, Object>> qushiTop = scoreAndTop(qushiList, "qushi", 10, expIndex);

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
        // push2ex池接口熔断期内0请求（GROUP_POOL 3分钟），防拉黑期继续请求延长封禁
        if (com.xk.srhwzzqdn.manager.util.StockDataFetcher.blocked(com.xk.srhwzzqdn.manager.util.StockDataFetcher.GROUP_POOL)) {
            logger.info("涨跌停池接口熔断期内（剩余{}秒），跳过请求",
                    com.xk.srhwzzqdn.manager.util.StockDataFetcher.remainMs(com.xk.srhwzzqdn.manager.util.StockDataFetcher.GROUP_POOL) / 1000);
            return out;
        }
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
        if (out.getIntValue("count") < 0) {
            // 池接口失败 → 3分钟内跳过后续池请求（含市场分析共享同一熔断组）
            com.xk.srhwzzqdn.manager.util.StockDataFetcher.markFail(
                    com.xk.srhwzzqdn.manager.util.StockDataFetcher.GROUP_POOL, 3 * 60 * 1000L);
        } else {
            com.xk.srhwzzqdn.manager.util.StockDataFetcher.markSuccess(com.xk.srhwzzqdn.manager.util.StockDataFetcher.GROUP_POOL);
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

    /** 行情快照粗筛（clist翻3页覆盖f184 top300）：市值/换手/涨幅/年初涨幅/非ST过滤后按主力净占比取top70。
     *  接口优先串行：东财clist接口（熔断期跳过）→ 新浪全市场快照兜底；东财失败激活GROUP_CLIST 3分钟熔断。
     *  原三源race同发（东财双host+新浪，每轮3路请求）已按合规模型改串行降级。 */
    private List<Map<String, Object>> fetchClistCandidates(List<String> degradeReasons) {
        boolean blockedNow = clistBlocked();
        if (blockedNow) {
            degradeReasons.add("行情快照接口熔断中（3分钟），东财源跳过，直接走新浪全市场快照兜底");
        } else {
            List<Map<String, Object>> east = fetchClistPages(getClistHosts()[1]);   // [1]=push2主源
            if (east != null && !east.isEmpty()) {
                markSuccessClist();
                upsertBasicIndustryQuietly(east);   // 东财正常时把行业/概念增量写入t_stock_basic，供封禁期兜底读取
                List<Map<String, Object>> list = new ArrayList<>(east);
                list.sort((a, b) -> {
                    Double pa = asDouble(a.get("mainInflowPct"));
                    Double pb = asDouble(b.get("mainInflowPct"));
                    return Double.compare(pb == null ? -999 : pb, pa == null ? -999 : pa);
                });
                return new ArrayList<>(list.subList(0, Math.min(70, list.size())));
            }
            markClistFail();
            degradeReasons.add("行情快照东财接口失败（已熔断3分钟），走新浪全市场快照兜底");
        }
        List<Map<String, Object>> sina = fetchClistCandidatesFromSina();
        if (sina != null && !sina.isEmpty()) {
            enrichCandidatesFromTencent(sina);   // 新浪无量比，腾讯批量行情补量比/市值（每60只一批）
            fillIndustryFromDb(sina);   // 新浪无行业/概念，库内t_stock_basic补齐
            degradeReasons.add("行情快照走新浪成交额top兜底，候选" + sina.size() + "只（主力净流入/年初涨幅缺失）");
            return sina; // 新浪源内部已按成交额降序过滤并取top70
        }
        degradeReasons.add("行情快照两源（东财+新浪）均失败，低位潜伏/趋势延续本轮降级");
        return new ArrayList<>();
    }

    /** 东财单host clist分页聚合（3页并发，单源内部分页并行）：全部页成功返回解析行列表，任一页失败返回null（视为该源失败） */
    private List<Map<String, Object>> fetchClistPages(String host) {
        ExecutorService exec = Executors.newFixedThreadPool(CLIST_PAGES);
        try {
            List<java.util.concurrent.Future<List<Map<String, Object>>>> pageFutures = new ArrayList<>();
            for (int pn = 1; pn <= CLIST_PAGES; pn++) {
                final int page = pn;
                pageFutures.add(exec.submit(() -> {
                    String body = httpGetNoRetry(host + String.format(CLIST_PATH, page));
                    if (body == null) return null;
                    JSONObject data = JSON.parseObject(body).getJSONObject("data");
                    JSONArray diff = toDiffArray(data == null ? null : data.get("diff"));
                    if (diff == null || diff.isEmpty()) return null;
                    List<Map<String, Object>> rows = new ArrayList<>();
                    parseClistRows(diff, rows);
                    return rows;
                }));
            }
            List<Map<String, Object>> merged = new ArrayList<>();
            for (java.util.concurrent.Future<List<Map<String, Object>>> f : pageFutures) {
                List<Map<String, Object>> rows = f.get(15, TimeUnit.SECONDS);
                if (rows == null || rows.isEmpty()) return null;
                merged.addAll(rows);
            }
            return merged.isEmpty() ? null : merged;
        } catch (Exception e) {
            logger.warn("短线选股clist分页聚合失败 host={}", host, e);
            return null;
        } finally {
            exec.shutdownNow();
        }
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
            // 接口优先串行：东财ulist批量快照接口（熔断期跳过）→ 腾讯批量行情 → 新浪批量行情；行业/概念缺失由库内t_stock_basic补齐
            boolean clistBlockedNow = clistBlocked();
            boolean ok;
            if (clistBlockedNow) {
                degradeReasons.add("行情集群熔断中，妖股快照东财请求跳过，直接走腾讯/新浪批量行情兜底");
                ok = enrichPoolSnapshotFromTencent(batch);
                if (!ok) ok = enrichPoolSnapshotFromSina(batch);
                if (ok) fillIndustryFromDb(batch);
            } else {
                Boolean east = applyUlistBatch(getClistHosts()[1], secids.toString(), batch);   // [1]=push2主源
                if (Boolean.TRUE.equals(east)) {
                    ok = true;
                    upsertBasicIndustryQuietly(batch);   // 池票行业/概念同步增量入库
                } else {
                    ok = enrichPoolSnapshotFromTencent(batch);
                    if (!ok) ok = enrichPoolSnapshotFromSina(batch);
                    if (ok) fillIndustryFromDb(batch);
                    if (!ok) {
                        degradeReasons.add("妖股快照三源（东财+腾讯+新浪批量行情）均失败，部分妖股资金/换手字段缺失");
                    }
                }
            }
            try {
                Thread.sleep(200); // 批间限速防反爬
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    /** 单hostulist批量快照拉取+字段回填（供race竞争）：成功true/失败null；字段口径 f62主力净流入/f184主力净占比/f20总市值/f10量比/f100行业/f103概念 */
    private Boolean applyUlistBatch(String host, String secids, List<Map<String, Object>> batch) {
        String body = httpGetNoRetry(host + String.format(ULIST_PATH, secids));
        if (body == null) return null;
        try {
            JSONObject data = JSON.parseObject(body).getJSONObject("data");
            JSONArray diff = toDiffArray(data == null ? null : data.get("diff"));
            if (diff == null || diff.isEmpty()) return null;
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
            return Boolean.TRUE;
        } catch (Exception e) {
            logger.warn("池票快照补充解析失败 host={}", host, e);
            return null;
        }
    }

    /**
     * 新浪全市场快照兜底（东财clist双域名均失败时使用）：
     * Market_Center.getHQNodeData 按成交额降序翻3页覆盖top240（原按换手率排序会被流通市值20-500亿过滤后剩~0只，实测成交额排序通过76只），
     * 提供换手率/流通市值/涨跌幅，但不提供主力净流入/量比/年初涨幅/行业/概念（置空）。
     * 粗筛条件与parseClistRows一致（年初涨幅过滤跳过），过滤后按成交额降序取top70。
     * 返回null表示该源失败（供race竞争；空列表与失败等价）。
     */
    private List<Map<String, Object>> fetchClistCandidatesFromSina() {
        List<Map<String, Object>> merged = new ArrayList<>();
        String base = getSinaHqNodeUrl();
        for (int page = 1; page <= 3; page++) {
            String url = base + "?page=" + page + "&num=80&node=hs_a&sort=amount&desc=1&_s_r_a=auto";
            String body = httpGetNoRetry(url);
            if (body == null) {
                logger.warn("新浪全市场快照兜底HTTP失败 page={}", page);
                return merged.isEmpty() ? null : merged;
            }
            try {
                JSONArray arr = JSON.parseArray(body);
                if (arr == null || arr.isEmpty()) {
                    logger.warn("新浪全市场快照返回空 page={} body前80={}", page,
                            body.substring(0, Math.min(80, body.length())));
                    break;
                }
                for (int i = 0; i < arr.size(); i++) {
                    JSONObject d = arr.getJSONObject(i);
                    String name = d.getString("name");
                    if (name == null || name.contains("ST") || name.contains("退")) continue;
                    String code = d.getString("code");
                    if (code == null) continue;
                    Double price = asDouble(d.get("trade"));
                    Double pct = asDouble(d.get("changepercent"));
                    Double turnover = asDouble(d.get("turnoverratio"));
                    Double circCap = asDouble(d.get("nmc")); // 流通市值（万元，需÷10000转亿元）
                    if (price == null || price < 2) continue;
                    if (circCap == null) continue;
                    circCap = circCap / 10000;
                    if (circCap < 20 || circCap > 500) continue;
                    if (turnover == null || turnover < 2 || turnover > 25) continue;
                    if (pct == null || pct < -2 || pct > 11) continue;
                    Map<String, Object> s = new LinkedHashMap<>();
                    s.put("code", code);
                    s.put("name", name);
                    s.put("marketFlag", code.startsWith("6") ? 1 : 0);
                    s.put("price", round2(price));
                    s.put("changePct", round2(pct));
                    s.put("industry", null);
                    s.put("circCap", round2(circCap));
                    Double totalCap = asDouble(d.get("mktcap"));
                    s.put("totalCap", totalCap == null ? null : round2(totalCap / 10000));
                    s.put("turnoverRate", round2(turnover));
                    s.put("volumeRatio", null);
                    s.put("mainInflow", null);
                    s.put("mainInflowPct", null);
                    s.put("inner", null);
                    s.put("outer", null);
                    s.put("outerInnerRatio", null);
                    s.put("ytdPct", null);
                    s.put("concept", null);
                    merged.add(s);
                }
                if (page < 3) Thread.sleep(200);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                logger.warn("新浪全市场快照解析失败 page={}", page, e);
                break;
            }
        }
        // 已按成交额降序拉取+过滤（保序），空结果视为该源失败（race竞争语义）
        return merged.isEmpty() ? null : new ArrayList<>(merged.subList(0, Math.min(70, merged.size())));
    }

    /**
     * 新浪批量行情兜底（东财ulist双域名均失败时使用）：
     * hq.sinajs.cn/list= 按代码批量查询，提供价格/涨跌幅，
     * 不提供主力净流入/量比/行业/概念（置空）。
     * @return true=至少补齐了一只
     */
    /** 腾讯批量行情兜底（东财ulist失败→腾讯→新浪三级）：qt.gtimg.cn/q=批量，补价格/涨跌幅/换手率/量比/总市值。
     *  字段实测：3现价 4昨收 32涨跌% 37成交额(万) 38换手率 45总市值(亿) 49量比；腾讯无主力资金（ff_接口已下线）
     *  与行业/概念（后者由fillIndustryFromDb库内补齐）。GB2312编码仅影响中文名（不使用），数字字段ASCII兼容。 */
    private boolean enrichPoolSnapshotFromTencent(List<Map<String, Object>> batch) {
        StringBuilder symbols = new StringBuilder();
        for (Map<String, Object> s : batch) {
            String code = (String) s.get("code");
            if (symbols.length() > 0) symbols.append(',');
            symbols.append(code.startsWith("6") ? "sh" : "sz").append(code);
        }
        String body = httpGetNoRetry(getTxQuoteUrl() + symbols);
        if (body == null) return false;
        int count = 0;
        for (String line : body.split(";")) {
            int eq = line.indexOf('=');
            if (eq < 0) continue;
            String key = line.substring(0, eq).trim();
            String val = line.substring(eq + 1).trim();
            if (val.startsWith("\"") && val.endsWith("\"")) val = val.substring(1, val.length() - 1);
            if (val.isEmpty() || "1".equals(val)) continue;   // v_pv_none_match="1" 防呆
            String[] f = val.split("~");
            if (f.length < 50) continue;
            String code = key.replaceAll(".*v_", "").replaceAll("^(sh|sz)", "");
            for (Map<String, Object> s : batch) {
                if (code.equals(s.get("code"))) {
                    Double price = asDouble(f[3]);
                    Double prevClose = asDouble(f[4]);
                    if (price != null) s.put("price", round2(price));
                    if (price != null && prevClose != null && prevClose > 0) {
                        s.put("changePct", round2((price - prevClose) / prevClose * 100));
                    }
                    if (asDouble(s.get("turnoverRate")) == null) s.put("turnoverRate", asDouble(f[38]));
                    if (asDouble(s.get("volumeRatio")) == null) {
                        Double vr = asDouble(f[49]);
                        s.put("volumeRatio", vr == null ? null : round2(vr));
                    }
                    if (asDouble(s.get("totalCap")) == null) s.put("totalCap", asDouble(f[45]));   // 已是亿
                    count++;
                    break;
                }
            }
        }
        return count > 0;
    }

    /** 库内补齐行业/概念（东财失败时新浪/腾讯兜底源无此字段）：t_stock_basic一次IN查询，仅填缺失字段。
     *  查询失败只打日志不抛出——行业/概念缺失不影响选股主流程。 */
    private void fillIndustryFromDb(List<Map<String, Object>> stocks) {
        if (stocks == null || stocks.isEmpty()) return;
        List<String> need = new ArrayList<>();
        for (Map<String, Object> s : stocks) {
            if (isBlankField(s.get("industry")) || isBlankField(s.get("concept"))) {
                String code = (String) s.get("code");
                if (code != null && !need.contains(code)) need.add(code);
            }
        }
        if (need.isEmpty()) return;
        try {
            List<Map<String, Object>> rows = shortTermPickMapper.selectIndustryByCodes(need);
            Map<String, Map<String, Object>> byCode = new HashMap<>();
            for (Map<String, Object> r : rows) byCode.put((String) r.get("stockCode"), r);
            for (Map<String, Object> s : stocks) {
                Map<String, Object> r = byCode.get(s.get("code"));
                if (r == null) continue;
                if (isBlankField(s.get("industry")) && !isBlankField(r.get("industry"))) {
                    s.put("industry", r.get("industry"));
                }
                if (isBlankField(s.get("concept")) && !isBlankField(r.get("conceptSectors"))) {
                    s.put("concept", trimConcept(String.valueOf(r.get("conceptSectors"))));
                }
            }
        } catch (Exception e) {
            logger.warn("库内行业/概念补齐失败（不影响选股主流程）: {}", e.getMessage());
        }
    }

    private static boolean isBlankField(Object v) {
        return v == null || "".equals(v) || "-".equals(v);
    }

    /** 东财源成功时把见到的名称/行业/概念增量写入t_stock_basic（封禁期fillIndustryFromDb的数据来源）。
     *  只写这三列且失败仅打日志，不影响选股主流程；每批200行 upsert。 */
    private void upsertBasicIndustryQuietly(Collection<Map<String, Object>> stocks) {
        List<Map<String, Object>> rows = new ArrayList<>();
        for (Map<String, Object> s : stocks) {
            String code = (String) s.get("code");
            Object ind = s.get("industry");
            Object con = s.get("concept");
            if (code == null || (isBlankField(ind) && isBlankField(con))) continue;
            Map<String, Object> r = new HashMap<>();
            r.put("stockCode", code);
            r.put("stockName", s.get("name"));
            r.put("industry", isBlankField(ind) ? null : String.valueOf(ind));
            r.put("concept", isBlankField(con) ? null : String.valueOf(con));
            rows.add(r);
        }
        if (rows.isEmpty()) return;
        try {
            for (int from = 0; from < rows.size(); from += 200) {
                shortTermPickMapper.upsertBasicIndustry(rows.subList(from, Math.min(from + 200, rows.size())));
            }
        } catch (Exception e) {
            logger.warn("行业/概念增量入库失败（不影响选股主流程）: {}", e.getMessage());
        }
    }

    /** 新浪兜底候选的量比补齐（新浪全市场快照无量比/主力资金）：复用腾讯批量行情，每60只一批单次尝试 */
    private void enrichCandidatesFromTencent(List<Map<String, Object>> candidates) {
        for (int from = 0; from < candidates.size(); from += 60) {
            enrichPoolSnapshotFromTencent(candidates.subList(from, Math.min(from + 60, candidates.size())));
        }
    }

    private boolean enrichPoolSnapshotFromSina(List<Map<String, Object>> batch) {
        StringBuilder symbols = new StringBuilder();
        for (Map<String, Object> s : batch) {
            String code = (String) s.get("code");
            if (symbols.length() > 0) symbols.append(',');
            symbols.append(code.startsWith("6") ? "sh" : "sz").append(code);
        }
        String url = getSinaQuoteUrl() + symbols.toString();
        String body = httpGetNoRetry(url);
        if (body == null) return false;
        int count = 0;
        for (String line : body.split(";")) {
            int eq = line.indexOf('=');
            if (eq < 0) continue;
            String key = line.substring(0, eq).trim();
            String val = line.substring(eq + 1).trim();
            if (val.startsWith("\"") && val.endsWith("\"")) val = val.substring(1, val.length() - 1);
            if (val.isEmpty()) continue;
            String[] f = val.split(",");
            if (f.length < 10) continue;
            String symbol = key.replaceAll(".*hq_str_", "");
            if (symbol.length() < 4) continue;
            String code = symbol.substring(2);
            for (Map<String, Object> s : batch) {
                if (code.equals(s.get("code"))) {
                    Double price = asDouble(f[3]);
                    Double prevClose = asDouble(f[2]);
                    if (price != null) s.put("price", round2(price));
                    if (price != null && prevClose != null && prevClose > 0) {
                        s.put("changePct", round2((price - prevClose) / prevClose * 100));
                    }
                    count++;
                    break;
                }
            }
        }
        return count > 0;
    }

    // ================================================================
    // K线抓取（东财单次→熔断→腾讯兜底→新浪兜底）
    // ================================================================
    /** 单根日K：date + open,close,high,low,volume */
    static class Bar {
        String date;
        double open, close, high, low, volume;
    }

    /** 并发拉取60日日K（16线程；每股内接口优先串行：东财（熔断期跳过）→腾讯→新浪，均单次尝试；替代原每股三源race同发） */
    private Map<String, List<Bar>> fetchKlines(Map<String, Integer> targets) {
        Map<String, List<Bar>> map = new ConcurrentHashMap<>();
        // 16线程：40股分3批跑完（8线程分5批，全失败时仅轮次等待就~51s）；单源快速失败后正常轮次~2s/批
        ExecutorService es = Executors.newFixedThreadPool(16);
        try {
            List<Callable<Void>> tasks = new ArrayList<>();
            for (Map.Entry<String, Integer> e : targets.entrySet()) {
                tasks.add(() -> {
                    try {
                        String code = e.getKey();
                        int market = e.getValue();
                        // 接口优先串行：东财K线接口（熔断期跳过；失败即激活5分钟熔断，同批后续股自动直走兜底）→腾讯→新浪
                        List<Bar> bars = null;
                        if (!eastKlineBlocked()) {
                            bars = fetchKlineEast(code, market);
                        }
                        if (bars == null || bars.isEmpty()) {
                            bars = fetchKlineTx(code, market);
                        }
                        if (bars == null || bars.isEmpty()) {
                            bars = fetchKlineSina(code, market);
                        }
                        if (bars != null && !bars.isEmpty()) {
                            map.put(code, bars);
                        } else {
                            logger.warn("K线三源（东财/腾讯/新浪）均失败 code={}", code);
                        }
                    } catch (Exception ignore) {
                        // 单股失败不影响整体，该股按K线缺失处理
                    }
                    Thread.sleep(80);
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
            String resp = httpGetNoRetry(String.format(getEastKlineUrlTpl(), market + "." + code));
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

    /**
     * 主力净流入兜底（东财clist/ulist快照不可达时）：push2his资金流日K逐票补齐当日主力净流入(亿)与净占比(%)。
     * 盘中该接口当日行有数（分钟级延迟），与东财快照f62/f184口径一致；只认tradeDate当日行，绝不串用历史数据；
     * 仅补空值不覆盖东财主源数据。GROUP_KLINE熔断期内0请求（push2his与K线同host家族）；失败触发5分钟熔断；
     * 150ms间隔限速。行为判定/评分（主力流入占比0~10分因子）依赖此字段，兜底保证选股与算法一致。
     */
    private void backfillMainInflowFromFflow(Collection<Map<String, Object>> stocks, String tradeDate, List<String> degradeReasons) {
        List<Map<String, Object>> missing = new ArrayList<>();
        for (Map<String, Object> s : stocks) {
            if (s.get("mainInflow") == null || s.get("mainInflowPct") == null) missing.add(s);
        }
        if (missing.isEmpty()) return;
        if (eastKlineBlocked()) {
            degradeReasons.add("东财K线集群熔断中，主力净流入资金流日K兜底跳过（" + missing.size() + "只字段缺失）");
            return;
        }
        int filled = 0, failed = 0;
        for (Map<String, Object> s : missing) {
            if (eastKlineBlocked()) break;   // 首次失败触发熔断后剩余票0请求
            String code = (String) s.get("code");
            int market = marketInt(code);
            String resp = httpGetNoRetry(String.format(getFflowDayUrlTpl(), market + "." + code));
            if (resp == null) {
                failed++;
                markEastKlineFail();
            } else {
                try {
                    JSONArray ks = JSON.parseObject(resp).getJSONObject("data").getJSONArray("klines");
                    if (ks != null) {
                        for (int i = ks.size() - 1; i >= 0; i--) {
                            String[] f = ks.getString(i).split(",");
                            if (!tradeDate.equals(f[0])) continue;   // 只认当日行
                            if (s.get("mainInflow") == null) s.put("mainInflow", round2(asDouble(f[1]) / 1e8));
                            if (s.get("mainInflowPct") == null) s.put("mainInflowPct", round2(asDouble(f[2])));
                            filled++;
                            break;
                        }
                    }
                } catch (Exception e) {
                    logger.warn("资金流日K兜底解析失败 code={}", code, e);
                }
            }
            try {
                Thread.sleep(150L);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                return;
            }
        }
        if (filled > 0) degradeReasons.add("主力净流入经资金流日K兜底补齐" + filled + "只（分钟级延迟口径）");
        if (failed > 0) degradeReasons.add("主力净流入兜底失败" + failed + "只（字段留空，解封后自动恢复）");
    }

    /** 腾讯日K兜底（前复权，口径与东财一致） */
    private List<Bar> fetchKlineTx(String code, int market) {
        try {
            String mkt = market == 1 ? "sh" : "sz";
            String resp = httpGetNoRetry(getTxKlineUrl() + "?param=" + mkt + code + ",day,,,60,qfq");
            if (resp == null) {
                logger.warn("腾讯K线HTTP失败 code={}", code);
                return null;
            }
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

    /**
     * 新浪日K线兜底（东财/腾讯均失败时使用）：quotes.sina.cn/cn/api/jsonp_v2.php 接口，scale=240日K，
     * datalen=60，返回JSONP格式 var srhwTrend=[{day,open,high,low,close,volume},...]，volume单位是"股"。
     */
    private List<Bar> fetchKlineSina(String code, int market) {
        try {
            String symbol = (market == 1 ? "sh" : "sz") + code;
            String url = getSinaKlineUrl() + "/CN_MarketDataService.getKLineData?symbol=" + symbol
                    + "&scale=240&ma=no&datalen=60";
            String resp = httpGetNoRetry(url);
            if (resp == null || resp.isEmpty()) {
                logger.warn("新浪K线兜底HTTP失败 code={} resp={}", code, resp == null ? "null" : "empty");
                return null;
            }
            int start = resp.indexOf('[');
            int end = resp.lastIndexOf(']');
            if (start < 0 || end <= start) {
                logger.warn("新浪K线兜底解析失败 code={} resp前100={}", code, resp.substring(0, Math.min(100, resp.length())));
                return null;
            }
            JSONArray arr = JSON.parseArray(resp.substring(start, end + 1));
            if (arr == null || arr.isEmpty()) {
                logger.warn("新浪K线兜底空数组 code={}", code);
                return null;
            }
            List<Bar> bars = new ArrayList<>(arr.size());
            for (int i = 0; i < arr.size(); i++) {
                JSONObject o = arr.getJSONObject(i);
                String day = o.getString("day");
                if (day == null || day.length() < 10) continue;
                Double open = asDouble(o.get("open"));
                Double close = asDouble(o.get("close"));
                Double high = asDouble(o.get("high"));
                Double low = asDouble(o.get("low"));
                Double volume = asDouble(o.get("volume"));
                if (open == null || close == null || high == null || low == null) continue;
                Bar b = new Bar();
                b.date = day.substring(0, 10);
                b.open = open;
                b.close = close;
                b.high = high;
                b.low = low;
                b.volume = volume == null ? 0 : volume;
                bars.add(b);
            }
            return bars.isEmpty() ? null : bars;
        } catch (Exception e) {
            logger.warn("新浪K线兜底失败 code={}", code);
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

        // 非池涨停票+主力强流入 → 继续拉升（clist抓到的涨停票但非涨停池来源）
        if (!limitUpNow && zhaban == 0 && pct != null && pct >= 9.5
                && mainPositive && mainPct != null && mainPct > 5) {
            behavior = "pull_up";
            ev.add(String.format("涨停%.1f%%", pct));
            ev.add(String.format("主力净流入占成交%.1f%%", mainPct));
            if (mainIn != null) ev.add(String.format("主力净流入%.2f亿", mainIn));
            s.put("behavior", behavior);
            s.put("behaviorEvidence", String.join("；", ev));
            return;
        }

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
            if (mainPct != null && mainPct > 10 && mainPositive && pct != null && pct > 5) {
                behavior = "pull_up";
                ev.add(String.format("主力净流入占成交%.1f%%", mainPct));
                ev.add(String.format("涨幅%.1f%%", pct));
                if (mainIn != null) ev.add(String.format("主力净流入%.2f亿", mainIn));
            } else if (mainPositive && mainPct != null && mainPct > 5
                    && (shrink || (pct != null && pct <= 5))) {
                behavior = "absorb";
                ev.add(String.format("主力净流入%.2f亿占成交%.1f%%", mainIn, mainPct));
                if (shrink) ev.add("缩量");
                if (pct != null) ev.add(String.format("涨幅%.1f%%", pct));
            } else if (mainPct != null && mainPct <= -3 && (mainIn == null || mainIn < 0)) {
                behavior = "distribute";
                ev.add(String.format("主力净流出占成交%.1f%%", mainPct));
            } else if (maBull && pct != null && Math.abs(pct) <= 5
                    && (shrink || (volumeRatio != null && volumeRatio < 1.2))) {
                behavior = "organize";
                ev.add("均线多头窄幅整理");
                ev.add(String.format("涨幅%.1f%%", pct));
                ev.add("量能温和");
            } else if (mainPositive && outerDominant && shrink) {
                behavior = "absorb";
                ev.add(String.format("主力净流入%.2f亿", mainIn));
                ev.add(String.format("外盘/内盘%.2f主动买占优", ratio));
                ev.add("缩量");
            } else {
                behavior = "unknown";
                ev.add("量价信号不明确，无法判定");
            }
        }
        s.put("behavior", behavior);
        s.put("behaviorEvidence", String.join("；", ev));
    }

    /** 读取最近100条经验构建索引：stockCode → expType列表（供选股评分调整，经验闭环） */
    private Map<String, List<String>> buildExpIndex() {
        Map<String, List<String>> index = new HashMap<>();
        try {
            List<ShortTermExperience> exps = shortTermPickMapper.selectRecentExperience(100);
            if (exps != null) {
                for (ShortTermExperience e : exps) {
                    index.computeIfAbsent(e.getStockCode(), k -> new ArrayList<>()).add(e.getExpType());
                }
            }
        } catch (Exception e) {
            logger.warn("读取历史经验索引失败，选股评分不做经验调整", e);
        }
        return index;
    }

    /** 出货行为一票剔除 + 评分排序 + topN + 经验闭环调整 + 优先级理由/确认信号/风险提示 */
    private List<Map<String, Object>> scoreAndTop(Collection<Map<String, Object>> pool, String type, int topN,
            Map<String, List<String>> expIndex) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (Map<String, Object> s : pool) {
            if ("distribute".equals(s.get("behavior"))) continue; // 出货嫌疑不入选
            double baseScore = calcScore(s, type);
            // 经验闭环：根据历史复盘经验调整评分（crash惩罚/fake_out轻惩/success奖励）
            String code = (String) s.get("code");
            List<String> exps = expIndex.get(code);
            if (exps != null && !exps.isEmpty() && baseScore > 0) {
                int crashCount = 0, fakeOutCount = 0, successCount = 0;
                for (String exp : exps) {
                    if ("crash".equals(exp)) crashCount++;
                    else if ("fake_out".equals(exp)) fakeOutCount++;
                    else if ("success".equals(exp) || "limit_up".equals(exp)) successCount++;
                }
                double factor = 1.0 - crashCount * 0.15 - fakeOutCount * 0.08 + successCount * 0.05;
                factor = Math.max(0.3, Math.min(1.3, factor));
                double adjustedScore = baseScore * factor;
                s.put("score", round2(adjustedScore));
                if (factor != 1.0) {
                    s.put("expAdjust", String.format("历史经验%d条(崩车%d/假强%d/成功%d)，评分×%.2f(%.1f→%.1f)",
                            exps.size(), crashCount, fakeOutCount, successCount, factor, baseScore, adjustedScore));
                }
            } else {
                s.put("score", round2(baseScore));
            }
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
            if (mainPct != null) score += clamp(mainPct, 0, 20) * 0.5;              // 主力流入占比 0~10
            if (gain20 != null && gain20 >= -8 && gain20 <= 12) score += 2;         // 平台未启动
            if ("pull_up".equals(behavior)) score += 5;
            else if ("absorb".equals(behavior)) score += 4;
            else if ("organize".equals(behavior)) score += 2;
            else if (mainPct != null && mainPct > 15) score += 1.5;                 // unknown但主力强流入保底
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
            if (mainPct != null) score += clamp(mainPct, 0, 15) * 0.4;              // 主力流入占比 0~6
            if ("pull_up".equals(behavior)) score += 5;
            else if ("organize".equals(behavior)) score += 3;
            else if ("absorb".equals(behavior)) score += 2;
            else if (mainPct != null && mainPct > 15) score += 2;                   // unknown但主力强流入保底
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
            String pickType = p.getPickType();
            // 按类型区分初始窗口：妖股T+3（连板3天见分晓），潜伏/趋势T+5
            int window = "qianfu".equals(pickType) ? 5 : ("qushi".equals(pickType) ? 5 : 3);
            Map<String, Object> fact = windowFact(bars, idx, window, price0);
            String expType = classifyExp(fact, pickType);
            if (expType == null) {
                // 扩大窗口：潜伏T+10（等启动），趋势/妖股T+5
                int window2 = "qianfu".equals(pickType) ? 10 : 5;
                if (idx + window2 <= bars.size()) {
                    window = window2;
                    fact = windowFact(bars, idx, window, price0);
                    expType = classifyExp(fact, pickType);
                }
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
            exp.setResultBrief(truncate(String.format("推荐价%.2f元，推荐后%d个交易日最高%+.1f%%、最低%+.1f%%、期末%+.1f%%，期间涨停%d次，量能比%.1f",
                    price0, window,
                    asDouble(fact.get("maxHighPct")) == null ? 0 : asDouble(fact.get("maxHighPct")),
                    asDouble(fact.get("minLowPct")) == null ? 0 : asDouble(fact.get("minLowPct")),
                    asDouble(fact.get("endPct")) == null ? 0 : asDouble(fact.get("endPct")),
                    (int) fact.getOrDefault("ztCount", 0),
                    asDouble(fact.get("volRatio")) == null ? 0 : asDouble(fact.get("volRatio"))), 500));
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

    /** 推荐后window根bar的事实统计（相对推荐价），含量能变化（推荐后均量/推荐前5日均量） */
    private Map<String, Object> windowFact(List<Bar> bars, int idx, int window, double price0) {
        double maxHigh = -Double.MAX_VALUE, minLow = Double.MAX_VALUE;
        int ztCount = 0;
        double prevClose = idx > 0 ? bars.get(idx - 1).close : price0;
        double volSum = 0;
        int volCount = 0;
        for (int i = idx; i < idx + window && i < bars.size(); i++) {
            Bar b = bars.get(i);
            maxHigh = Math.max(maxHigh, b.high);
            minLow = Math.min(minLow, b.low);
            if (prevClose > 0 && (b.close / prevClose - 1) * 100 >= 9.5) ztCount++;
            prevClose = b.close;
            volSum += b.volume;
            volCount++;
        }
        double endClose = bars.get(Math.min(idx + window, bars.size()) - 1).close;
        // 推荐前5日均量（量能变化基准）
        double preVolSum = 0;
        int preVolCount = 0;
        for (int i = Math.max(0, idx - 5); i < idx && i < bars.size(); i++) {
            preVolSum += bars.get(i).volume;
            preVolCount++;
        }
        Map<String, Object> f = new LinkedHashMap<>();
        f.put("maxHighPct", round2((maxHigh / price0 - 1) * 100));
        f.put("minLowPct", round2((minLow / price0 - 1) * 100));
        f.put("endPct", round2((endClose / price0 - 1) * 100));
        f.put("ztCount", ztCount);
        f.put("volRatio", preVolCount > 0 && volCount > 0 ? round2((volSum / volCount) / (preVolSum / preVolCount)) : null);
        return f;
    }

    /** 事实归类（按推荐类型区分阈值）：limit_up > success > crash > fake_out > no_start */
    private String classifyExp(Map<String, Object> fact, String pickType) {
        Double maxHigh = asDouble(fact.get("maxHighPct"));
        Double minLow = asDouble(fact.get("minLowPct"));
        Double endPct = asDouble(fact.get("endPct"));
        int ztCount = (int) fact.getOrDefault("ztCount", 0);
        if (maxHigh == null || minLow == null || endPct == null) return null;
        if (ztCount >= 1) return "limit_up";
        if ("qianfu".equals(pickType)) {
            // 低位潜伏：启动5%即成功；跌5%该止损；冲高回落3%算假突破；未启动且没大跌=中性不生成
            if (maxHigh >= 5 && endPct >= 2) return "success";
            if (minLow <= -5) return "crash";
            if (maxHigh >= 3 && endPct < 0) return "fake_out";
            if (maxHigh < 3 && endPct >= -3) return null;
            return "no_start";
        } else if ("qushi".equals(pickType)) {
            // 趋势延续：趋势破坏看endPct<-5或最低≤-6%；冲高回落4%算假强
            if (maxHigh >= 5 && endPct >= 2) return "success";
            if (minLow <= -6 || endPct < -5) return "crash";
            if (maxHigh >= 4 && endPct < 0) return "fake_out";
            if (maxHigh < 3 && endPct < 1) return "no_start";
            return null;
        } else {
            // 妖股梯队：连板股期望高，3天最高不到5%已偏弱
            if (maxHigh >= 8 && endPct >= 3) return "success";
            if (minLow <= -8) return "crash";
            if (maxHigh >= 5 && endPct < 0) return "fake_out";
            if (maxHigh < 5 && endPct >= 0) return "no_start";
            return null;
        }
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
            String user = "以下是若干条\"历史短线推荐→实际走势\"的事实记录。\n"
                    + "pickType为推荐策略：yaogu妖股梯队（连板高度+封板质量，期望继续拉升）/qianfu低位潜伏（低位缩量等启动，不启动不算失败）/qushi趋势延续（均线多头+近5日有涨停，期望持续向上）。\n"
                    + "expType为程序判定归类：limit_up晋级/success按信号启动/crash崩车/fake_out假强/no_start不启动。量能比>1为放量，<1为缩量。\n"
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

    // ==================== 请求头 ====================
    // 合规约束：固定单一UA/Referer正常访问公开数据接口，不做UA轮换伪装、不规避访问控制；
    // 访问频率由全局熔断器+单次尝试+批量200ms间隔约束，克制调用
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
        // 新浪接口2021年起强制要求sina站内Referer（接口访问要求，非伪装；finance.sina.com.cn实测200，hq.sinajs.cn自引用会403）；其余固定东财行情页来源
        request.setHeader("Referer", isSina ? "https://finance.sina.com.cn/" : "https://quote.eastmoney.com/");
    }

    /** GET请求（单次尝试，无重试）：失败由调用方降级兜底源/激活熔断；maxAttempts参数已废弃统一单次（重试会延长拉黑时长且放大请求量） */
    private static String httpGet(String url, int maxAttempts) {
        return httpGetSingle(url);
    }

    private static String httpGetSingle(String url) {
        try (CloseableHttpClient client = HttpClientBuilder.create().disableAutomaticRetries().build()) {
            HttpGet request = new HttpGet(url);
            applyBrowserHeaders(request);
            request.setConfig(RequestConfig.custom()
                    .setConnectTimeout(5000)
                    .setSocketTimeout(8000) // race架构下快速失败优于长等待：正常请求<1s，8s足够宽裕
                    .build());
            try (CloseableHttpResponse response = client.execute(request)) {
                if (response.getStatusLine().getStatusCode() == 200) {
                    return EntityUtils.toString(response.getEntity(), "UTF-8");
                }
                logger.warn("短线选股HTTP非200 | 状态={} | url={}",
                        response.getStatusLine().getStatusCode(), url);
            }
        } catch (Exception e) {
            logger.warn("短线选股HTTP失败 | url={} | 原因={}", url, e.getMessage());
        }
        return null;
    }

    private static void markEastKlineFail() {
        com.xk.srhwzzqdn.manager.util.StockDataFetcher.markFail(
                com.xk.srhwzzqdn.manager.util.StockDataFetcher.GROUP_KLINE, 5 * 60 * 1000L);
    }

    private static boolean eastKlineBlocked() {
        return com.xk.srhwzzqdn.manager.util.StockDataFetcher.blocked(
                com.xk.srhwzzqdn.manager.util.StockDataFetcher.GROUP_KLINE);
    }

    private static void markClistFail() {
        com.xk.srhwzzqdn.manager.util.StockDataFetcher.markFail(
                com.xk.srhwzzqdn.manager.util.StockDataFetcher.GROUP_CLIST, 3 * 60 * 1000L);
    }

    private static void markSuccessClist() {
        com.xk.srhwzzqdn.manager.util.StockDataFetcher.markSuccess(
                com.xk.srhwzzqdn.manager.util.StockDataFetcher.GROUP_CLIST);
    }

    private static boolean clistBlocked() {
        return com.xk.srhwzzqdn.manager.util.StockDataFetcher.blocked(
                com.xk.srhwzzqdn.manager.util.StockDataFetcher.GROUP_CLIST);
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
