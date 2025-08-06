package com.multilevelcache.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 交易實體類
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("transaction")
public class Transaction {

    /**
     * 主鍵ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 交易ID
     */
    @TableField("transaction_id")
    private String transactionId;

    /**
     * 用戶ID
     */
    @TableField("user_id")
    private String userId;

    /**
     * 賬戶號碼
     */
    @TableField("account_number")
    private String accountNumber;

    /**
     * 交易類型
     */
    @TableField("transaction_type")
    private String transactionType;

    /**
     * 交易金額
     */
    @TableField("amount")
    private BigDecimal amount;

    /**
     * 貨幣類型
     */
    @TableField("currency")
    private String currency;

    /**
     * 交易描述
     */
    @TableField("description")
    private String description;

    /**
     * 交易狀態
     */
    @TableField("status")
    private String status;

    /**
     * 創建時間
     */
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 更新時間
     */
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    /**
     * 邏輯刪除標記
     */
    @TableLogic
    @TableField("deleted")
    private Integer deleted;
} 