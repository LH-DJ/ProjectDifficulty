package com.multilevelcache.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Redis 緩存配置類 (L2 緩存)
 * 
 * @author MultiLevelCache Team
 * @version 2.0.0
 */
@Configuration
@EnableCaching
public class RedisCacheConfig {

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${cache.redis.transaction.expire-after-write:2h}")
    private String transactionExpireAfterWrite;

    @Value("${cache.redis.transaction.expire-after-access:1h}")
    private String transactionExpireAfterAccess;

    @Value("${cache.redis.blacklist.expire-after-write:4h}")
    private String blacklistExpireAfterWrite;

    @Value("${cache.redis.blacklist.expire-after-access:2h}")
    private String blacklistExpireAfterAccess;

    /**
     * Redis 緩存管理器 (L2 緩存)
     */
    @Bean("redisCacheManager")
    public CacheManager redisCacheManager(RedisConnectionFactory connectionFactory) {
        // 使用全局配置的 ObjectMapper
        GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer(objectMapper);
        StringRedisSerializer stringSerializer = new StringRedisSerializer();

        // 默認緩存配置
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofHours(1)) // 默認 1 小時過期
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(stringSerializer))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(jsonSerializer))
                .disableCachingNullValues();

        // 為不同緩存配置不同的過期時間
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
        
        // 交易記錄緩存配置
        cacheConfigurations.put("transactionCache", defaultConfig
                .entryTtl(parseDuration(transactionExpireAfterWrite))
                .prefixCacheNameWith("transaction:"));

        // 黑名單緩存配置
        cacheConfigurations.put("blacklistCache", defaultConfig
                .entryTtl(parseDuration(blacklistExpireAfterWrite))
                .prefixCacheNameWith("blacklist:"));

        RedisCacheManager cacheManager = RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .build();

        System.out.println("✅ Redis CacheManager 配置成功！");
        System.out.println("📦 已配置 Redis 緩存: transactionCache, blacklistCache");
        
        return cacheManager;
    }

    /**
     * Redis Template 配置
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // 使用全局配置的 ObjectMapper
        GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer(objectMapper);
        StringRedisSerializer stringSerializer = new StringRedisSerializer();

        // 設置序列化器
        template.setKeySerializer(stringSerializer);
        template.setValueSerializer(jsonSerializer);
        template.setHashKeySerializer(stringSerializer);
        template.setHashValueSerializer(jsonSerializer);
        
        template.afterPropertiesSet();
        
        return template;
    }

    /**
     * 解析時間字符串為 Duration
     */
    private Duration parseDuration(String duration) {
        if (duration.endsWith("s")) {
            return Duration.ofSeconds(Long.parseLong(duration.substring(0, duration.length() - 1)));
        } else if (duration.endsWith("m")) {
            return Duration.ofMinutes(Long.parseLong(duration.substring(0, duration.length() - 1)));
        } else if (duration.endsWith("h")) {
            return Duration.ofHours(Long.parseLong(duration.substring(0, duration.length() - 1)));
        } else {
            return Duration.ofSeconds(Long.parseLong(duration));
        }
    }
} 