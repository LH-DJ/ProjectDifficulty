package com.multilevelcache.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * 黑名單DTO類
 * 
 * @author MultiLevelCache Team
 * @version 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BlacklistDTO {

    private String userId;
    private String accountNumber;
    private String reason;
    private String blacklistType;
    private String blacklistTypeDescription;
    private String status;
    private String statusDescription;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime expiresAt;

    /**
     * 響應時間統計
     */
    private Long responseTimeMs;
    private String cacheLevel;
    private Boolean fromCache;
    private Boolean isBlacklisted;
} 