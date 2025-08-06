package com.multilevelcache.controller;

import com.multilevelcache.cache.MultiLevelCacheService;
import com.multilevelcache.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 緩存管理控制器
 * 提供緩存操作和監控功能
 * 
 * @author MultiLevelCache Team
 * @version 2.0.0
 */
@RestController
@RequestMapping("/api/cache")
@RequiredArgsConstructor
@Slf4j
public class CacheController {

    private final MultiLevelCacheService cacheService;

    /**
     * 獲取緩存統計信息
     */
    @GetMapping("/stats")
    public ApiResponse<Map<String, String>> getCacheStats() {
        long startTime = System.currentTimeMillis();
        
        try {
            Map<String, String> stats = new HashMap<>();
            stats.put("transactionCache", cacheService.getCacheStats("transactionCache"));
            stats.put("blacklistCache", cacheService.getCacheStats("blacklistCache"));
            
            return ApiResponse.<Map<String, String>>builder()
                    .success(true)
                    .message("緩存統計信息獲取成功")
                    .data(stats)
                    .responseTimeMs(System.currentTimeMillis() - startTime)
                    .cacheLevel("L1+L2")
                    .fromCache(false)
                    .build();
        } catch (Exception e) {
            log.error("獲取緩存統計信息失敗", e);
            return ApiResponse.<Map<String, String>>builder()
                    .success(false)
                    .message("獲取緩存統計信息失敗: " + e.getMessage())
                    .responseTimeMs(System.currentTimeMillis() - startTime)
                    .build();
        }
    }

    /**
     * 清空指定緩存
     */
    @DeleteMapping("/{cacheName}")
    public ApiResponse<Void> clearCache(@PathVariable String cacheName) {
        long startTime = System.currentTimeMillis();
        
        try {
            cacheService.clear(cacheName);
            
            return ApiResponse.<Void>builder()
                    .success(true)
                    .message("緩存清空成功: " + cacheName)
                    .responseTimeMs(System.currentTimeMillis() - startTime)
                    .cacheLevel("L1+L2")
                    .fromCache(false)
                    .build();
        } catch (Exception e) {
            log.error("清空緩存失敗: {}", cacheName, e);
            return ApiResponse.<Void>builder()
                    .success(false)
                    .message("清空緩存失敗: " + e.getMessage())
                    .responseTimeMs(System.currentTimeMillis() - startTime)
                    .build();
        }
    }

    /**
     * 刪除指定緩存項
     */
    @DeleteMapping("/{cacheName}/{key}")
    public ApiResponse<Void> evictCache(@PathVariable String cacheName, @PathVariable String key) {
        long startTime = System.currentTimeMillis();
        
        try {
            cacheService.evict(cacheName, key);
            
            return ApiResponse.<Void>builder()
                    .success(true)
                    .message("緩存項刪除成功: " + cacheName + ":" + key)
                    .responseTimeMs(System.currentTimeMillis() - startTime)
                    .cacheLevel("L1+L2")
                    .fromCache(false)
                    .build();
        } catch (Exception e) {
            log.error("刪除緩存項失敗: {}:{}", cacheName, key, e);
            return ApiResponse.<Void>builder()
                    .success(false)
                    .message("刪除緩存項失敗: " + e.getMessage())
                    .responseTimeMs(System.currentTimeMillis() - startTime)
                    .build();
        }
    }

    /**
     * 檢查緩存項是否存在
     */
    @GetMapping("/{cacheName}/{key}/exists")
    public ApiResponse<Boolean> checkCacheExists(@PathVariable String cacheName, @PathVariable String key) {
        long startTime = System.currentTimeMillis();
        
        try {
            boolean exists = cacheService.exists(cacheName, key);
            
            return ApiResponse.<Boolean>builder()
                    .success(true)
                    .message("緩存項檢查完成")
                    .data(exists)
                    .responseTimeMs(System.currentTimeMillis() - startTime)
                    .cacheLevel("L1+L2")
                    .fromCache(false)
                    .build();
        } catch (Exception e) {
            log.error("檢查緩存項失敗: {}:{}", cacheName, key, e);
            return ApiResponse.<Boolean>builder()
                    .success(false)
                    .message("檢查緩存項失敗: " + e.getMessage())
                    .responseTimeMs(System.currentTimeMillis() - startTime)
                    .build();
        }
    }

    /**
     * 手動存入緩存
     */
    @PostMapping("/{cacheName}/{key}")
    public ApiResponse<Void> putCache(@PathVariable String cacheName, 
                                    @PathVariable String key, 
                                    @RequestBody Object value) {
        long startTime = System.currentTimeMillis();
        
        try {
            cacheService.put(cacheName, key, value);
            
            return ApiResponse.<Void>builder()
                    .success(true)
                    .message("緩存項存入成功: " + cacheName + ":" + key)
                    .responseTimeMs(System.currentTimeMillis() - startTime)
                    .cacheLevel("L1+L2")
                    .fromCache(false)
                    .build();
        } catch (Exception e) {
            log.error("存入緩存項失敗: {}:{}", cacheName, key, e);
            return ApiResponse.<Void>builder()
                    .success(false)
                    .message("存入緩存項失敗: " + e.getMessage())
                    .responseTimeMs(System.currentTimeMillis() - startTime)
                    .build();
        }
    }

    /**
     * 獲取緩存項
     */
    @GetMapping("/{cacheName}/{key}")
    public ApiResponse<Object> getCache(@PathVariable String cacheName, @PathVariable String key) {
        long startTime = System.currentTimeMillis();
        
        try {
            Object value = cacheService.get(cacheName, key, Object.class);
            
            if (value != null) {
                return ApiResponse.<Object>builder()
                        .success(true)
                        .message("緩存項獲取成功")
                        .data(value)
                        .responseTimeMs(System.currentTimeMillis() - startTime)
                        .cacheLevel("L1+L2")
                        .fromCache(true)
                        .build();
            } else {
                return ApiResponse.<Object>builder()
                        .success(true)
                        .message("緩存項不存在")
                        .data(null)
                        .responseTimeMs(System.currentTimeMillis() - startTime)
                        .cacheLevel("L1+L2")
                        .fromCache(false)
                        .build();
            }
        } catch (Exception e) {
            log.error("獲取緩存項失敗: {}:{}", cacheName, key, e);
            return ApiResponse.<Object>builder()
                    .success(false)
                    .message("獲取緩存項失敗: " + e.getMessage())
                    .responseTimeMs(System.currentTimeMillis() - startTime)
                    .build();
        }
    }

    /**
     * 獲取緩存健康狀態
     */
    @GetMapping("/health")
    public ApiResponse<Map<String, Object>> getCacheHealth() {
        long startTime = System.currentTimeMillis();
        
        try {
            Map<String, Object> health = new HashMap<>();
            
            // 檢查 L1 緩存
            boolean l1Healthy = cacheService.exists("transactionCache", "health_check");
            health.put("l1Cache", l1Healthy ? "UP" : "DOWN");
            
            // 檢查 L2 緩存
            boolean l2Healthy = cacheService.exists("blacklistCache", "health_check");
            health.put("l2Cache", l2Healthy ? "UP" : "DOWN");
            
            // 整體健康狀態
            String overallStatus = (l1Healthy && l2Healthy) ? "UP" : "DEGRADED";
            health.put("status", overallStatus);
            health.put("timestamp", System.currentTimeMillis());
            
            return ApiResponse.<Map<String, Object>>builder()
                    .success(true)
                    .message("緩存健康檢查完成")
                    .data(health)
                    .responseTimeMs(System.currentTimeMillis() - startTime)
                    .cacheLevel("L1+L2")
                    .fromCache(false)
                    .build();
        } catch (Exception e) {
            log.error("緩存健康檢查失敗", e);
            return ApiResponse.<Map<String, Object>>builder()
                    .success(false)
                    .message("緩存健康檢查失敗: " + e.getMessage())
                    .responseTimeMs(System.currentTimeMillis() - startTime)
                    .build();
        }
    }
} 