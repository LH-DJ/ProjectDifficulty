package com.multilevelcache.controller;

import com.multilevelcache.dto.ApiResponse;
import com.multilevelcache.dto.BlacklistDTO;
import com.multilevelcache.entity.Blacklist;
import com.multilevelcache.service.BlacklistService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 黑名單控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/blacklist")
@RequiredArgsConstructor
public class BlacklistController {

    private final BlacklistService blacklistService;

    /**
     * 檢查用戶黑名單
     */
    @GetMapping("/check/user/{userId}")
    public ApiResponse<BlacklistDTO> checkUserBlacklist(@PathVariable String userId) {
        long startTime = System.currentTimeMillis();
        BlacklistDTO blacklist = blacklistService.checkUserBlacklist(userId);
        long responseTime = System.currentTimeMillis() - startTime;
        
        return ApiResponse.success(blacklist, responseTime, blacklist.getCacheLevel(), blacklist.getFromCache());
    }

    /**
     * 檢查賬戶黑名單
     */
    @GetMapping("/check/account/{accountNumber}")
    public ApiResponse<BlacklistDTO> checkAccountBlacklist(@PathVariable String accountNumber) {
        long startTime = System.currentTimeMillis();
        BlacklistDTO blacklist = blacklistService.checkAccountBlacklist(accountNumber);
        long responseTime = System.currentTimeMillis() - startTime;
        
        return ApiResponse.success(blacklist, responseTime, blacklist.getCacheLevel(), blacklist.getFromCache());
    }

    /**
     * 根據用戶ID查詢黑名單記錄
     */
    @GetMapping("/user/{userId}")
    public ApiResponse<List<BlacklistDTO>> getBlacklistByUserId(@PathVariable String userId) {
        long startTime = System.currentTimeMillis();
        List<BlacklistDTO> blacklists = blacklistService.getBlacklistByUserId(userId);
        long responseTime = System.currentTimeMillis() - startTime;
        
        if (!blacklists.isEmpty()) {
            return ApiResponse.success(blacklists, responseTime, "L1", true);
        } else {
            return ApiResponse.success(blacklists, responseTime, "DB", false);
        }
    }

    /**
     * 根據賬戶號碼查詢黑名單記錄
     */
    @GetMapping("/account/{accountNumber}")
    public ApiResponse<List<BlacklistDTO>> getBlacklistByAccountNumber(@PathVariable String accountNumber) {
        long startTime = System.currentTimeMillis();
        List<BlacklistDTO> blacklists = blacklistService.getBlacklistByAccountNumber(accountNumber);
        long responseTime = System.currentTimeMillis() - startTime;
        
        if (!blacklists.isEmpty()) {
            return ApiResponse.success(blacklists, responseTime, "L1", true);
        } else {
            return ApiResponse.success(blacklists, responseTime, "DB", false);
        }
    }

    /**
     * 根據黑名單類型查詢
     */
    @GetMapping("/type/{blacklistType}")
    public ApiResponse<List<BlacklistDTO>> getBlacklistsByType(@PathVariable String blacklistType) {
        long startTime = System.currentTimeMillis();
        List<BlacklistDTO> blacklists = blacklistService.getBlacklistsByType(blacklistType);
        long responseTime = System.currentTimeMillis() - startTime;
        
        if (!blacklists.isEmpty()) {
            return ApiResponse.success(blacklists, responseTime, "L1", true);
        } else {
            return ApiResponse.success(blacklists, responseTime, "DB", false);
        }
    }

    /**
     * 查詢有效的黑名單記錄
     */
    @GetMapping("/active")
    public ApiResponse<List<BlacklistDTO>> getActiveBlacklists() {
        long startTime = System.currentTimeMillis();
        List<BlacklistDTO> blacklists = blacklistService.getActiveBlacklists();
        long responseTime = System.currentTimeMillis() - startTime;
        
        if (!blacklists.isEmpty()) {
            return ApiResponse.success(blacklists, responseTime, "L1", true);
        } else {
            return ApiResponse.success(blacklists, responseTime, "DB", false);
        }
    }

    /**
     * 創建黑名單記錄
     */
    @PostMapping
    public ApiResponse<BlacklistDTO> createBlacklist(@RequestBody Blacklist blacklist) {
        long startTime = System.currentTimeMillis();
        BlacklistDTO createdBlacklist = blacklistService.createBlacklist(blacklist);
        long responseTime = System.currentTimeMillis() - startTime;
        
        return ApiResponse.success(createdBlacklist, responseTime, "DB", false);
    }

    /**
     * 更新黑名單狀態
     */
    @PutMapping("/{id}/status")
    public ApiResponse<BlacklistDTO> updateBlacklistStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        long startTime = System.currentTimeMillis();
        BlacklistDTO updatedBlacklist = blacklistService.updateBlacklistStatus(id, status);
        long responseTime = System.currentTimeMillis() - startTime;
        
        if (updatedBlacklist != null && updatedBlacklist.getUserId() != null) {
            return ApiResponse.success(updatedBlacklist, responseTime, "DB", false);
        } else {
            return ApiResponse.error("黑名單記錄不存在或更新失敗");
        }
    }

    /**
     * 創建測試數據
     */
    @PostMapping("/test-data")
    public ApiResponse<String> createTestData() {
        try {
            // 創建一些測試黑名單記錄
            for (int i = 1; i <= 5; i++) {
                Blacklist blacklist = new Blacklist();
                blacklist.setUserId("USER" + String.format("%03d", i));
                blacklist.setAccountNumber("ACC" + String.format("%03d", i));
                blacklist.setReason("測試黑名單 " + i);
                blacklist.setBlacklistType("USER");
                blacklist.setStatus("ACTIVE");
                blacklist.setExpiresAt(LocalDateTime.now().plusDays(30));
                
                blacklistService.createBlacklist(blacklist);
            }
            
            return ApiResponse.success("測試數據創建成功", 0L, "DB", false);
        } catch (Exception e) {
            log.error("創建測試數據失敗", e);
            return ApiResponse.error("創建測試數據失敗: " + e.getMessage());
        }
    }
} 