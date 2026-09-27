package com.xk.srhwzzqdn.manager.util;

import com.xk.srhwzzqdn.manager.system.mapper.SysCommConfigMapper;
import com.xk.srhwzzqdn.model.entity.system.SysCommConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 接口配置工具类（全量缓存版）
 * 启动后首次调用一次性读取全部启用配置到内存缓存，后续从内存读不查库；
 * 缓存5分钟TTL过期后下次调用自动重新加载；配置更新后可调refreshCache()手动刷新。
 * 静态方法供 StockAssetServiceImpl / StockQuoteUtil 等 static 上下文调用
 */
public class InterfaceConfigUtil {
    private static final Logger logger = LoggerFactory.getLogger(InterfaceConfigUtil.class);

    private static final long CACHE_TTL_MS = 5 * 60 * 1000L;
    private static volatile long cacheLoadTime = 0L;
    private static final ConcurrentHashMap<String, SysCommConfig> cache = new ConcurrentHashMap<>();

    /** 懒加载全量配置到缓存（线程安全，过期才重新加载） */
    private static void ensureCacheLoaded() {
        if (System.currentTimeMillis() - cacheLoadTime < CACHE_TTL_MS && !cache.isEmpty()) {
            return;
        }
        try {
            SysCommConfigMapper mapper = SpringContextHolder.getBean(SysCommConfigMapper.class);
            if (mapper == null) return;
            List<SysCommConfig> all = mapper.findAllEnabled();
            if (all != null) {
                ConcurrentHashMap<String, SysCommConfig> newCache = new ConcurrentHashMap<>();
                for (SysCommConfig c : all) {
                    if (c.getId() != null) newCache.put(c.getId(), c);
                }
                cache.clear();
                cache.putAll(newCache);
                cacheLoadTime = System.currentTimeMillis();
                logger.info("接口配置缓存已加载,共{}条", cache.size());
            }
        } catch (Exception e) {
            logger.warn("加载接口配置缓存失败,将使用fallback兜底 | error={}", e.getMessage());
        }
    }

    /** 手动刷新缓存（配置更新后调用） */
    public static void refreshCache() {
        cacheLoadTime = 0L;
        ensureCacheLoaded();
    }

    /**
     * 获取接口URL：先从缓存读value，读不到用 fallback 兜底
     * @param configId   配置id
     * @param fallback   兜底URL(原写死URL)
     * @return 最终使用的URL
     */
    public static String getUrl(String configId, String fallback) {
        ensureCacheLoaded();
        SysCommConfig config = cache.get(configId);
        if (config != null && config.getValue() != null && !config.getValue().trim().isEmpty()) {
            return config.getValue().trim();
        }
        return fallback;
    }

    /**
     * 获取配置表中的兜底URL（fallback_url字段）
     * @param configId   配置id
     * @return fallback_url值，读不到返回null
     */
    public static String getFallbackUrl(String configId) {
        ensureCacheLoaded();
        SysCommConfig config = cache.get(configId);
        if (config != null && config.getFallbackUrl() != null && !config.getFallbackUrl().trim().isEmpty()) {
            return config.getFallbackUrl().trim();
        }
        return null;
    }

    /**
     * 获取完整配置对象
     * @param configId 配置id
     * @return SysCommConfig对象，读不到返回null
     */
    public static SysCommConfig getConfig(String configId) {
        ensureCacheLoaded();
        return cache.get(configId);
    }
}
