package com.multilevelcache.cache;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

/**
 * 多級緩存服務類
 * 
 * @author MultiLevelCache Team
 * @version 1.0.0
 */
@Service
@Slf4j
public class MultiLevelCacheService {

    @Autowired
    @Qualifier("caffeineCacheManager")
    private CacheManager caffeineCacheManager;

    /**
     * 從緩存中獲取值
     */
    public <T> T get(String cacheName, String key, Class<T> clazz) {
        long startTime = System.currentTimeMillis();
        
        try {
            Cache caffeineCache = caffeineCacheManager.getCache(cacheName);
            if (caffeineCache != null) {
                Cache.ValueWrapper caffeineValue = caffeineCache.get(key);
                if (caffeineValue != null) {
                    log.debug("🎯 從Caffeine緩存命中: {} - {}", cacheName, key);
                    return clazz.cast(caffeineValue.get());
                }
            }

            log.debug("❌ 緩存未命中: {} - {}", cacheName, key);
            return null;
        } finally {
            long endTime = System.currentTimeMillis();
            log.debug("⏱️ 緩存查詢耗時: {}ms - {} - {}", endTime - startTime, cacheName, key);
        }
    }

    /**
     * 將值存入緩存
     */
    public void put(String cacheName, String key, Object value) {
        long startTime = System.currentTimeMillis();
        
        try {
            Cache caffeineCache = caffeineCacheManager.getCache(cacheName);
            if (caffeineCache != null) {
                caffeineCache.put(key, value);
                log.debug("💾 存入Caffeine緩存: {} - {}", cacheName, key);
            }
        } finally {
            long endTime = System.currentTimeMillis();
            log.debug("⏱️ 緩存存入耗時: {}ms - {} - {}", endTime - startTime, cacheName, key);
        }
    }

    /**
     * 從緩存中刪除值
     */
    public void evict(String cacheName, String key) {
        long startTime = System.currentTimeMillis();
        
        try {
            Cache caffeineCache = caffeineCacheManager.getCache(cacheName);
            if (caffeineCache != null) {
                caffeineCache.evict(key);
                log.debug("🗑️ 從Caffeine緩存刪除: {} - {}", cacheName, key);
            }
        } finally {
            long endTime = System.currentTimeMillis();
            log.debug("⏱️ 緩存刪除耗時: {}ms - {} - {}", endTime - startTime, cacheName, key);
        }
    }

    /**
     * 清空指定緩存
     */
    public void clear(String cacheName) {
        long startTime = System.currentTimeMillis();
        
        try {
            Cache caffeineCache = caffeineCacheManager.getCache(cacheName);
            if (caffeineCache != null) {
                caffeineCache.clear();
                log.debug("🧹 清空Caffeine緩存: {}", cacheName);
            }
        } finally {
            long endTime = System.currentTimeMillis();
            log.debug("⏱️ 緩存清空耗時: {}ms - {}", endTime - startTime, cacheName);
        }
    }

    /**
     * 檢查緩存是否存在
     */
    public boolean exists(String cacheName, String key) {
        Cache caffeineCache = caffeineCacheManager.getCache(cacheName);
        return caffeineCache != null && caffeineCache.get(key) != null;
    }

    /**
     * 獲取緩存統計信息
     */
    public String getCacheStats(String cacheName) {
        StringBuilder stats = new StringBuilder();
        
        Cache caffeineCache = caffeineCacheManager.getCache(cacheName);
        if (caffeineCache != null) {
            stats.append("Caffeine緩存統計: ").append(cacheName).append("\n");
            // 這裡可以添加更多統計信息
        }

        return stats.toString();
    }
} 