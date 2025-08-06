# MultiLevelCache API 文檔

## 📋 API 概述

MultiLevelCache 提供了一套完整的 RESTful API，支持交易記錄查詢、黑名單檢查、性能測試和系統監控等功能。

## 🔗 基礎信息

- **基礎URL**: `http://localhost:8080`
- **內容類型**: `application/json`
- **字符編碼**: `UTF-8`

## 📊 通用響應格式

所有API響應都遵循統一的格式：

```json
{
  "success": true,
  "message": "操作成功",
  "data": {...},
  "responseTimeMs": 15,
  "cacheLevel": "L1",
  "fromCache": true,
  "timestamp": "2024-01-01T12:00:00"
}
```

### 響應字段說明

| 字段 | 類型 | 說明 |
|------|------|------|
| success | Boolean | 操作是否成功 |
| message | String | 響應消息 |
| data | Object | 響應數據 |
| responseTimeMs | Long | 響應時間（毫秒） |
| cacheLevel | String | 數據來源級別（L1/DB） |
| fromCache | Boolean | 是否來自緩存 |
| timestamp | String | 響應時間戳 |

## 🔍 交易記錄 API

### 1. 根據交易ID查詢交易記錄

**端點**: `GET /api/transactions/{transactionId}`

**路徑參數**:
- `transactionId` (String): 交易ID

**請求示例**:
```bash
curl -X GET "http://localhost:8080/api/transactions/TXN000001"
```

**響應示例**:
```json
{
  "success": true,
  "message": "查詢成功",
  "data": {
    "transactionId": "TXN000001",
    "userId": "USER001",
    "accountNumber": "ACC001",
    "transactionType": "DEPOSIT",
    "amount": 1000.00,
    "currency": "CNY",
    "description": "工資收入",
    "status": "COMPLETED",
    "createdAt": "2024-01-01T10:00:00"
  },
  "responseTimeMs": 5,
  "cacheLevel": "L1",
  "fromCache": true,
  "timestamp": "2024-01-01T12:00:00"
}
```

### 2. 根據用戶ID查詢交易記錄

**端點**: `GET /api/transactions/user/{userId}`

**路徑參數**:
- `userId` (String): 用戶ID

**查詢參數**:
- `page` (Integer, 可選): 頁碼，默認1
- `size` (Integer, 可選): 頁面大小，默認10

**請求示例**:
```bash
curl -X GET "http://localhost:8080/api/transactions/user/USER001?page=1&size=10"
```

**響應示例**:
```json
{
  "success": true,
  "message": "查詢成功",
  "data": {
    "records": [
      {
        "transactionId": "TXN000001",
        "userId": "USER001",
        "accountNumber": "ACC001",
        "transactionType": "DEPOSIT",
        "amount": 1000.00,
        "currency": "CNY",
        "description": "工資收入",
        "status": "COMPLETED",
        "createdAt": "2024-01-01T10:00:00"
      }
    ],
    "total": 1,
    "page": 1,
    "size": 10
  },
  "responseTimeMs": 25,
  "cacheLevel": "DB",
  "fromCache": false,
  "timestamp": "2024-01-01T12:00:00"
}
```

## 🚫 黑名單 API

### 1. 檢查用戶黑名單

**端點**: `GET /api/blacklist/user/{userId}`

**路徑參數**:
- `userId` (String): 用戶ID

**請求示例**:
```bash
curl -X GET "http://localhost:8080/api/blacklist/user/USER001"
```

**響應示例**:
```json
{
  "success": true,
  "message": "查詢成功",
  "data": {
    "userId": "USER001",
    "accountNumber": "ACC001",
    "reason": "可疑交易",
    "blacklistType": "USER",
    "status": "ACTIVE",
    "createdAt": "2024-01-01T10:00:00",
    "expiresAt": "2024-02-01T10:00:00",
    "isBlacklisted": true
  },
  "responseTimeMs": 3,
  "cacheLevel": "L1",
  "fromCache": true,
  "timestamp": "2024-01-01T12:00:00"
}
```

### 2. 檢查賬戶黑名單

**端點**: `GET /api/blacklist/account/{accountNumber}`

**路徑參數**:
- `accountNumber` (String): 賬戶號碼

**請求示例**:
```bash
curl -X GET "http://localhost:8080/api/blacklist/account/ACC001"
```

**響應示例**:
```json
{
  "success": true,
  "message": "查詢成功",
  "data": {
    "userId": "USER001",
    "accountNumber": "ACC001",
    "reason": "可疑交易",
    "blacklistType": "ACCOUNT",
    "status": "ACTIVE",
    "createdAt": "2024-01-01T10:00:00",
    "expiresAt": "2024-02-01T10:00:00",
    "isBlacklisted": true
  },
  "responseTimeMs": 2,
  "cacheLevel": "L1",
  "fromCache": true,
  "timestamp": "2024-01-01T12:00:00"
}
```

## ⚡ 性能測試 API

### 1. 交易記錄查詢性能測試

**端點**: `GET /api/performance/test/transactions`

**查詢參數**:
- `requestCount` (Integer, 可選): 請求數量，默認100
- `concurrentThreads` (Integer, 可選): 並發線程數，默認10

**請求示例**:
```bash
curl -X GET "http://localhost:8080/api/performance/test/transactions?requestCount=100&concurrentThreads=10"
```

**響應示例**:
```json
{
  "success": true,
  "message": "性能測試完成",
  "data": {
    "testType": "交易記錄查詢",
    "requestCount": 100,
    "concurrentThreads": 10,
    "averageResponseTime": 15.5,
    "minResponseTime": 2,
    "maxResponseTime": 45,
    "medianResponseTime": 12,
    "percentile95ResponseTime": 28,
    "targetAchieved": true
  },
  "responseTimeMs": 1500,
  "cacheLevel": "TEST",
  "fromCache": false,
  "timestamp": "2024-01-01T12:00:00"
}
```

### 2. 黑名單檢查性能測試

**端點**: `GET /api/performance/test/blacklist`

**查詢參數**:
- `requestCount` (Integer, 可選): 請求數量，默認100
- `concurrentThreads` (Integer, 可選): 並發線程數，默認10

**請求示例**:
```bash
curl -X GET "http://localhost:8080/api/performance/test/blacklist?requestCount=100&concurrentThreads=10"
```

### 3. 綜合性能測試

**端點**: `GET /api/performance/test/comprehensive`

**查詢參數**:
- `requestCount` (Integer, 可選): 請求數量，默認50
- `concurrentThreads` (Integer, 可選): 並發線程數，默認10

**請求示例**:
```bash
curl -X GET "http://localhost:8080/api/performance/test/comprehensive?requestCount=50&concurrentThreads=10"
```

**響應示例**:
```json
{
  "success": true,
  "message": "綜合性能測試完成",
  "data": {
    "requestCount": 50,
    "concurrentThreads": 10,
    "transactionTest": {
      "testType": "交易記錄查詢",
      "averageResponseTime": 12.3,
      "targetAchieved": true
    },
    "blacklistTest": {
      "testType": "黑名單檢查",
      "averageResponseTime": 8.7,
      "targetAchieved": true
    },
    "overallAverageResponseTime": 10.5,
    "targetAchieved": true
  },
  "responseTimeMs": 2500,
  "cacheLevel": "TEST",
  "fromCache": false,
  "timestamp": "2024-01-01T12:00:00"
}
```

## 📊 監控 API

### 1. 系統健康檢查

**端點**: `GET /actuator/health`

**請求示例**:
```bash
curl -X GET "http://localhost:8080/actuator/health"
```

**響應示例**:
```json
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP",
      "details": {
        "database": "MySQL",
        "validationQuery": "SELECT 1"
      }
    },
    "diskSpace": {
      "status": "UP",
      "details": {
        "total": 107374182400,
        "free": 53687091200,
        "threshold": 10485760
      }
    }
  }
}
```

### 2. 緩存統計信息

**端點**: `GET /actuator/caches`

**請求示例**:
```bash
curl -X GET "http://localhost:8080/actuator/caches"
```

**響應示例**:
```json
{
  "transactionCache": {
    "size": 150,
    "maxSize": 1000,
    "hitRate": 0.85,
    "evictionCount": 25
  },
  "blacklistCache": {
    "size": 80,
    "maxSize": 500,
    "hitRate": 0.92,
    "evictionCount": 12
  }
}
```

### 3. 應用信息

**端點**: `GET /actuator/info`

**請求示例**:
```bash
curl -X GET "http://localhost:8080/actuator/info"
```

**響應示例**:
```json
{
  "app": {
    "name": "MultiLevelCache",
    "version": "2.0.0",
    "description": "多級緩存系統"
  },
  "build": {
    "artifact": "MultiLevelCache",
    "version": "2.0.0",
    "time": "2024-01-01T10:00:00Z"
  }
}
```

## 🔧 緩存管理 API

### 1. 清空指定緩存

**端點**: `DELETE /api/cache/{cacheName}`

**路徑參數**:
- `cacheName` (String): 緩存名稱（transactionCache/blacklistCache）

**請求示例**:
```bash
curl -X DELETE "http://localhost:8080/api/cache/transactionCache"
```

**響應示例**:
```json
{
  "success": true,
  "message": "緩存清空成功",
  "data": null,
  "responseTimeMs": 5,
  "cacheLevel": "MANAGEMENT",
  "fromCache": false,
  "timestamp": "2024-01-01T12:00:00"
}
```

### 2. 刪除指定緩存項

**端點**: `DELETE /api/cache/{cacheName}/{key}`

**路徑參數**:
- `cacheName` (String): 緩存名稱
- `key` (String): 緩存鍵

**請求示例**:
```bash
curl -X DELETE "http://localhost:8080/api/cache/transactionCache/transaction:TXN000001"
```

## 🚨 錯誤處理

### 錯誤響應格式

```json
{
  "success": false,
  "message": "錯誤描述",
  "data": null,
  "responseTimeMs": 10,
  "cacheLevel": "ERROR",
  "fromCache": false,
  "timestamp": "2024-01-01T12:00:00"
}
```

### 常見錯誤碼

| HTTP狀態碼 | 錯誤類型 | 說明 |
|------------|----------|------|
| 400 | Bad Request | 請求參數錯誤 |
| 404 | Not Found | 資源不存在 |
| 500 | Internal Server Error | 服務器內部錯誤 |
| 503 | Service Unavailable | 服務不可用 |

### 錯誤示例

**404 - 交易記錄不存在**:
```json
{
  "success": false,
  "message": "交易記錄不存在: TXN999999",
  "data": null,
  "responseTimeMs": 25,
  "cacheLevel": "DB",
  "fromCache": false,
  "timestamp": "2024-01-01T12:00:00"
}
```

**500 - 數據庫連接錯誤**:
```json
{
  "success": false,
  "message": "數據庫連接失敗",
  "data": null,
  "responseTimeMs": 5000,
  "cacheLevel": "ERROR",
  "fromCache": false,
  "timestamp": "2024-01-01T12:00:00"
}
```

## 📝 API 使用示例

### 1. 完整的交易記錄查詢流程

```bash
#!/bin/bash

# 1. 健康檢查
echo "檢查系統健康狀態..."
curl -s "http://localhost:8080/actuator/health" | jq '.'

# 2. 查詢交易記錄（首次查詢）
echo "首次查詢交易記錄..."
curl -s "http://localhost:8080/api/transactions/TXN000001" | jq '.'

# 3. 重複查詢（驗證緩存）
echo "重複查詢交易記錄..."
curl -s "http://localhost:8080/api/transactions/TXN000001" | jq '.'

# 4. 性能測試
echo "執行性能測試..."
curl -s "http://localhost:8080/api/performance/test/transactions?requestCount=100&concurrentThreads=10" | jq '.'
```

### 2. 黑名單檢查流程

```bash
#!/bin/bash

# 1. 檢查用戶黑名單
echo "檢查用戶黑名單..."
curl -s "http://localhost:8080/api/blacklist/user/USER001" | jq '.'

# 2. 檢查賬戶黑名單
echo "檢查賬戶黑名單..."
curl -s "http://localhost:8080/api/blacklist/account/ACC001" | jq '.'

# 3. 性能測試
echo "執行黑名單性能測試..."
curl -s "http://localhost:8080/api/performance/test/blacklist?requestCount=100&concurrentThreads=10" | jq '.'
```

## 🔐 安全考慮

### 1. 輸入驗證

- 所有路徑參數和查詢參數都會進行驗證
- 防止SQL注入和XSS攻擊
- 限制請求大小和頻率

### 2. 錯誤處理

- 不暴露敏感信息
- 統一的錯誤響應格式
- 詳細的日誌記錄

### 3. 性能保護

- 請求頻率限制
- 緩存容量限制
- 超時處理

## 📈 性能優化建議

### 1. 客戶端優化

- 使用連接池
- 設置合理的超時時間
- 實現重試機制

### 2. 緩存策略

- 合理設置緩存TTL
- 監控緩存命中率
- 定期清理過期緩存

### 3. 監控告警

- 設置響應時間告警
- 監控錯誤率
- 關注系統資源使用

---

**注意**: 本API文檔會根據系統版本更新，請確保使用最新版本。 