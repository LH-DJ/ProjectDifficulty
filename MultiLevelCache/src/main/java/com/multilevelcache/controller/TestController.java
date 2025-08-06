package com.multilevelcache.controller;

import com.multilevelcache.dto.ApiResponse;
import com.multilevelcache.entity.Transaction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 測試控制器
 * 用於驗證 LocalDateTime 序列化等功能
 * 
 * @author MultiLevelCache Team
 * @version 2.0.0
 */
@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
@Slf4j
public class TestController {

    /**
     * 測試 LocalDateTime 序列化
     */
    @GetMapping("/datetime")
    public ApiResponse<Map<String, Object>> testDateTime() {
        long startTime = System.currentTimeMillis();
        
        try {
            Map<String, Object> result = new HashMap<>();
            
            // 創建測試數據
            Transaction transaction = new Transaction();
            transaction.setTransactionId("TEST001");
            transaction.setUserId("USER001");
            transaction.setAccountNumber("ACC001");
            transaction.setTransactionType("TRANSFER");
            transaction.setAmount(new BigDecimal("100.00"));
            transaction.setCurrency("USD");
            transaction.setDescription("測試交易");
            transaction.setStatus("SUCCESS");
            transaction.setCreatedAt(LocalDateTime.now());
            transaction.setUpdatedAt(LocalDateTime.now());
            
            result.put("transaction", transaction);
            result.put("currentTime", LocalDateTime.now());
            result.put("testMessage", "LocalDateTime 序列化測試成功");
            
            return ApiResponse.<Map<String, Object>>builder()
                    .success(true)
                    .message("LocalDateTime 序列化測試成功")
                    .data(result)
                    .responseTimeMs(System.currentTimeMillis() - startTime)
                    .cacheLevel("N/A")
                    .fromCache(false)
                    .build();
        } catch (Exception e) {
            log.error("LocalDateTime 序列化測試失敗", e);
            return ApiResponse.<Map<String, Object>>builder()
                    .success(false)
                    .message("LocalDateTime 序列化測試失敗: " + e.getMessage())
                    .responseTimeMs(System.currentTimeMillis() - startTime)
                    .build();
        }
    }

    /**
     * 測試 JSON 序列化
     */
    @PostMapping("/json")
    public ApiResponse<Object> testJsonSerialization(@RequestBody Object request) {
        long startTime = System.currentTimeMillis();
        
        try {
            return ApiResponse.<Object>builder()
                    .success(true)
                    .message("JSON 序列化測試成功")
                    .data(request)
                    .responseTimeMs(System.currentTimeMillis() - startTime)
                    .cacheLevel("N/A")
                    .fromCache(false)
                    .build();
        } catch (Exception e) {
            log.error("JSON 序列化測試失敗", e);
            return ApiResponse.<Object>builder()
                    .success(false)
                    .message("JSON 序列化測試失敗: " + e.getMessage())
                    .responseTimeMs(System.currentTimeMillis() - startTime)
                    .build();
        }
    }
} 