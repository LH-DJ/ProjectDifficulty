package com.multilevelcache.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.multilevelcache.dto.ApiResponse;
import com.multilevelcache.dto.TransactionDTO;
import com.multilevelcache.entity.Transaction;
import com.multilevelcache.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * 交易控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    /**
     * 根據交易ID查詢交易記錄
     */
    @GetMapping("/{transactionId}")
    public ApiResponse<TransactionDTO> getTransactionById(@PathVariable String transactionId) {
        long startTime = System.currentTimeMillis();
        TransactionDTO transaction = transactionService.getTransactionById(transactionId);
        long responseTime = System.currentTimeMillis() - startTime;
        
        if (transaction != null && transaction.getTransactionId() != null) {
            return ApiResponse.success(transaction, responseTime, transaction.getCacheLevel(), transaction.getFromCache());
        } else {
            return ApiResponse.error("交易記錄不存在");
        }
    }

    /**
     * 根據用戶ID查詢交易記錄
     */
    @GetMapping("/user/{userId}")
    public ApiResponse<List<TransactionDTO>> getTransactionsByUserId(@PathVariable String userId) {
        long startTime = System.currentTimeMillis();
        List<TransactionDTO> transactions = transactionService.getTransactionsByUserId(userId);
        long responseTime = System.currentTimeMillis() - startTime;
        
        if (!transactions.isEmpty()) {
            return ApiResponse.success(transactions, responseTime, "L1", true);
        } else {
            return ApiResponse.success(transactions, responseTime, "DB", false);
        }
    }

    /**
     * 根據賬戶號碼查詢交易記錄
     */
    @GetMapping("/account/{accountNumber}")
    public ApiResponse<List<TransactionDTO>> getTransactionsByAccountNumber(@PathVariable String accountNumber) {
        long startTime = System.currentTimeMillis();
        List<TransactionDTO> transactions = transactionService.getTransactionsByAccountNumber(accountNumber);
        long responseTime = System.currentTimeMillis() - startTime;
        
        if (!transactions.isEmpty()) {
            return ApiResponse.success(transactions, responseTime, "L1", true);
        } else {
            return ApiResponse.success(transactions, responseTime, "DB", false);
        }
    }

    /**
     * 分頁查詢用戶交易記錄
     */
    @GetMapping("/user/{userId}/page")
    public ApiResponse<IPage<TransactionDTO>> getTransactionsByPage(
            @PathVariable String userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        long startTime = System.currentTimeMillis();
        IPage<TransactionDTO> transactionPage = transactionService.getTransactionsByPage(userId, page, size);
        long responseTime = System.currentTimeMillis() - startTime;
        
        return ApiResponse.success(transactionPage, responseTime, "DB", false);
    }

    /**
     * 創建交易記錄
     */
    @PostMapping
    public ApiResponse<TransactionDTO> createTransaction(@RequestBody Transaction transaction) {
        long startTime = System.currentTimeMillis();
        TransactionDTO createdTransaction = transactionService.createTransaction(transaction);
        long responseTime = System.currentTimeMillis() - startTime;
        
        return ApiResponse.success(createdTransaction, responseTime, "DB", false);
    }

    /**
     * 更新交易狀態
     */
    @PutMapping("/{transactionId}/status")
    public ApiResponse<TransactionDTO> updateTransactionStatus(
            @PathVariable String transactionId,
            @RequestParam String status) {
        long startTime = System.currentTimeMillis();
        TransactionDTO updatedTransaction = transactionService.updateTransactionStatus(transactionId, status);
        long responseTime = System.currentTimeMillis() - startTime;
        
        if (updatedTransaction != null && updatedTransaction.getTransactionId() != null) {
            return ApiResponse.success(updatedTransaction, responseTime, "DB", false);
        } else {
            return ApiResponse.error("交易記錄不存在或更新失敗");
        }
    }

    /**
     * 健康檢查
     */
    @GetMapping("/health")
    public ApiResponse<String> health() {
        return ApiResponse.success("交易服務運行正常", 0L, "NONE", false);
    }

    /**
     * 創建測試數據
     */
    @PostMapping("/test-data")
    public ApiResponse<String> createTestData() {
        try {
            // 創建一些測試交易記錄
            for (int i = 1; i <= 10; i++) {
                Transaction transaction = new Transaction();
                transaction.setUserId("USER" + String.format("%03d", i));
                transaction.setAccountNumber("ACC" + String.format("%03d", i));
                transaction.setTransactionType("DEPOSIT");
                transaction.setAmount(new BigDecimal("1000.00"));
                transaction.setCurrency("CNY");
                transaction.setDescription("測試交易 " + i);
                transaction.setStatus("COMPLETED");
                
                transactionService.createTransaction(transaction);
            }
            
            return ApiResponse.success("測試數據創建成功", 0L, "DB", false);
        } catch (Exception e) {
            log.error("創建測試數據失敗", e);
            return ApiResponse.error("創建測試數據失敗: " + e.getMessage());
        }
    }
} 