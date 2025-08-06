# MultiLevelCache - 多級緩存架構

## 📋 項目概述

MultiLevelCache 是一個基於 Spring Boot 的多級緩存架構項目，實現了 **L1 (Caffeine) + L2 (Redis) 二級緩存** 架構。該項目提供了高性能的緩存解決方案，適用於高併發、大數據量的應用場景。

### 🏗️ 架構特點

- **二級緩存架構**: L1 本地緩存 (Caffeine) + L2 分布式緩存 (Redis)
- **智能緩存策略**: 自動在 L1 和 L2 之間同步數據
- **高性能**: 本地緩存提供毫秒級響應，分布式緩存提供數據共享
- **高可用**: 支持緩存降級和故障恢復
- **監控完善**: 提供詳細的緩存統計和健康檢查

### 🎯 應用場景

1. **高併發讀取**: 交易記錄查詢、用戶信息查詢
2. **數據共享**: 多實例部署時的數據一致性
3. **性能優化**: 減少數據庫壓力，提升響應速度
4. **緩存降級**: 當 Redis 不可用時，自動降級到本地緩存

## 🚀 快速開始

### 環境要求

- **Java**: 8 或更高版本
- **Maven**: 3.6 或更高版本
- **MySQL**: 5.7 或更高版本
- **Redis**: 5.0 或更高版本 (可選，用於二級緩存)

### 安裝步驟

1. **克隆項目**
   ```bash
   git clone <repository-url>
   cd MultiLevelCache
   ```

2. **配置數據庫**
   ```sql
   -- 創建數據庫
   CREATE DATABASE multilevel_cache CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   
   -- 執行初始化腳本
   source src/main/resources/sql/init.sql
   ```

3. **配置 Redis** (可選)
   ```bash
   # 使用 Docker 啟動 Redis
   docker run -d -p 6379:6379 redis:latest
   
   # 或使用本地 Redis
   redis-server
   ```

4. **編譯項目**
   ```bash
   mvn clean package
   ```

5. **啟動應用**

   **本地模式** (僅 Caffeine 緩存):
   ```bash
   java -jar target/MultiLevelCache-2.0.0.jar --spring.profiles.active=local
   ```

   **Redis 模式** (二級緩存):
   ```bash
   java -jar target/MultiLevelCache-2.0.0.jar --spring.profiles.active=redis
   ```

   **使用啟動腳本**:
   ```bash
   # Windows
   start-app.bat
   
   # 或直接啟動 Redis 模式
   start-redis.bat
   ```

### 訪問地址

- **應用首頁**: http://localhost:8080
- **健康檢查**: http://localhost:8080/actuator/health
- **緩存監控**: http://localhost:8080/actuator/caches
- **API 文檔**: 見 [API_DOCUMENTATION.md](API_DOCUMENTATION.md)

## 🏗️ 架構設計

### 二級緩存架構

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   客戶端請求     │───▶│   L1 緩存       │───▶│   L2 緩存       │
│                │    │   (Caffeine)    │    │   (Redis)       │
└─────────────────┘    └─────────────────┘    └─────────────────┘
                                │                       │
                                ▼                       ▼
                       ┌─────────────────┐    ┌─────────────────┐
                       │   數據庫        │    │   數據庫        │
                       │   (MySQL)       │    │   (MySQL)       │
                       └─────────────────┘    └─────────────────┘
```

### 緩存策略

1. **讀取策略**:
   - 優先查詢 L1 緩存 (Caffeine)
   - L1 未命中時查詢 L2 緩存 (Redis)
   - L2 命中時將數據同步到 L1
   - 兩級都未命中時查詢數據庫

2. **寫入策略**:
   - 同時寫入 L1 和 L2 緩存
   - 確保數據一致性

3. **失效策略**:
   - 同時從 L1 和 L2 緩存中刪除
   - 支持批量清空操作

### 核心組件

- **MultiLevelCacheService**: 二級緩存服務核心
- **CacheConfig**: L1 緩存配置 (Caffeine)
- **RedisCacheConfig**: L2 緩存配置 (Redis)
- **CacheController**: 緩存管理 API

## 📊 性能特點

### 緩存層級對比

| 特性 | L1 緩存 (Caffeine) | L2 緩存 (Redis) |
|------|-------------------|-----------------|
| 響應時間 | < 1ms | 1-5ms |
| 存儲容量 | 有限 (內存) | 大 (可擴展) |
| 數據共享 | 否 (本地) | 是 (分布式) |
| 持久性 | 否 | 是 |
| 網絡依賴 | 無 | 有 |

### 性能優勢

1. **極速響應**: L1 緩存提供毫秒級響應
2. **數據共享**: L2 緩存支持多實例數據共享
3. **自動降級**: Redis 不可用時自動降級到本地緩存
4. **智能同步**: 自動在兩級緩存間同步數據

## 🔧 配置說明

### 緩存配置

```yaml
# L1 緩存配置 (Caffeine)
cache:
  caffeine:
    transaction:
      maximum-size: 1000        # 最大緩存條目數
      expire-after-write: 30m   # 寫入後過期時間
      expire-after-access: 10m  # 訪問後過期時間
    blacklist:
      maximum-size: 500
      expire-after-write: 60m
      expire-after-access: 20m

# L2 緩存配置 (Redis)
cache:
  redis:
    transaction:
      expire-after-write: 2h    # 寫入後過期時間
      expire-after-access: 1h   # 訪問後過期時間
    blacklist:
      expire-after-write: 4h
      expire-after-access: 2h
```

### Redis 配置

```yaml
spring:
  redis:
    host: localhost
    port: 6379
    password: 
    database: 0
    timeout: 2000ms
    lettuce:
      pool:
        max-active: 8
        max-wait: -1ms
        max-idle: 8
        min-idle: 0
```

## 🧪 測試指南

詳細的測試指南請參考 [TEST_GUIDE.md](TEST_GUIDE.md)

### 快速測試

1. **功能測試**
   ```bash
   # 測試交易查詢
   curl http://localhost:8080/api/transactions/TXN001
   
   # 測試黑名單查詢
   curl http://localhost:8080/api/blacklist/user/USER001
   ```

2. **性能測試**
   ```bash
   # 緩存命中率測試
   curl http://localhost:8080/api/performance/test/transaction/1000
   
   # 綜合性能測試
   curl http://localhost:8080/api/performance/comprehensive
   ```

3. **緩存管理**
   ```bash
   # 查看緩存統計
   curl http://localhost:8080/api/cache/stats
   
   # 清空緩存
   curl -X DELETE http://localhost:8080/api/cache/transactionCache
   ```

## 📈 監控與管理

### 健康檢查

- **緩存健康狀態**: `/api/cache/health`
- **應用健康狀態**: `/actuator/health`
- **緩存統計信息**: `/api/cache/stats`

### 監控指標

- 緩存命中率
- 響應時間
- 緩存大小
- 錯誤率

## 🚀 部署指南

詳細的部署指南請參考 [DEPLOYMENT_GUIDE.md](DEPLOYMENT_GUIDE.md)

### 生產環境部署

1. **Docker 部署**
   ```bash
   # 構建鏡像
   docker build -t multilevelcache .
   
   # 運行容器
   docker run -d -p 8080:8080 --name multilevelcache multilevelcache
   ```

2. **傳統部署**
   ```bash
   # 打包
   mvn clean package -Dmaven.test.skip=true
   
   # 運行
   java -jar target/MultiLevelCache-2.0.0.jar --spring.profiles.active=redis
   ```

## 🔧 故障排除

### 常見問題

1. **Redis 連接失敗**
   - 檢查 Redis 服務是否啟動
   - 驗證連接配置
   - 查看應用日誌

2. **緩存性能問題**
   - 調整緩存大小配置
   - 優化過期時間設置
   - 監控緩存命中率

3. **內存溢出**
   - 減少 Caffeine 緩存大小
   - 調整過期策略
   - 增加 JVM 堆內存

### 日誌分析

```bash
# 查看緩存相關日誌
tail -f logs/application.log | grep "cache"

# 查看性能日誌
tail -f logs/application.log | grep "performance"
```

## 📚 API 文檔

詳細的 API 文檔請參考 [API_DOCUMENTATION.md](API_DOCUMENTATION.md)

### 主要 API 端點

- **交易相關**: `/api/transactions/*`
- **黑名單相關**: `/api/blacklist/*`
- **性能測試**: `/api/performance/*`
- **緩存管理**: `/api/cache/*`
- **監控相關**: `/actuator/*`

## 🤝 貢獻指南

1. Fork 本項目
2. 創建功能分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 開啟 Pull Request

## 📄 許可證

本項目採用 MIT 許可證 - 查看 [LICENSE](LICENSE) 文件了解詳情

## 📞 聯繫方式

- 項目維護者: MultiLevelCache Team
- 郵箱: support@multilevelcache.com
- 項目地址: https://github.com/your-org/MultiLevelCache

---

**⭐ 如果這個項目對您有幫助，請給我們一個星標！** 