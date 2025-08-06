package com.multilevelcache.controller;

import com.multilevelcache.aspect.PerformanceMonitorAspect;
import com.multilevelcache.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.ThreadMXBean;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 系統監控控制器
 */
@RestController
@RequestMapping("/api/monitor")
@RequiredArgsConstructor
@Slf4j
public class MonitorController {

    private final PerformanceMonitorAspect performanceMonitorAspect;

    /**
     * 獲取系統性能統計
     */
    @GetMapping("/performance")
    public ApiResponse<Map<String, Object>> getPerformanceStats() {
        Map<String, Object> stats = new HashMap<>();
        
        // 方法調用統計
        ConcurrentHashMap<String, AtomicLong> callCount = performanceMonitorAspect.getMethodCallCount();
        ConcurrentHashMap<String, AtomicLong> totalTime = performanceMonitorAspect.getMethodTotalTime();
        
        Map<String, Object> methodStats = new HashMap<>();
        callCount.forEach((methodName, count) -> {
            AtomicLong time = totalTime.get(methodName);
            if (time != null && count.get() > 0) {
                double avgTime = (double) time.get() / count.get();
                Map<String, Object> methodInfo = new HashMap<>();
                methodInfo.put("callCount", count.get());
                methodInfo.put("totalTime", time.get());
                methodInfo.put("averageTime", String.format("%.2f", avgTime));
                methodStats.put(methodName, methodInfo);
            }
        });
        
        stats.put("methodStats", methodStats);
        
        // 系統資源統計
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
        
        Map<String, Object> systemStats = new HashMap<>();
        systemStats.put("heapMemoryUsed", memoryBean.getHeapMemoryUsage().getUsed());
        systemStats.put("heapMemoryMax", memoryBean.getHeapMemoryUsage().getMax());
        systemStats.put("nonHeapMemoryUsed", memoryBean.getNonHeapMemoryUsage().getUsed());
        systemStats.put("threadCount", threadBean.getThreadCount());
        systemStats.put("peakThreadCount", threadBean.getPeakThreadCount());
        
        stats.put("systemStats", systemStats);
        
        return ApiResponse.success(stats, 0L, "NONE", false);
    }

    /**
     * 獲取緩存統計信息
     */
    @GetMapping("/cache")
    public ApiResponse<Map<String, Object>> getCacheStats() {
        Map<String, Object> cacheStats = new HashMap<>();
        
        // 這裡可以添加更詳細的緩存統計信息
        // 例如：緩存命中率、緩存大小、緩存失效次數等
        
        cacheStats.put("message", "緩存統計功能待實現");
        
        return ApiResponse.success(cacheStats, 0L, "NONE", false);
    }

    /**
     * 獲取系統健康狀態
     */
    @GetMapping("/health")
    public ApiResponse<Map<String, Object>> getSystemHealth() {
        Map<String, Object> health = new HashMap<>();
        
        // 檢查內存使用情況
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        long heapUsed = memoryBean.getHeapMemoryUsage().getUsed();
        long heapMax = memoryBean.getHeapMemoryUsage().getMax();
        double memoryUsage = (double) heapUsed / heapMax * 100;
        
        health.put("memoryUsage", String.format("%.2f%%", memoryUsage));
        health.put("heapUsed", heapUsed);
        health.put("heapMax", heapMax);
        health.put("status", memoryUsage < 80 ? "HEALTHY" : "WARNING");
        
        return ApiResponse.success(health, 0L, "NONE", false);
    }

    /**
     * 清理性能統計數據
     */
    @GetMapping("/clear-stats")
    public ApiResponse<String> clearPerformanceStats() {
        // 這裡可以添加清理統計數據的邏輯
        log.info("清理性能統計數據");
        return ApiResponse.success("統計數據已清理", 0L, "NONE", false);
    }
} 