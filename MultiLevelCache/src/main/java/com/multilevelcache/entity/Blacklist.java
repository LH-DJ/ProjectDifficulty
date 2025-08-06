package com.multilevelcache.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 黑名單實體類
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("blacklist")
public class Blacklist {

    /**
     * 主鍵ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

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
     * 黑名單原因
     */
    @TableField("reason")
    private String reason;

    /**
     * 黑名單類型
     */
    @TableField("blacklist_type")
    private String blacklistType;

    /**
     * 黑名單狀態
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
     * 過期時間
     */
    @TableField("expires_at")
    private LocalDateTime expiresAt;

    /**
     * 邏輯刪除標記
     */
    @TableLogic
    @TableField("deleted")
    private Integer deleted;
} 