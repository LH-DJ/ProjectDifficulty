# 多級緩存架構項目 (MultiLevelCache)

## 項目概述

這是一個基於 Spring Boot 3.2 的多級緩存架構項目，使用 **MySQL + MyBatis Plus + Caffeine + Redis** 技術棧，旨在將歷史交易明細查詢和黑名單過濾的響應時間降低到 200ms 以下。

## 技術架構

### 核心技術棧
- **Spring Boot 3.2.0** - 主框架
- **Java 17** - 運行環境
- **MySQL 8.0** - 主數據庫
- **MyBatis Plus 3.5.4.1** - ORM 框架
- **Caffeine 3.1.8** - L1 本地緩存
- **Redis** - L2 分佈式緩存
- **Lombok** - 代碼簡化工具

### 多級緩存架構
```
用戶請求 → L1 (Caffeine) → L2 (Redis) → MySQL
                ↑              ↑
                └── 回寫機制 ──┘
```

## 功能特性

### 🚀 核心功能
- **多級緩存**: Caffeine (L1) + Redis (L2) 雙層緩存
- **交易管理**: 交易記錄的增刪改查
- **黑名單過濾**: 用戶和賬戶黑名單檢查
- **性能監控**: 響應時間和緩存命中率統計
- **Web 測試頁面**: 直觀的 API 測試界面
- **系統監控**: 實時性能統計和健康檢查
- **異步處理**: 支持異步操作和並發處理

### 📊 性能目標
- **響應時間**: < 200ms
- **緩存命中率**: > 80%
- **並發支持**: 100+ 並發請求

## 快速開始

### 環境要求
- Java 17+
- MySQL 8.0+
- Redis 6.0+
- Maven 3.6+

### 1. 數據庫配置
```sql
-- 創建數據庫
CREATE DATABASE multilevel_cache DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 執行初始化腳本
-- 詳見: src/main/resources/sql/init.sql
```

### 2. 配置文件
修改 `src/main/resources/application.yml`:
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/multilevel_cache?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
    username: root
    password: 123456
  
  data:
    redis:
      host: localhost
      port: 6379
```

### 3. 啟動應用
```bash
# 編譯項目
mvn clean compile

# 運行應用
mvn spring-boot:run
```

### 4. 訪問測試頁面
打開瀏覽器訪問: http://localhost:8080

## Docker 部署

### 使用 Docker Compose
```bash
# 構建並啟動所有服務
docker-compose up -d

# 查看服務狀態
docker-compose ps

# 查看日誌
docker-compose logs -f app

# 停止服務
docker-compose down
```

### 手動 Docker 部署
```bash
# 構建鏡像
docker build -t multilevel-cache:2.0.0 .

# 運行容器
docker run -d \
  --name multilevel-cache-app \
  -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=docker \
  multilevel-cache:2.0.0
```

## API 接口

### 交易記錄 API
```
GET  /api/transactions/{transactionId}     # 查詢交易記錄
GET  /api/transactions/user/{userId}       # 查詢用戶交易
GET  /api/transactions/account/{accountNumber} # 查詢賬戶交易
GET  /api/transactions/user/{userId}/page  # 分頁查詢用戶交易
POST /api/transactions                     # 創建交易記錄
PUT  /api/transactions/{transactionId}/status # 更新交易狀態
```

### 黑名單 API
```
GET  /api/blacklist/check/user/{userId}   # 檢查用戶黑名單
GET  /api/blacklist/check/account/{accountNumber} # 檢查賬戶黑名單
GET  /api/blacklist/user/{userId}         # 查詢用戶黑名單記錄
GET  /api/blacklist/account/{accountNumber} # 查詢賬戶黑名單記錄
GET  /api/blacklist/type/{blacklistType}  # 按類型查詢黑名單
GET  /api/blacklist/active                # 查詢有效黑名單
POST /api/blacklist                       # 創建黑名單記錄
PUT  /api/blacklist/{id}/status           # 更新黑名單狀態
```

### 性能測試 API
```
GET  /api/test/transactions?concurrency=10 # 交易性能測試
GET  /api/test/blacklist?concurrency=10   # 黑名單性能測試
GET  /api/test/comprehensive              # 綜合性能測試
```

### 系統監控 API
```
GET  /api/monitor/performance             # 獲取性能統計
GET  /api/monitor/cache                   # 獲取緩存統計
GET  /api/monitor/health                  # 系統健康檢查
GET  /api/monitor/clear-stats             # 清理統計數據
```

## 項目結構

```
MultiLevelCache/
├── src/
│   ├── main/
│   │   ├── java/com/multilevelcache/
│   │   │   ├── aspect/          # AOP 切面
│   │   │   ├── cache/           # 緩存服務
│   │   │   ├── config/          # 配置類
│   │   │   ├── controller/      # 控制器
│   │   │   ├── dto/            # 數據傳輸對象
│   │   │   ├── entity/         # 實體類
│   │   │   ├── mapper/         # MyBatis Plus Mapper
│   │   │   └── service/        # 業務服務
│   │   └── resources/
│   │       ├── mapper/         # XML 映射文件
│   │       ├── sql/            # SQL 腳本
│   │       ├── static/         # 靜態資源
│   │       └── application.yml # 配置文件
│   └── test/                   # 測試代碼
├── docker-compose.yml          # Docker Compose 配置
├── Dockerfile                  # Docker 鏡像配置
├── pom.xml                     # Maven 配置
└── README.md                   # 項目文檔
```

## 緩存策略

### L1 緩存 (Caffeine)
- **容量**: 1000 條記錄
- **過期時間**: 30 分鐘寫入過期，10 分鐘訪問過期
- **特點**: 本地內存，響應速度極快

### L2 緩存 (Redis)
- **容量**: 無限制
- **過期時間**: 30 分鐘
- **特點**: 分佈式共享，持久化

### 緩存更新策略
- **讀取**: L1 → L2 → MySQL (回寫 L1)
- **寫入**: 更新 MySQL → 清除相關緩存
- **失效**: 主動清除 + 時間過期

## 性能監控

### 響應時間統計
- 平均響應時間
- 最小/最大響應時間
- 95% 分位數響應時間
- 緩存命中率

### 緩存統計
- L1 緩存命中率
- L2 緩存命中率
- 緩存大小和內存使用

### 系統監控
- 內存使用情況
- 線程統計
- 方法調用統計
- 性能警告和錯誤

## 開發指南

### 添加新的實體
1. 創建實體類 (使用 MyBatis Plus 註解)
2. 創建 Mapper 接口
3. 創建 XML 映射文件
4. 創建 Service 類
5. 創建 Controller 類

### 緩存使用
```java
// 從緩存獲取
T cached = cacheService.get(CACHE_NAME, key, T.class);

// 寫入緩存
cacheService.put(CACHE_NAME, key, value);

// 清除緩存
cacheService.evict(CACHE_NAME, key);
```

### 異步處理
```java
@Async("taskExecutor")
public CompletableFuture<Result> asyncMethod() {
    // 異步處理邏輯
    return CompletableFuture.completedFuture(result);
}
```

## 部署說明

### 本地部署
1. **環境準備**
   - 安裝 Java 17
   - 安裝 Maven 3.6+
   - 安裝 MySQL 8.0+
   - 安裝 Redis 6.0+

2. **數據庫初始化**
   ```sql
   CREATE DATABASE multilevel_cache;
   -- 執行 init.sql 腳本
   ```

3. **應用啟動**
   ```bash
   mvn spring-boot:run
   ```

### Docker 部署
1. **使用 Docker Compose**
   ```bash
   docker-compose up -d
   ```

2. **手動部署**
   ```bash
   docker build -t multilevel-cache .
   docker run -p 8080:8080 multilevel-cache
   ```

### 生產環境配置
- 調整緩存大小和過期時間
- 配置 Redis 集群
- 設置數據庫連接池
- 配置日誌級別
- 設置監控和告警

## 故障排除

### 常見問題
1. **MySQL 連接失敗**: 檢查數據庫配置和網絡
2. **Redis 連接失敗**: 檢查 Redis 服務狀態
3. **緩存不生效**: 檢查緩存配置和鍵值設計
4. **性能不達標**: 調整緩存參數和數據庫索引

### 日誌查看
```bash
# 查看應用日誌
tail -f logs/application.log

# 查看緩存統計
curl http://localhost:8080/actuator/caches

# 查看性能統計
curl http://localhost:8080/api/monitor/performance
```

## 版本歷史

### v2.0.0 (2024-01-01)
- ✅ 升級到 Spring Boot 3.2.0
- ✅ 升級到 Java 17
- ✅ 升級 MyBatis Plus 到 3.5.4.1
- ✅ 添加系統監控功能
- ✅ 添加性能監控切面
- ✅ 支持異步處理
- ✅ 添加 Docker 部署支持
- ✅ 優化緩存配置和性能
- ✅ 完善文檔和部署指南

### v1.0.0 (2024-01-01)
- ✅ 完成 MySQL + MyBatis Plus 架構
- ✅ 實現多級緩存 (Caffeine + Redis)
- ✅ 添加 Web 測試頁面
- ✅ 完成交易和黑名單功能
- ✅ 實現性能監控

## 貢獻指南

1. Fork 本項目
2. 創建功能分支
3. 提交變更
4. 發起 Pull Request

## 許可證

MIT License

## 聯繫方式

- 項目地址: [GitHub Repository]
- 問題反饋: [Issues]
- 技術支持: [Support Email]

---

**注意**: 本項目僅用於學習和演示目的，生產環境使用前請進行充分的測試和優化。 