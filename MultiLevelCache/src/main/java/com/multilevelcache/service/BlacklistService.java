package com.multilevelcache.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.multilevelcache.cache.MultiLevelCacheService;
import com.multilevelcache.dto.BlacklistDTO;
import com.multilevelcache.entity.Blacklist;
import com.multilevelcache.mapper.BlacklistMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 黑名單服務類
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BlacklistService {

    private final BlacklistMapper blacklistMapper;
    private final MultiLevelCacheService cacheService;

    private static final String CACHE_NAME = "blacklistCache";

    /**
     * 檢查用戶黑名單
     */
    public BlacklistDTO checkUserBlacklist(String userId) {
        long startTime = System.currentTimeMillis();
        String cacheKey = "user_blacklist:" + userId;
        
        // 嘗試從緩存獲取
        Blacklist cachedBlacklist = cacheService.get(CACHE_NAME, cacheKey, Blacklist.class);
        if (cachedBlacklist != null) {
            return buildBlacklistDTO(cachedBlacklist, System.currentTimeMillis() - startTime, "L1", true, true);
        }

        // 從數據庫查詢
        Blacklist blacklist = blacklistMapper.isUserBlacklisted(userId);
        if (blacklist != null) {
            // 寫入緩存
            cacheService.put(CACHE_NAME, cacheKey, blacklist);
            return buildBlacklistDTO(blacklist, System.currentTimeMillis() - startTime, "DB", false, true);
        }

        return buildBlacklistDTO(null, System.currentTimeMillis() - startTime, "DB", false, false);
    }

    /**
     * 檢查賬戶黑名單
     */
    public BlacklistDTO checkAccountBlacklist(String accountNumber) {
        long startTime = System.currentTimeMillis();
        String cacheKey = "account_blacklist:" + accountNumber;
        
        // 嘗試從緩存獲取
        Blacklist cachedBlacklist = cacheService.get(CACHE_NAME, cacheKey, Blacklist.class);
        if (cachedBlacklist != null) {
            return buildBlacklistDTO(cachedBlacklist, System.currentTimeMillis() - startTime, "L1", true, true);
        }

        // 從數據庫查詢
        Blacklist blacklist = blacklistMapper.isAccountBlacklisted(accountNumber);
        if (blacklist != null) {
            // 寫入緩存
            cacheService.put(CACHE_NAME, cacheKey, blacklist);
            return buildBlacklistDTO(blacklist, System.currentTimeMillis() - startTime, "DB", false, true);
        }

        return buildBlacklistDTO(null, System.currentTimeMillis() - startTime, "DB", false, false);
    }

    /**
     * 根據用戶ID查詢黑名單記錄
     */
    public List<BlacklistDTO> getBlacklistByUserId(String userId) {
        long startTime = System.currentTimeMillis();
        String cacheKey = "user_blacklists:" + userId;
        
        // 嘗試從緩存獲取
        List<Blacklist> cachedBlacklists = cacheService.get(CACHE_NAME, cacheKey, List.class);
        if (cachedBlacklists != null) {
            return cachedBlacklists.stream()
                    .map(b -> buildBlacklistDTO(b, System.currentTimeMillis() - startTime, "L1", true, true))
                    .collect(Collectors.toList());
        }

        // 從數據庫查詢
        List<Blacklist> blacklists = blacklistMapper.selectByUserId(userId);
        
        if (!blacklists.isEmpty()) {
            // 寫入緩存
            cacheService.put(CACHE_NAME, cacheKey, blacklists);
        }

        return blacklists.stream()
                .map(b -> buildBlacklistDTO(b, System.currentTimeMillis() - startTime, "DB", false, true))
                .collect(Collectors.toList());
    }

    /**
     * 根據賬戶號碼查詢黑名單記錄
     */
    public List<BlacklistDTO> getBlacklistByAccountNumber(String accountNumber) {
        long startTime = System.currentTimeMillis();
        String cacheKey = "account_blacklists:" + accountNumber;
        
        // 嘗試從緩存獲取
        List<Blacklist> cachedBlacklists = cacheService.get(CACHE_NAME, cacheKey, List.class);
        if (cachedBlacklists != null) {
            return cachedBlacklists.stream()
                    .map(b -> buildBlacklistDTO(b, System.currentTimeMillis() - startTime, "L1", true, true))
                    .collect(Collectors.toList());
        }

        // 從數據庫查詢
        List<Blacklist> blacklists = blacklistMapper.selectByAccountNumber(accountNumber);
        
        if (!blacklists.isEmpty()) {
            // 寫入緩存
            cacheService.put(CACHE_NAME, cacheKey, blacklists);
        }

        return blacklists.stream()
                .map(b -> buildBlacklistDTO(b, System.currentTimeMillis() - startTime, "DB", false, true))
                .collect(Collectors.toList());
    }

    /**
     * 創建黑名單記錄
     */
    public BlacklistDTO createBlacklist(Blacklist blacklist) {
        long startTime = System.currentTimeMillis();
        
        // 設置默認值
        blacklist.setStatus("ACTIVE");
        blacklist.setExpiresAt(LocalDateTime.now().plusDays(30));
        
        // 保存到數據庫
        blacklistMapper.insert(blacklist);
        
        // 清除相關緩存
        clearRelatedCaches(blacklist);
        
        return buildBlacklistDTO(blacklist, System.currentTimeMillis() - startTime, "DB", false, true);
    }

    /**
     * 更新黑名單狀態
     */
    public BlacklistDTO updateBlacklistStatus(Long id, String status) {
        long startTime = System.currentTimeMillis();
        
        Blacklist blacklist = blacklistMapper.selectById(id);
        if (blacklist != null) {
            blacklist.setStatus(status);
            blacklistMapper.updateById(blacklist);
            
            // 清除相關緩存
            clearRelatedCaches(blacklist);
            
            return buildBlacklistDTO(blacklist, System.currentTimeMillis() - startTime, "DB", false, true);
        }
        
        return buildBlacklistDTO(null, System.currentTimeMillis() - startTime, "DB", false, false);
    }

    /**
     * 根據黑名單類型查詢
     */
    public List<BlacklistDTO> getBlacklistsByType(String blacklistType) {
        long startTime = System.currentTimeMillis();
        String cacheKey = "blacklist_type:" + blacklistType;
        
        // 嘗試從緩存獲取
        List<Blacklist> cachedBlacklists = cacheService.get(CACHE_NAME, cacheKey, List.class);
        if (cachedBlacklists != null) {
            return cachedBlacklists.stream()
                    .map(b -> buildBlacklistDTO(b, System.currentTimeMillis() - startTime, "L1", true, true))
                    .collect(Collectors.toList());
        }

        // 從數據庫查詢
        List<Blacklist> blacklists = blacklistMapper.selectByBlacklistType(blacklistType);
        
        if (!blacklists.isEmpty()) {
            // 寫入緩存
            cacheService.put(CACHE_NAME, cacheKey, blacklists);
        }

        return blacklists.stream()
                .map(b -> buildBlacklistDTO(b, System.currentTimeMillis() - startTime, "DB", false, true))
                .collect(Collectors.toList());
    }

    /**
     * 查詢有效的黑名單記錄
     */
    public List<BlacklistDTO> getActiveBlacklists() {
        long startTime = System.currentTimeMillis();
        String cacheKey = "active_blacklists";
        
        // 嘗試從緩存獲取
        List<Blacklist> cachedBlacklists = cacheService.get(CACHE_NAME, cacheKey, List.class);
        if (cachedBlacklists != null) {
            return cachedBlacklists.stream()
                    .map(b -> buildBlacklistDTO(b, System.currentTimeMillis() - startTime, "L1", true, true))
                    .collect(Collectors.toList());
        }

        // 從數據庫查詢
        List<Blacklist> blacklists = blacklistMapper.selectActiveBlacklists();
        
        if (!blacklists.isEmpty()) {
            // 寫入緩存
            cacheService.put(CACHE_NAME, cacheKey, blacklists);
        }

        return blacklists.stream()
                .map(b -> buildBlacklistDTO(b, System.currentTimeMillis() - startTime, "DB", false, true))
                .collect(Collectors.toList());
    }

    /**
     * 清除相關緩存
     */
    private void clearRelatedCaches(Blacklist blacklist) {
        cacheService.evict(CACHE_NAME, "user_blacklist:" + blacklist.getUserId());
        cacheService.evict(CACHE_NAME, "account_blacklist:" + blacklist.getAccountNumber());
        cacheService.evict(CACHE_NAME, "user_blacklists:" + blacklist.getUserId());
        cacheService.evict(CACHE_NAME, "blacklist_type:" + blacklist.getBlacklistType());
        cacheService.evict(CACHE_NAME, "active_blacklists");
    }

    /**
     * 構建黑名單DTO
     */
    private BlacklistDTO buildBlacklistDTO(Blacklist blacklist, long responseTime, String cacheLevel, boolean fromCache, boolean isBlacklisted) {
        if (blacklist == null) {
            return BlacklistDTO.builder()
                    .responseTimeMs(responseTime)
                    .cacheLevel(cacheLevel)
                    .fromCache(fromCache)
                    .isBlacklisted(isBlacklisted)
                    .build();
        }

        return BlacklistDTO.builder()
                .userId(blacklist.getUserId())
                .accountNumber(blacklist.getAccountNumber())
                .reason(blacklist.getReason())
                .blacklistType(blacklist.getBlacklistType())
                .blacklistTypeDescription(blacklist.getBlacklistType())
                .status(blacklist.getStatus())
                .statusDescription(blacklist.getStatus())
                .createdAt(blacklist.getCreatedAt())
                .updatedAt(blacklist.getUpdatedAt())
                .expiresAt(blacklist.getExpiresAt())
                .responseTimeMs(responseTime)
                .cacheLevel(cacheLevel)
                .fromCache(fromCache)
                .isBlacklisted(isBlacklisted)
                .build();
    }
} 