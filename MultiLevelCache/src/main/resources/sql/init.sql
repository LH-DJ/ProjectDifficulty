-- 創建數據庫
CREATE DATABASE IF NOT EXISTS multilevel_cache DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE multilevel_cache;

-- 創建交易記錄表
CREATE TABLE IF NOT EXISTS `transaction` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主鍵ID',
    `transaction_id` VARCHAR(50) NOT NULL COMMENT '交易ID',
    `user_id` VARCHAR(50) NOT NULL COMMENT '用戶ID',
    `account_number` VARCHAR(50) NOT NULL COMMENT '賬戶號碼',
    `transaction_type` VARCHAR(20) NOT NULL COMMENT '交易類型',
    `amount` DECIMAL(19,2) NOT NULL COMMENT '交易金額',
    `currency` VARCHAR(3) NOT NULL COMMENT '貨幣類型',
    `description` VARCHAR(500) COMMENT '交易描述',
    `status` VARCHAR(20) NOT NULL COMMENT '交易狀態',
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '創建時間',
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新時間',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '邏輯刪除標記',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_transaction_id` (`transaction_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_account_number` (`account_number`),
    KEY `idx_created_at` (`created_at`),
    KEY `idx_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='交易記錄表';

-- 創建黑名單表
CREATE TABLE IF NOT EXISTS `blacklist` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主鍵ID',
    `user_id` VARCHAR(50) NOT NULL COMMENT '用戶ID',
    `account_number` VARCHAR(50) NOT NULL COMMENT '賬戶號碼',
    `reason` VARCHAR(500) NOT NULL COMMENT '黑名單原因',
    `blacklist_type` VARCHAR(20) NOT NULL COMMENT '黑名單類型',
    `status` VARCHAR(20) NOT NULL COMMENT '黑名單狀態',
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '創建時間',
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新時間',
    `expires_at` TIMESTAMP NULL COMMENT '過期時間',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '邏輯刪除標記',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_account_number` (`account_number`),
    KEY `idx_blacklist_type` (`blacklist_type`),
    KEY `idx_status` (`status`),
    KEY `idx_expires_at` (`expires_at`),
    KEY `idx_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='黑名單表';

-- 插入測試數據（可選）
-- 交易記錄測試數據
INSERT INTO `transaction` (`transaction_id`, `user_id`, `account_number`, `transaction_type`, `amount`, `currency`, `description`, `status`) VALUES
('TXN000001', 'USER001', 'ACC001', 'DEPOSIT', 1000.00, 'CNY', '工資收入', 'COMPLETED'),
('TXN000002', 'USER002', 'ACC002', 'WITHDRAWAL', 500.00, 'USD', '購物消費', 'COMPLETED'),
('TXN000003', 'USER003', 'ACC003', 'TRANSFER', 2000.00, 'EUR', '轉賬', 'COMPLETED'),
('TXN000004', 'USER004', 'ACC004', 'PAYMENT', 300.00, 'JPY', '投資理財', 'COMPLETED'),
('TXN000005', 'USER005', 'ACC005', 'DEPOSIT', 1500.00, 'CNY', '生活費用', 'COMPLETED');

-- 黑名單測試數據
INSERT INTO `blacklist` (`user_id`, `account_number`, `reason`, `blacklist_type`, `status`, `expires_at`) VALUES
('USER001', 'ACC001', '可疑交易', 'USER', 'ACTIVE', DATE_ADD(NOW(), INTERVAL 30 DAY),
('USER002', 'ACC002', '違規操作', 'ACCOUNT', 'ACTIVE', DATE_ADD(NOW(), INTERVAL 15 DAY),
('USER003', 'ACC003', '風險賬戶', 'USER', 'ACTIVE', DATE_ADD(NOW(), INTERVAL 60 DAY),
('USER004', 'ACC004', '異常行為', 'ACCOUNT', 'ACTIVE', DATE_ADD(NOW(), INTERVAL 45 DAY),
('USER005', 'ACC005', '合規問題', 'USER', 'ACTIVE', DATE_ADD(NOW(), INTERVAL 90 DAY); 