package com.multilevelcache.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.multilevelcache.cache.MultiLevelCacheService;
import com.multilevelcache.dto.TransactionDTO;
import com.multilevelcache.entity.Transaction;
import com.multilevelcache.mapper.TransactionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 交易服務類
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionMapper transactionMapper;
    private final MultiLevelCacheService cacheService;

    private static final String CACHE_NAME = "transactionCache";

    /**
     * 根據交易ID查詢交易記錄
     */
    public TransactionDTO getTransactionById(String transactionId) {
        long startTime = System.currentTimeMillis();
        String cacheKey = "transaction:" + transactionId;
        
        // 嘗試從緩存獲取
        Transaction cachedTransaction = cacheService.get(CACHE_NAME, cacheKey, Transaction.class);
        if (cachedTransaction != null) {
            return buildTransactionDTO(cachedTransaction, System.currentTimeMillis() - startTime, "L1", true);
        }

        // 從數據庫查詢
        Transaction transaction = transactionMapper.selectByTransactionId(transactionId);
        if (transaction != null) {
            // 寫入緩存
            cacheService.put(CACHE_NAME, cacheKey, transaction);
            return buildTransactionDTO(transaction, System.currentTimeMillis() - startTime, "DB", false);
        }

        return buildTransactionDTO(null, System.currentTimeMillis() - startTime, "DB", false);
    }

    /**
     * 根據用戶ID查詢交易記錄
     */
    public List<TransactionDTO> getTransactionsByUserId(String userId) {
        long startTime = System.currentTimeMillis();
        String cacheKey = "user_transactions:" + userId;
        
        // 嘗試從緩存獲取
        List<Transaction> cachedTransactions = cacheService.get(CACHE_NAME, cacheKey, List.class);
        if (cachedTransactions != null) {
            return cachedTransactions.stream()
                    .map(t -> buildTransactionDTO(t, System.currentTimeMillis() - startTime, "L1", true))
                    .collect(Collectors.toList());
        }

        // 從數據庫查詢
        List<Transaction> transactions = transactionMapper.selectByUserIdOrderByCreatedAtDesc(userId);
        
        if (!transactions.isEmpty()) {
            // 寫入緩存
            cacheService.put(CACHE_NAME, cacheKey, transactions);
        }

        return transactions.stream()
                .map(t -> buildTransactionDTO(t, System.currentTimeMillis() - startTime, "DB", false))
                .collect(Collectors.toList());
    }

    /**
     * 根據賬戶號碼查詢交易記錄
     */
    public List<TransactionDTO> getTransactionsByAccountNumber(String accountNumber) {
        long startTime = System.currentTimeMillis();
        String cacheKey = "account_transactions:" + accountNumber;
        
        // 嘗試從緩存獲取
        List<Transaction> cachedTransactions = cacheService.get(CACHE_NAME, cacheKey, List.class);
        if (cachedTransactions != null) {
            return cachedTransactions.stream()
                    .map(t -> buildTransactionDTO(t, System.currentTimeMillis() - startTime, "L1", true))
                    .collect(Collectors.toList());
        }

        // 從數據庫查詢
        List<Transaction> transactions = transactionMapper.selectByAccountNumber(accountNumber);
        
        if (!transactions.isEmpty()) {
            // 寫入緩存
            cacheService.put(CACHE_NAME, cacheKey, transactions);
        }

        return transactions.stream()
                .map(t -> buildTransactionDTO(t, System.currentTimeMillis() - startTime, "DB", false))
                .collect(Collectors.toList());
    }

    /**
     * 創建交易記錄
     */
    public TransactionDTO createTransaction(Transaction transaction) {
        long startTime = System.currentTimeMillis();
        
        // 生成交易ID
        transaction.setTransactionId("TXN" + UUID.randomUUID().toString().replace("-", "").substring(0, 16));
        transaction.setStatus("PENDING");
        
        // 保存到數據庫
        transactionMapper.insert(transaction);
        
        // 清除相關緩存
        clearRelatedCaches(transaction);
        
        return buildTransactionDTO(transaction, System.currentTimeMillis() - startTime, "DB", false);
    }

    /**
     * 更新交易狀態
     */
    public TransactionDTO updateTransactionStatus(String transactionId, String status) {
        long startTime = System.currentTimeMillis();
        
        QueryWrapper<Transaction> wrapper = new QueryWrapper<>();
        wrapper.eq("transaction_id", transactionId);
        
        Transaction transaction = transactionMapper.selectOne(wrapper);
        if (transaction != null) {
            transaction.setStatus(status);
            transactionMapper.updateById(transaction);
            
            // 清除相關緩存
            clearRelatedCaches(transaction);
            
            return buildTransactionDTO(transaction, System.currentTimeMillis() - startTime, "DB", false);
        }
        
        return buildTransactionDTO(null, System.currentTimeMillis() - startTime, "DB", false);
    }

    /**
     * 分頁查詢交易記錄
     */
    public IPage<TransactionDTO> getTransactionsByPage(String userId, int page, int size) {
        long startTime = System.currentTimeMillis();
        
        Page<Transaction> pageParam = new Page<>(page, size);
        IPage<Transaction> transactionPage = transactionMapper.selectPage(pageParam, 
                new QueryWrapper<Transaction>().eq("user_id", userId));
        
        IPage<TransactionDTO> result = transactionPage.convert(transaction -> 
                buildTransactionDTO(transaction, System.currentTimeMillis() - startTime, "DB", false));
        
        return result;
    }

    /**
     * 清除相關緩存
     */
    private void clearRelatedCaches(Transaction transaction) {
        cacheService.evict(CACHE_NAME, "transaction:" + transaction.getTransactionId());
        cacheService.evict(CACHE_NAME, "user_transactions:" + transaction.getUserId());
        cacheService.evict(CACHE_NAME, "account_transactions:" + transaction.getAccountNumber());
    }

    /**
     * 構建交易DTO
     */
    private TransactionDTO buildTransactionDTO(Transaction transaction, long responseTime, String cacheLevel, boolean fromCache) {
        if (transaction == null) {
            return TransactionDTO.builder()
                    .responseTimeMs(responseTime)
                    .cacheLevel(cacheLevel)
                    .fromCache(fromCache)
                    .build();
        }

        return TransactionDTO.builder()
                .transactionId(transaction.getTransactionId())
                .userId(transaction.getUserId())
                .accountNumber(transaction.getAccountNumber())
                .transactionType(transaction.getTransactionType())
                .transactionTypeDescription(transaction.getTransactionType())
                .amount(transaction.getAmount())
                .currency(transaction.getCurrency())
                .description(transaction.getDescription())
                .status(transaction.getStatus())
                .statusDescription(transaction.getStatus())
                .createdAt(transaction.getCreatedAt())
                .updatedAt(transaction.getUpdatedAt())
                .responseTimeMs(responseTime)
                .cacheLevel(cacheLevel)
                .fromCache(fromCache)
                .build();
    }
} 