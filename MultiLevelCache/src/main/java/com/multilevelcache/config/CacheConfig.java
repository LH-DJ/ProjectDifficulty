package com.multilevelcache.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.concurrent.TimeUnit;

/**
 * 緩存配置類
 */
@Configuration
@EnableCaching
public class CacheConfig {

    @Value("${cache.caffeine.transaction.maximum-size:1000}")
    private int transactionMaxSize;

    @Value("${cache.caffeine.transaction.expire-after-write:30m}")
    private String transactionExpireAfterWrite;

    @Value("${cache.caffeine.transaction.expire-after-access:10m}")
    private String transactionExpireAfterAccess;

    @Value("${cache.caffeine.blacklist.maximum-size:500}")
    private int blacklistMaxSize;

    @Value("${cache.caffeine.blacklist.expire-after-write:60m}")
    private String blacklistExpireAfterWrite;

    @Value("${cache.caffeine.blacklist.expire-after-access:20m}")
    private String blacklistExpireAfterAccess;

    /**
     * Caffeine 緩存管理器（L1 緩存）
     */
    @Bean
    @Primary
    public CacheManager caffeineCacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        
        // 交易記錄緩存
        cacheManager.registerCustomCache("transactionCache", 
            Caffeine.newBuilder()
                .maximumSize(transactionMaxSize)
                .expireAfterWrite(parseDuration(transactionExpireAfterWrite), TimeUnit.MILLISECONDS)
                .expireAfterAccess(parseDuration(transactionExpireAfterAccess), TimeUnit.MILLISECONDS)
                .build());
        
        // 黑名單緩存
        cacheManager.registerCustomCache("blacklistCache", 
            Caffeine.newBuilder()
                .maximumSize(blacklistMaxSize)
                .expireAfterWrite(parseDuration(blacklistExpireAfterWrite), TimeUnit.MILLISECONDS)
                .expireAfterAccess(parseDuration(blacklistExpireAfterAccess), TimeUnit.MILLISECONDS)
                .build());
        
        System.out.println("✅ CacheManager 配置成功！");
        System.out.println("📦 已配置緩存: transactionCache, blacklistCache");
        
        return cacheManager;
    }

    /**
     * 解析時間字符串為毫秒
     */
    private long parseDuration(String duration) {
        if (duration.endsWith("s")) {
            return Long.parseLong(duration.substring(0, duration.length() - 1)) * 1000;
        } else if (duration.endsWith("m")) {
            return Long.parseLong(duration.substring(0, duration.length() - 1)) * 60 * 1000;
        } else if (duration.endsWith("h")) {
            return Long.parseLong(duration.substring(0, duration.length() - 1)) * 60 * 60 * 1000;
        } else {
            return Long.parseLong(duration);
        }
    }
} 