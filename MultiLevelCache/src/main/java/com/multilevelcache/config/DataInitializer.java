package com.multilevelcache.config;

import com.multilevelcache.entity.Blacklist;
import com.multilevelcache.entity.Transaction;
import com.multilevelcache.mapper.BlacklistMapper;
import com.multilevelcache.mapper.TransactionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Random;

/**
 * 數據初始化器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final TransactionMapper transactionMapper;
    private final BlacklistMapper blacklistMapper;

    @Override
    public void run(String... args) throws Exception {
        log.info("開始初始化測試數據...");
        
        // 檢查是否已有數據
        if (transactionMapper.selectCount(null) == 0) {
            createTransactionTestData();
        }
        
        if (blacklistMapper.selectCount(null) == 0) {
            createBlacklistTestData();
        }
        
        log.info("測試數據初始化完成！");
    }

    /**
     * 創建交易測試數據
     */
    private void createTransactionTestData() {
        log.info("創建交易測試數據...");
        
        Random random = new Random();
        String[] userIds = {"USER001", "USER002", "USER003", "USER004", "USER005"};
        String[] accountNumbers = {"ACC001", "ACC002", "ACC003", "ACC004", "ACC005"};
        String[] transactionTypes = {"DEPOSIT", "WITHDRAWAL", "TRANSFER", "PAYMENT"};
        String[] currencies = {"CNY", "USD", "EUR", "JPY"};
        String[] descriptions = {"工資收入", "購物消費", "轉賬", "投資理財", "生活費用"};

        for (int i = 0; i < 100; i++) {
            Transaction transaction = new Transaction();
            transaction.setTransactionId("TXN" + String.format("%06d", i + 1));
            transaction.setUserId(userIds[random.nextInt(userIds.length)]);
            transaction.setAccountNumber(accountNumbers[random.nextInt(accountNumbers.length)]);
            transaction.setTransactionType(transactionTypes[random.nextInt(transactionTypes.length)]);
            transaction.setAmount(BigDecimal.valueOf(random.nextDouble() * 10000));
            transaction.setCurrency(currencies[random.nextInt(currencies.length)]);
            transaction.setDescription(descriptions[random.nextInt(descriptions.length)]);
            transaction.setStatus("COMPLETED");
            transaction.setCreatedAt(LocalDateTime.now().minusDays(random.nextInt(30)));
            transaction.setUpdatedAt(LocalDateTime.now());
            transaction.setDeleted(0);

            transactionMapper.insert(transaction);
        }
        
        log.info("成功創建 {} 條交易測試數據", 100);
    }

    /**
     * 創建黑名單測試數據
     */
    private void createBlacklistTestData() {
        log.info("創建黑名單測試數據...");
        
        // 創建一些黑名單記錄
        String[] userIds = {"USER001", "USER002", "USER003"};
        String[] accountNumbers = {"ACC001", "ACC002", "ACC003"};
        String[] reasons = {"可疑交易", "異常行為", "風險控制"};
        String[] blacklistTypes = {"USER", "ACCOUNT"};

        for (int i = 0; i < 10; i++) {
            Blacklist blacklist = new Blacklist();
            blacklist.setUserId(userIds[i % userIds.length]);
            blacklist.setAccountNumber(accountNumbers[i % accountNumbers.length]);
            blacklist.setReason(reasons[i % reasons.length]);
            blacklist.setBlacklistType(blacklistTypes[i % blacklistTypes.length]);
            blacklist.setStatus("ACTIVE");
            blacklist.setCreatedAt(LocalDateTime.now().minusDays(i));
            blacklist.setUpdatedAt(LocalDateTime.now());
            blacklist.setExpiresAt(LocalDateTime.now().plusDays(30));
            blacklist.setDeleted(0);

            blacklistMapper.insert(blacklist);
        }
        
        log.info("成功創建 {} 條黑名單測試數據", 10);
    }
} 