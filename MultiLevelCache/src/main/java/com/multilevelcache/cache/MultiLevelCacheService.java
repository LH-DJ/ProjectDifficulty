package com.multilevelcache.cache;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 多級緩存服務類
 * 實現 L1 (Caffeine) + L2 (Redis) 二級緩存架構
 * 
 * @author MultiLevelCache Team
 * @version 2.0.0
 */
@Service
@Slf4j
public class MultiLevelCacheService {

    @Autowired
    @Qualifier("caffeineCacheManager")
    private CacheManager caffeineCacheManager;

    @Autowired
    @Qualifier("redisCacheManager")
    private CacheManager redisCacheManager;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 從二級緩存中獲取值
     * 查詢順序：L1 (Caffeine) -> L2 (Redis) -> 數據庫
     */
    public <T> T get(String cacheName, String key, Class<T> clazz) {
        long startTime = System.currentTimeMillis();
        
        try {
            // 1. 首先查詢 L1 緩存 (Caffeine)
            T l1Value = getFromL1Cache(cacheName, key, clazz);
            if (l1Value != null) {
                log.debug("🎯 L1 緩存命中: {} - {}", cacheName, key);
                return l1Value;
            }

            // 2. L1 未命中，查詢 L2 緩存 (Redis)
            T l2Value = getFromL2Cache(cacheName, key, clazz);
            if (l2Value != null) {
                log.debug("🎯 L2 緩存命中: {} - {}", cacheName, key);
                // 將 L2 的數據同步到 L1
                putToL1Cache(cacheName, key, l2Value);
                return l2Value;
            }

            log.debug("❌ 二級緩存均未命中: {} - {}", cacheName, key);
            return null;
        } finally {
            long endTime = System.currentTimeMillis();
            log.debug("⏱️ 二級緩存查詢耗時: {}ms - {} - {}", endTime - startTime, cacheName, key);
        }
    }

    /**
     * 將值存入二級緩存
     * 同時存入 L1 和 L2 緩存
     */
    public void put(String cacheName, String key, Object value) {
        long startTime = System.currentTimeMillis();
        
        try {
            // 同時存入 L1 和 L2 緩存
            putToL1Cache(cacheName, key, value);
            putToL2Cache(cacheName, key, value);
            
            log.debug("💾 數據已存入二級緩存: {} - {}", cacheName, key);
        } finally {
            long endTime = System.currentTimeMillis();
            log.debug("⏱️ 二級緩存存入耗時: {}ms - {} - {}", endTime - startTime, cacheName, key);
        }
    }

    /**
     * 從二級緩存中刪除值
     * 同時從 L1 和 L2 緩存中刪除
     */
    public void evict(String cacheName, String key) {
        long startTime = System.currentTimeMillis();
        
        try {
            // 同時從 L1 和 L2 緩存中刪除
            evictFromL1Cache(cacheName, key);
            evictFromL2Cache(cacheName, key);
            
            log.debug("🗑️ 數據已從二級緩存刪除: {} - {}", cacheName, key);
        } finally {
            long endTime = System.currentTimeMillis();
            log.debug("⏱️ 二級緩存刪除耗時: {}ms - {} - {}", endTime - startTime, cacheName, key);
        }
    }

    /**
     * 清空指定緩存
     * 同時清空 L1 和 L2 緩存
     */
    public void clear(String cacheName) {
        long startTime = System.currentTimeMillis();
        
        try {
            // 同時清空 L1 和 L2 緩存
            clearL1Cache(cacheName);
            clearL2Cache(cacheName);
            
            log.debug("🧹 二級緩存已清空: {}", cacheName);
        } finally {
            long endTime = System.currentTimeMillis();
            log.debug("⏱️ 二級緩存清空耗時: {}ms - {}", endTime - startTime, cacheName);
        }
    }

    /**
     * 檢查緩存是否存在
     * 檢查 L1 緩存，如果不存在則檢查 L2 緩存
     */
    public boolean exists(String cacheName, String key) {
        // 先檢查 L1 緩存
        if (existsInL1Cache(cacheName, key)) {
            return true;
        }
        
        // 再檢查 L2 緩存
        return existsInL2Cache(cacheName, key);
    }

    /**
     * 獲取緩存統計信息
     */
    public String getCacheStats(String cacheName) {
        StringBuilder stats = new StringBuilder();
        
        // L1 緩存統計
        Cache l1Cache = caffeineCacheManager.getCache(cacheName);
        if (l1Cache != null) {
            stats.append("L1 緩存 (Caffeine) 統計: ").append(cacheName).append("\n");
            // 這裡可以添加更多統計信息
        }

        // L2 緩存統計
        Cache l2Cache = redisCacheManager.getCache(cacheName);
        if (l2Cache != null) {
            stats.append("L2 緩存 (Redis) 統計: ").append(cacheName).append("\n");
            // 這裡可以添加更多統計信息
        }

        return stats.toString();
    }

    // ==================== L1 緩存操作 (Caffeine) ====================

    /**
     * 從 L1 緩存獲取數據
     */
    private <T> T getFromL1Cache(String cacheName, String key, Class<T> clazz) {
        try {
            Cache cache = caffeineCacheManager.getCache(cacheName);
            if (cache != null) {
                Cache.ValueWrapper value = cache.get(key);
                if (value != null) {
                    return clazz.cast(value.get());
                }
            }
        } catch (Exception e) {
            log.warn("L1 緩存查詢異常: {} - {}", cacheName, key, e);
        }
        return null;
    }

    /**
     * 存入 L1 緩存
     */
    private void putToL1Cache(String cacheName, String key, Object value) {
        try {
            Cache cache = caffeineCacheManager.getCache(cacheName);
            if (cache != null) {
                cache.put(key, value);
            }
        } catch (Exception e) {
            log.warn("L1 緩存存入異常: {} - {}", cacheName, key, e);
        }
    }

    /**
     * 從 L1 緩存刪除
     */
    private void evictFromL1Cache(String cacheName, String key) {
        try {
            Cache cache = caffeineCacheManager.getCache(cacheName);
            if (cache != null) {
                cache.evict(key);
            }
        } catch (Exception e) {
            log.warn("L1 緩存刪除異常: {} - {}", cacheName, key, e);
        }
    }

    /**
     * 清空 L1 緩存
     */
    private void clearL1Cache(String cacheName) {
        try {
            Cache cache = caffeineCacheManager.getCache(cacheName);
            if (cache != null) {
                cache.clear();
            }
        } catch (Exception e) {
            log.warn("L1 緩存清空異常: {}", cacheName, e);
        }
    }

    /**
     * 檢查 L1 緩存是否存在
     */
    private boolean existsInL1Cache(String cacheName, String key) {
        try {
            Cache cache = caffeineCacheManager.getCache(cacheName);
            return cache != null && cache.get(key) != null;
        } catch (Exception e) {
            log.warn("L1 緩存檢查異常: {} - {}", cacheName, key, e);
            return false;
        }
    }

    // ==================== L2 緩存操作 (Redis) ====================

    /**
     * 從 L2 緩存獲取數據
     */
    private <T> T getFromL2Cache(String cacheName, String key, Class<T> clazz) {
        try {
            String redisKey = buildRedisKey(cacheName, key);
            Object value = redisTemplate.opsForValue().get(redisKey);
            if (value != null) {
                return clazz.cast(value);
            }
        } catch (Exception e) {
            log.warn("L2 緩存查詢異常: {} - {}", cacheName, key, e);
        }
        return null;
    }

    /**
     * 存入 L2 緩存
     */
    private void putToL2Cache(String cacheName, String key, Object value) {
        try {
            String redisKey = buildRedisKey(cacheName, key);
            // 根據緩存類型設置不同的過期時間
            long expireTime = getExpireTimeForCache(cacheName);
            redisTemplate.opsForValue().set(redisKey, value, expireTime, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.warn("L2 緩存存入異常: {} - {}", cacheName, key, e);
        }
    }

    /**
     * 從 L2 緩存刪除
     */
    private void evictFromL2Cache(String cacheName, String key) {
        try {
            String redisKey = buildRedisKey(cacheName, key);
            redisTemplate.delete(redisKey);
        } catch (Exception e) {
            log.warn("L2 緩存刪除異常: {} - {}", cacheName, key, e);
        }
    }

    /**
     * 清空 L2 緩存
     */
    private void clearL2Cache(String cacheName) {
        try {
            String pattern = buildRedisKey(cacheName, "*");
            Set<String> keys = redisTemplate.keys(pattern);
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
            }
        } catch (Exception e) {
            log.warn("L2 緩存清空異常: {}", cacheName, e);
        }
    }

    /**
     * 檢查 L2 緩存是否存在
     */
    private boolean existsInL2Cache(String cacheName, String key) {
        try {
            String redisKey = buildRedisKey(cacheName, key);
            return Boolean.TRUE.equals(redisTemplate.hasKey(redisKey));
        } catch (Exception e) {
            log.warn("L2 緩存檢查異常: {} - {}", cacheName, key, e);
            return false;
        }
    }

    // ==================== 輔助方法 ====================

    /**
     * 構建 Redis 鍵
     */
    private String buildRedisKey(String cacheName, String key) {
        return cacheName + ":" + key;
    }

    /**
     * 根據緩存類型獲取過期時間（秒）
     */
    private long getExpireTimeForCache(String cacheName) {
        switch (cacheName) {
            case "transactionCache":
                return 2 * 60 * 60; // 2 小時
            case "blacklistCache":
                return 4 * 60 * 60; // 4 小時
            default:
                return 60 * 60; // 1 小時
        }
    }
} 