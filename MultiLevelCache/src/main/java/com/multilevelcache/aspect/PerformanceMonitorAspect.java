package com.multilevelcache.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 性能監控切面
 */
@Aspect
@Component
@Slf4j
public class PerformanceMonitorAspect {

    private final ConcurrentHashMap<String, AtomicLong> methodCallCount = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, AtomicLong> methodTotalTime = new ConcurrentHashMap<>();

    /**
     * 監控服務層方法的執行時間
     */
    @Around("execution(* com.multilevelcache.service.*.*(..))")
    public Object monitorServiceMethods(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String fullMethodName = className + "." + methodName;

        long startTime = System.currentTimeMillis();
        try {
            Object result = joinPoint.proceed();
            long executionTime = System.currentTimeMillis() - startTime;
            
            // 記錄統計信息
            recordMethodStats(fullMethodName, executionTime);
            
            // 如果執行時間超過閾值，記錄警告
            if (executionTime > 200) {
                log.warn("方法 {} 執行時間過長: {}ms", fullMethodName, executionTime);
            }
            
            return result;
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            log.error("方法 {} 執行異常，耗時: {}ms", fullMethodName, executionTime, e);
            throw e;
        }
    }

    /**
     * 監控緩存操作的執行時間
     */
    @Around("execution(* com.multilevelcache.cache.*.*(..))")
    public Object monitorCacheMethods(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String fullMethodName = className + "." + methodName;

        long startTime = System.currentTimeMillis();
        try {
            Object result = joinPoint.proceed();
            long executionTime = System.currentTimeMillis() - startTime;
            
            // 記錄統計信息
            recordMethodStats(fullMethodName, executionTime);
            
            // 緩存操作應該很快，如果超過50ms就記錄警告
            if (executionTime > 50) {
                log.warn("緩存操作 {} 執行時間過長: {}ms", fullMethodName, executionTime);
            }
            
            return result;
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            log.error("緩存操作 {} 執行異常，耗時: {}ms", fullMethodName, executionTime, e);
            throw e;
        }
    }

    /**
     * 記錄方法統計信息
     */
    private void recordMethodStats(String methodName, long executionTime) {
        methodCallCount.computeIfAbsent(methodName, k -> new AtomicLong(0)).incrementAndGet();
        methodTotalTime.computeIfAbsent(methodName, k -> new AtomicLong(0)).addAndGet(executionTime);
    }

    /**
     * 獲取方法統計信息
     */
    public ConcurrentHashMap<String, AtomicLong> getMethodCallCount() {
        return methodCallCount;
    }

    /**
     * 獲取方法總執行時間
     */
    public ConcurrentHashMap<String, AtomicLong> getMethodTotalTime() {
        return methodTotalTime;
    }

    /**
     * 獲取方法平均執行時間
     */
    public double getAverageExecutionTime(String methodName) {
        AtomicLong callCount = methodCallCount.get(methodName);
        AtomicLong totalTime = methodTotalTime.get(methodName);
        
        if (callCount != null && totalTime != null && callCount.get() > 0) {
            return (double) totalTime.get() / callCount.get();
        }
        return 0.0;
    }
} 