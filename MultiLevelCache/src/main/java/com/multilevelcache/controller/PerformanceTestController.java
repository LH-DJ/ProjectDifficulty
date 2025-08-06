package com.multilevelcache.controller;

import com.multilevelcache.dto.ApiResponse;
import com.multilevelcache.service.BlacklistService;
import com.multilevelcache.service.TransactionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 性能測試控制器
 * 
 * @author MultiLevelCache Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/performance")
@Slf4j
public class PerformanceTestController {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private BlacklistService blacklistService;

    /**
     * 交易記錄查詢性能測試
     */
    @GetMapping("/test/transactions")
    public ApiResponse<PerformanceTestResult> testTransactionPerformance(
            @RequestParam(defaultValue = "100") int requestCount,
            @RequestParam(defaultValue = "10") int concurrentThreads) {
        
        log.info("🚀 開始交易記錄查詢性能測試: {} 請求, {} 並發線程", requestCount, concurrentThreads);
        
        PerformanceTestResult result = new PerformanceTestResult();
        result.setTestType("交易記錄查詢");
        result.setRequestCount(requestCount);
        result.setConcurrentThreads(concurrentThreads);
        
        List<Long> responseTimes = new ArrayList<>();
        ExecutorService executor = Executors.newFixedThreadPool(concurrentThreads);
        
        try {
            // 生成測試數據
            List<String> transactionIds = generateTestTransactionIds(requestCount);
            
            // 並發執行查詢
            List<CompletableFuture<Long>> futures = new ArrayList<>();
            
            for (String transactionId : transactionIds) {
                CompletableFuture<Long> future = CompletableFuture.supplyAsync(() -> {
                    long startTime = System.currentTimeMillis();
                    transactionService.getTransactionById(transactionId);
                    return System.currentTimeMillis() - startTime;
                }, executor);
                
                futures.add(future);
            }
            
            // 收集結果
            for (CompletableFuture<Long> future : futures) {
                responseTimes.add(future.get());
            }
            
            // 計算統計數據
            calculateStatistics(result, responseTimes);
            
            log.info("✅ 交易記錄查詢性能測試完成: 平均響應時間 {}ms", result.getAverageResponseTime());
            
        } catch (Exception e) {
            log.error("❌ 交易記錄查詢性能測試失敗", e);
            return ApiResponse.error("性能測試失敗: " + e.getMessage());
        } finally {
            executor.shutdown();
        }
        
        return ApiResponse.success(result);
    }

    /**
     * 黑名單檢查性能測試
     */
    @GetMapping("/test/blacklist")
    public ApiResponse<PerformanceTestResult> testBlacklistPerformance(
            @RequestParam(defaultValue = "100") int requestCount,
            @RequestParam(defaultValue = "10") int concurrentThreads) {
        
        log.info("🚀 開始黑名單檢查性能測試: {} 請求, {} 並發線程", requestCount, concurrentThreads);
        
        PerformanceTestResult result = new PerformanceTestResult();
        result.setTestType("黑名單檢查");
        result.setRequestCount(requestCount);
        result.setConcurrentThreads(concurrentThreads);
        
        List<Long> responseTimes = new ArrayList<>();
        ExecutorService executor = Executors.newFixedThreadPool(concurrentThreads);
        
        try {
            // 生成測試數據
            List<String> userIds = generateTestUserIds(requestCount);
            
            // 並發執行查詢
            List<CompletableFuture<Long>> futures = new ArrayList<>();
            
            for (String userId : userIds) {
                CompletableFuture<Long> future = CompletableFuture.supplyAsync(() -> {
                    long startTime = System.currentTimeMillis();
                    blacklistService.checkUserBlacklist(userId);
                    return System.currentTimeMillis() - startTime;
                }, executor);
                
                futures.add(future);
            }
            
            // 收集結果
            for (CompletableFuture<Long> future : futures) {
                responseTimes.add(future.get());
            }
            
            // 計算統計數據
            calculateStatistics(result, responseTimes);
            
            log.info("✅ 黑名單檢查性能測試完成: 平均響應時間 {}ms", result.getAverageResponseTime());
            
        } catch (Exception e) {
            log.error("❌ 黑名單檢查性能測試失敗", e);
            return ApiResponse.error("性能測試失敗: " + e.getMessage());
        } finally {
            executor.shutdown();
        }
        
        return ApiResponse.success(result);
    }

    /**
     * 綜合性能測試
     */
    @GetMapping("/test/comprehensive")
    public ApiResponse<ComprehensiveTestResult> testComprehensivePerformance(
            @RequestParam(defaultValue = "50") int requestCount,
            @RequestParam(defaultValue = "10") int concurrentThreads) {
        
        log.info("🚀 開始綜合性能測試: {} 請求, {} 並發線程", requestCount, concurrentThreads);
        
        ComprehensiveTestResult result = new ComprehensiveTestResult();
        result.setRequestCount(requestCount);
        result.setConcurrentThreads(concurrentThreads);
        
        // 測試交易記錄查詢
        ApiResponse<PerformanceTestResult> transactionResult = testTransactionPerformance(requestCount, concurrentThreads);
        if (transactionResult.getSuccess()) {
            result.setTransactionTest(transactionResult.getData());
        }
        
        // 測試黑名單檢查
        ApiResponse<PerformanceTestResult> blacklistResult = testBlacklistPerformance(requestCount, concurrentThreads);
        if (blacklistResult.getSuccess()) {
            result.setBlacklistTest(blacklistResult.getData());
        }
        
        // 計算綜合統計
        if (result.getTransactionTest() != null && result.getBlacklistTest() != null) {
            double avgTransactionTime = result.getTransactionTest().getAverageResponseTime();
            double avgBlacklistTime = result.getBlacklistTest().getAverageResponseTime();
            double totalAvgTime = (avgTransactionTime + avgBlacklistTime) / 2;
            
            result.setOverallAverageResponseTime(totalAvgTime);
            result.setTargetAchieved(totalAvgTime <= 200); // 目標200ms
        }
        
        log.info("✅ 綜合性能測試完成: 整體平均響應時間 {}ms", result.getOverallAverageResponseTime());
        
        return ApiResponse.success(result);
    }

    /**
     * 生成測試交易ID
     */
    private List<String> generateTestTransactionIds(int count) {
        List<String> ids = new ArrayList<>();
        Random random = new Random();
        
        for (int i = 0; i < count; i++) {
            int transactionNumber = random.nextInt(1000) + 1;
            ids.add("TXN_" + String.format("%06d", transactionNumber));
        }
        
        return ids;
    }

    /**
     * 生成測試用戶ID
     */
    private List<String> generateTestUserIds(int count) {
        List<String> ids = new ArrayList<>();
        Random random = new Random();
        
        for (int i = 0; i < count; i++) {
            int userNumber = random.nextInt(50) + 1;
            ids.add("USER_" + userNumber);
        }
        
        return ids;
    }

    /**
     * 計算統計數據
     */
    private void calculateStatistics(PerformanceTestResult result, List<Long> responseTimes) {
        if (responseTimes.isEmpty()) {
            return;
        }
        
        // 計算平均值
        double average = responseTimes.stream()
                .mapToLong(Long::longValue)
                .average()
                .orElse(0.0);
        
        // 計算最小值
        long min = responseTimes.stream()
                .mapToLong(Long::longValue)
                .min()
                .orElse(0);
        
        // 計算最大值
        long max = responseTimes.stream()
                .mapToLong(Long::longValue)
                .max()
                .orElse(0);
        
        // 計算中位數
        responseTimes.sort(Long::compareTo);
        long median = responseTimes.get(responseTimes.size() / 2);
        
        // 計算95%分位數
        int percentile95Index = (int) (responseTimes.size() * 0.95);
        long percentile95 = responseTimes.get(Math.min(percentile95Index, responseTimes.size() - 1));
        
        result.setAverageResponseTime(average);
        result.setMinResponseTime(min);
        result.setMaxResponseTime(max);
        result.setMedianResponseTime(median);
        result.setPercentile95ResponseTime(percentile95);
        result.setTargetAchieved(average <= 200); // 目標200ms
    }

    /**
     * 性能測試結果類
     */
    public static class PerformanceTestResult {
        private String testType;
        private int requestCount;
        private int concurrentThreads;
        private double averageResponseTime;
        private long minResponseTime;
        private long maxResponseTime;
        private long medianResponseTime;
        private long percentile95ResponseTime;
        private boolean targetAchieved;

        // Getters and Setters
        public String getTestType() { return testType; }
        public void setTestType(String testType) { this.testType = testType; }
        
        public int getRequestCount() { return requestCount; }
        public void setRequestCount(int requestCount) { this.requestCount = requestCount; }
        
        public int getConcurrentThreads() { return concurrentThreads; }
        public void setConcurrentThreads(int concurrentThreads) { this.concurrentThreads = concurrentThreads; }
        
        public double getAverageResponseTime() { return averageResponseTime; }
        public void setAverageResponseTime(double averageResponseTime) { this.averageResponseTime = averageResponseTime; }
        
        public long getMinResponseTime() { return minResponseTime; }
        public void setMinResponseTime(long minResponseTime) { this.minResponseTime = minResponseTime; }
        
        public long getMaxResponseTime() { return maxResponseTime; }
        public void setMaxResponseTime(long maxResponseTime) { this.maxResponseTime = maxResponseTime; }
        
        public long getMedianResponseTime() { return medianResponseTime; }
        public void setMedianResponseTime(long medianResponseTime) { this.medianResponseTime = medianResponseTime; }
        
        public long getPercentile95ResponseTime() { return percentile95ResponseTime; }
        public void setPercentile95ResponseTime(long percentile95ResponseTime) { this.percentile95ResponseTime = percentile95ResponseTime; }
        
        public boolean isTargetAchieved() { return targetAchieved; }
        public void setTargetAchieved(boolean targetAchieved) { this.targetAchieved = targetAchieved; }
    }

    /**
     * 綜合性能測試結果類
     */
    public static class ComprehensiveTestResult {
        private int requestCount;
        private int concurrentThreads;
        private PerformanceTestResult transactionTest;
        private PerformanceTestResult blacklistTest;
        private double overallAverageResponseTime;
        private boolean targetAchieved;

        // Getters and Setters
        public int getRequestCount() { return requestCount; }
        public void setRequestCount(int requestCount) { this.requestCount = requestCount; }
        
        public int getConcurrentThreads() { return concurrentThreads; }
        public void setConcurrentThreads(int concurrentThreads) { this.concurrentThreads = concurrentThreads; }
        
        public PerformanceTestResult getTransactionTest() { return transactionTest; }
        public void setTransactionTest(PerformanceTestResult transactionTest) { this.transactionTest = transactionTest; }
        
        public PerformanceTestResult getBlacklistTest() { return blacklistTest; }
        public void setBlacklistTest(PerformanceTestResult blacklistTest) { this.blacklistTest = blacklistTest; }
        
        public double getOverallAverageResponseTime() { return overallAverageResponseTime; }
        public void setOverallAverageResponseTime(double overallAverageResponseTime) { this.overallAverageResponseTime = overallAverageResponseTime; }
        
        public boolean isTargetAchieved() { return targetAchieved; }
        public void setTargetAchieved(boolean targetAchieved) { this.targetAchieved = targetAchieved; }
    }
} 