package com.multilevelcache.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.multilevelcache.entity.Transaction;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 交易記錄 Mapper 接口
 */
public interface TransactionMapper extends BaseMapper<Transaction> {

    /**
     * 根據交易ID查詢交易記錄
     */
    Transaction selectByTransactionId(@Param("transactionId") String transactionId);

    /**
     * 根據用戶ID查詢交易記錄（按創建時間倒序）
     */
    List<Transaction> selectByUserIdOrderByCreatedAtDesc(@Param("userId") String userId);

    /**
     * 根據用戶ID和時間範圍查詢交易記錄
     */
    List<Transaction> selectByUserIdAndDateRange(
            @Param("userId") String userId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    /**
     * 根據賬戶號碼查詢交易記錄
     */
    List<Transaction> selectByAccountNumber(@Param("accountNumber") String accountNumber);

    /**
     * 根據賬戶號碼和時間範圍查詢交易記錄
     */
    List<Transaction> selectByAccountNumberAndDateRange(
            @Param("accountNumber") String accountNumber,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    /**
     * 分頁查詢交易記錄
     */
    IPage<Transaction> selectPage(Page<Transaction> page, @Param("userId") String userId);

    /**
     * 統計用戶交易總數
     */
    Long countByUserId(@Param("userId") String userId);

    /**
     * 統計賬戶交易總數
     */
    Long countByAccountNumber(@Param("accountNumber") String accountNumber);
} 