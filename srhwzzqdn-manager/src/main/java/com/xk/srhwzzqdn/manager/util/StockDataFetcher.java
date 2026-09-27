package com.xk.srhwzzqdn.manager.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 股票数据并行竞争获取器 + 全局统一熔断器
 * <p>
 * 解决两类问题：
 * 1) 效率：多数据源（东财API→爬虫→腾讯→新浪）原先串行降级，一个源卡3次重试拖垮整体；
 *    race() 将所有数据源并行提交，任一源成功立即返回并中断其余源任务，总耗时≈最快源耗时。
 * 2) IP拉黑恶化：原先各服务各自为战（部分模块如 StockQuoteUtil 甚至无熔断），拉黑期内仍反复重试，
 *    反爬风控会因持续无效请求延长封禁时长；guard() 统一按"接口组"熔断——拉黑期内该组所有请求
 *    直接跳过（0请求），到期后自动恢复探测，探测成功即回归主源。
 * <p>
 * 熔断组与既有约束对齐：
 * - KLINE（push2his K线）：熔断5分钟
 * - CLIST（push2/push2delay 快照）：熔断3分钟
 * - QUOTE（push2 实时报价）：熔断3分钟
 * - FLOW（push2his 资金流）：熔断3分钟
 * - DATACENTER（datacenter-web 财务/股东/报表）：熔断5分钟
 * - POOL（push2ex 涨停/炸板/跌停池）：熔断3分钟
 */
public class StockDataFetcher {

    private static final Logger logger = LoggerFactory.getLogger(StockDataFetcher.class);

    // ==================== 熔断器（全局统一，跨服务共享同一份拉黑状态） ====================
    public static final String GROUP_KLINE = "kline";
    public static final String GROUP_CLIST = "clist";
    public static final String GROUP_QUOTE = "quote";
    public static final String GROUP_FLOW = "flow";
    public static final String GROUP_DATACENTER = "datacenter";
    public static final String GROUP_POOL = "pool";

    private static final ConcurrentHashMap<String, Long> BLOCKED_UNTIL = new ConcurrentHashMap<>();

    /** 该接口组是否处于熔断期（拉黑期内调用方应直接跳过请求，0流量等待解封） */
    public static boolean blocked(String group) {
        Long until = BLOCKED_UNTIL.get(group);
        return until != null && System.currentTimeMillis() < until;
    }

    /** 标记接口组请求失败，熔断 breakMs 毫秒（期间该组所有请求直接跳过） */
    public static void markFail(String group, long breakMs) {
        BLOCKED_UNTIL.put(group, System.currentTimeMillis() + breakMs);
        logger.warn("[数据熔断] 组={} 熔断{}秒：期间所有请求直接跳过，防延长拉黑", group, breakMs / 1000);
    }

    /** 探测成功后手动解除熔断（如 race 中东财恢复成功） */
    public static void markSuccess(String group) {
        BLOCKED_UNTIL.remove(group);
    }

    public static long remainMs(String group) {
        Long until = BLOCKED_UNTIL.get(group);
        if (until == null) return 0;
        return Math.max(0, until - System.currentTimeMillis());
    }

    // ==================== 并行竞争获取 ====================
    // 独立的 race 线程池：调用方在自己的线程/线程池中等待，源任务全部提交到这里，
    // 调用方线程不占用 race 池 → 嵌套 race（如 fetchKlines 的每股任务内再 race 三源）不会线程饥饿死锁。
    // 48线程：源任务在东财无响应时要等满socket超时（8s）才释放线程；40股短线K线一轮最多120任务，
    // 24线程时排队会挤占race窗口导致腾讯/新浪源任务未执行即被cancel（表现为源静默失败无日志）。
    // 禁自动重试后单任务占用上限=1×超时，48线程进一步兜底排队风险
    private static final AtomicInteger SEQ = new AtomicInteger();
    private static final ExecutorService RACE_POOL = Executors.newFixedThreadPool(48, r -> {
        Thread t = new Thread(r, "stock-data-race-" + SEQ.incrementAndGet());
        t.setDaemon(true);
        return t;
    });

    /** 竞争源：name=源标识（日志用），task=取数任务（返回null视为该源失败） */
    public static final class Source<T> {
        public final String name;
        public final Callable<T> task;

        public Source(String name, Callable<T> task) {
            this.name = name;
            this.task = task;
        }
    }

    /** 竞争结果：value=首个成功源的数据；source=命中源名称（日志/降级提示用） */
    public static class RaceResult<T> {
        public final T value;
        public final String source;

        RaceResult(T value, String source) {
            this.value = value;
            this.source = source;
        }
    }

    /**
     * 多数据源并行竞争：任一源返回"有效值"（非null）即胜出，立即中断其余源任务并返回。
     * 全部源失败（返回null/抛异常）或超时 → 返回null。
     *
     * @param sources        源任务列表，按优先级排列仅影响日志语义，不影响竞争
     * @param timeoutMs      总超时（建议≥单源超时+余量，如20秒）
     * @param winGroupOnFail 所有源都失败时，对哪些熔断组标记失败（传null不标记，由调用方自行处理）
     */
    @SafeVarargs
    public static <T> RaceResult<T> race(long timeoutMs, String[] winGroupOnFail, Source<T>... sources) {
        if (sources == null || sources.length == 0) return null;
        int n = sources.length;
        List<Future<T>> futures = new ArrayList<>(n);
        for (Source<T> s : sources) {
            try {
                futures.add(RACE_POOL.submit(s.task));
            } catch (Exception e) {
                futures.add(null);  // 提交失败按失败源处理
                logger.warn("[数据竞争] 源任务提交失败 name={}", s.name, e);
            }
        }
        long deadline = System.currentTimeMillis() + timeoutMs;
        List<Future<T>> winner = new ArrayList<>(1);
        String winName = null;
        T winValue = null;
        try {
            while (System.currentTimeMillis() < deadline) {
                for (int i = 0; i < futures.size(); i++) {
                    Future<T> f = futures.get(i);
                    if (f == null || f.isDone() || f.isCancelled()) continue;
                    try {
                        T v = f.get(0, TimeUnit.MILLISECONDS);  // 非阻塞探测
                        if (v != null) {
                            winner.add(f);
                            winName = sources[i].name;
                            winValue = v;
                            break;
                        }
                    } catch (Exception ignore) {
                        // 该源执行异常/超时探测失败 → 继续等其余源
                    }
                }
                if (winValue != null) break;
                Thread.sleep(20);
            }
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
        // 取消未完成与已完成的全部其余任务（interrupt源任务线程）
        for (int i = 0; i < futures.size(); i++) {
            Future<T> f = futures.get(i);
            if (f == null) continue;
            if (winner.contains(f)) continue;
            f.cancel(true);
        }
        if (winValue != null) {
            logger.info("[数据竞争] 命中源={} 耗时至deadline剩余{}ms", winName, deadline - System.currentTimeMillis());
            return new RaceResult<>(winValue, winName);
        }
        return failResult(winGroupOnFail);
    }

    private static <T> RaceResult<T> failResult(String[] groups) {
        if (groups != null) {
            for (String g : groups) {
                // 各组默认熔断时长与既有约束一致
                markFail(g, breakMsOf(g));
            }
        }
        return null;
    }

    /**
     * 共享数据采集线程池（守护线程）：供各服务把相互独立的采集块并行化（如市场分析的多板块聚合）。
     * 注意：任务内不要再向本池提交任务并同步等待，避免线程饥饿；嵌套竞争请用 race()。
     */
    public static ExecutorService executor() {
        return RACE_POOL;
    }

    private static long breakMsOf(String group) {
        switch (group) {
            case GROUP_KLINE: return 5 * 60 * 1000L;
            case GROUP_CLIST:
            case GROUP_QUOTE:
            case GROUP_FLOW:
            case GROUP_POOL: return 3 * 60 * 1000L;
            case GROUP_DATACENTER: return 5 * 60 * 1000L;
            default: return 3 * 60 * 1000L;
        }
    }

    private StockDataFetcher() {
    }
}
