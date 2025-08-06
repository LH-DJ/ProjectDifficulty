package com.multilevelcache.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 交易記錄DTO類
 * 
 * @author MultiLevelCache Team
 * @version 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionDTO {

    private String transactionId;
    private String userId;
    private String accountNumber;
    private String transactionType;
    private String transactionTypeDescription;
    private BigDecimal amount;
    private String currency;
    private String description;
    private String status;
    private String statusDescription;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * 響應時間統計
     */
    private Long responseTimeMs;
    private String cacheLevel;
    private Boolean fromCache;
} 