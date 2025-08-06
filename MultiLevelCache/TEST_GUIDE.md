# MultiLevelCache 測試指南

## 📋 測試概述

本指南詳細介紹 MultiLevelCache 項目的測試流程、測試場景和測試方法，幫助開發者和測試人員全面驗證系統功能。

## 🧪 測試環境準備

### 1. 環境要求

- **JDK**: 8+
- **MySQL**: 8.0+
- **Maven**: 3.6+
- **瀏覽器**: Chrome/Firefox/Edge
- **API測試工具**: Postman/curl

### 2. 數據庫初始化

```sql
-- 執行初始化腳本
source src/main/resources/sql/init.sql

-- 驗證數據
SELECT COUNT(*) FROM transaction;
SELECT COUNT(*) FROM blacklist;
```

### 3. 應用啟動

```bash
# 編譯項目
mvn clean package

# 啟動應用
java -jar target/MultiLevelCache-2.0.0.jar

# 驗證啟動
curl http://localhost:8080/actuator/health
```

## 🔍 功能測試

### 1. 交易記錄查詢測試

#### 1.1 單個交易記錄查詢

**測試目標**: 驗證交易記錄查詢功能的正確性

**測試步驟**:
```bash
# 測試緩存未命中情況
curl -X GET "http://localhost:8080/api/transactions/TXN000001"

# 測試緩存命中情況（重複查詢）
curl -X GET "http://localhost:8080/api/transactions/TXN000001"

# 測試不存在的交易記錄
curl -X GET "http://localhost:8080/api/transactions/TXN999999"
```

**預期結果**:
- 首次查詢：`fromCache: false`, `cacheLevel: "DB"`
- 重複查詢：`fromCache: true`, `cacheLevel: "L1"`
- 響應時間：緩存命中時應明顯快於數據庫查詢

#### 1.2 批量交易記錄查詢

**測試目標**: 驗證批量查詢的性能和正確性

**測試步驟**:
```bash
# 批量查詢測試
for i in {1..10}; do
  curl -X GET "http://localhost:8080/api/transactions/TXN00000$i" &
done
wait
```

### 2. 黑名單檢查測試

#### 2.1 用戶黑名單檢查

**測試目標**: 驗證用戶黑名單檢查功能

**測試步驟**:
```bash
# 檢查存在的用戶黑名單
curl -X GET "http://localhost:8080/api/blacklist/user/USER001"

# 檢查不存在的用戶黑名單
curl -X GET "http://localhost:8080/api/blacklist/user/USER999"

# 重複查詢驗證緩存
curl -X GET "http://localhost:8080/api/blacklist/user/USER001"
```

#### 2.2 賬戶黑名單檢查

**測試目標**: 驗證賬戶黑名單檢查功能

**測試步驟**:
```bash
# 檢查存在的賬戶黑名單
curl -X GET "http://localhost:8080/api/blacklist/account/ACC001"

# 檢查不存在的賬戶黑名單
curl -X GET "http://localhost:8080/api/blacklist/account/ACC999"
```

## ⚡ 性能測試

### 1. 單項性能測試

#### 1.1 交易記錄查詢性能測試

**測試目標**: 評估交易記錄查詢的性能表現

**測試命令**:
```bash
# 基礎性能測試
curl -X GET "http://localhost:8080/api/performance/test/transactions?requestCount=100&concurrentThreads=10"

# 高負載測試
curl -X GET "http://localhost:8080/api/performance/test/transactions?requestCount=1000&concurrentThreads=50"
```

**性能指標**:
- 平均響應時間 < 50ms
- 95%分位數響應時間 < 100ms
- 緩存命中率 > 80%（重複查詢）

#### 1.2 黑名單檢查性能測試

**測試目標**: 評估黑名單檢查的性能表現

**測試命令**:
```bash
# 基礎性能測試
curl -X GET "http://localhost:8080/api/performance/test/blacklist?requestCount=100&concurrentThreads=10"

# 高負載測試
curl -X GET "http://localhost:8080/api/performance/test/blacklist?requestCount=1000&concurrentThreads=50"
```

### 2. 綜合性能測試

**測試目標**: 評估系統整體性能表現

**測試命令**:
```bash
# 綜合性能測試
curl -X GET "http://localhost:8080/api/performance/test/comprehensive?requestCount=50&concurrentThreads=10"
```

**測試內容**:
- 交易記錄查詢性能
- 黑名單檢查性能
- 整體響應時間統計
- 緩存命中率分析

## 🔄 緩存測試

### 1. 緩存命中率測試

#### 1.1 首次查詢測試

**測試目標**: 驗證緩存未命中時的數據庫查詢

**測試步驟**:
1. 清空緩存（重啟應用或等待過期）
2. 執行查詢請求
3. 檢查響應中的 `fromCache` 字段
4. 記錄響應時間

**預期結果**:
```json
{
  "success": true,
  "data": {...},
  "fromCache": false,
  "cacheLevel": "DB",
  "responseTimeMs": 15
}
```

#### 1.2 重複查詢測試

**測試目標**: 驗證緩存命中時的快速響應

**測試步驟**:
1. 在首次查詢後立即重複查詢
2. 檢查響應中的 `fromCache` 字段
3. 比較響應時間

**預期結果**:
```json
{
  "success": true,
  "data": {...},
  "fromCache": true,
  "cacheLevel": "L1",
  "responseTimeMs": 2
}
```

### 2. 緩存失效測試

#### 2.1 TTL過期測試

**測試目標**: 驗證緩存過期機制

**測試步驟**:
1. 執行查詢並確認緩存命中
2. 等待緩存過期（根據配置的TTL時間）
3. 重新查詢並檢查是否從數據庫獲取

**配置參考**:
```yaml
cache:
  caffeine:
    transaction:
      expire-after-write: 30m  # 30分鐘後過期
    blacklist:
      expire-after-write: 60m  # 60分鐘後過期
```

#### 2.2 容量限制測試

**測試目標**: 驗證緩存容量限制機制

**測試步驟**:
1. 大量查詢不同Key的數據
2. 監控緩存統計信息
3. 驗證LRU淘汰機制

## 📊 監控測試

### 1. 健康檢查測試

**測試目標**: 驗證系統健康狀態

**測試命令**:
```bash
# 健康檢查
curl -X GET "http://localhost:8080/actuator/health"

# 詳細健康信息
curl -X GET "http://localhost:8080/actuator/health" -H "Accept: application/json"
```

**預期結果**:
```json
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP"
    },
    "diskSpace": {
      "status": "UP"
    }
  }
}
```

### 2. 緩存統計測試

**測試目標**: 監控緩存使用情況

**測試命令**:
```bash
# 緩存統計信息
curl -X GET "http://localhost:8080/actuator/caches"
```

## 🐛 異常測試

### 1. 數據庫連接異常

**測試目標**: 驗證數據庫異常時的系統行為

**測試步驟**:
1. 停止MySQL服務
2. 執行查詢請求
3. 檢查錯誤處理和響應

### 2. 緩存異常

**測試目標**: 驗證緩存異常時的降級處理

**測試步驟**:
1. 模擬緩存服務異常
2. 執行查詢請求
3. 驗證是否正確降級到數據庫查詢

### 3. 高並發測試

**測試目標**: 驗證高並發場景下的系統穩定性

**測試命令**:
```bash
# 使用Apache Bench進行壓力測試
ab -n 1000 -c 100 http://localhost:8080/api/transactions/TXN000001
```

## 📈 性能基準測試

### 1. 基準測試場景

#### 場景1: 低負載測試
- 請求數量: 100
- 並發線程: 5
- 預期平均響應時間: < 20ms

#### 場景2: 中等負載測試
- 請求數量: 1000
- 並發線程: 20
- 預期平均響應時間: < 50ms

#### 場景3: 高負載測試
- 請求數量: 10000
- 並發線程: 50
- 預期平均響應時間: < 100ms

### 2. 性能指標

| 指標 | 目標值 | 測量方法 |
|------|--------|----------|
| 平均響應時間 | < 50ms | API響應時間統計 |
| 95%分位數響應時間 | < 100ms | 性能測試結果 |
| 緩存命中率 | > 80% | 重複查詢統計 |
| 系統可用性 | > 99.9% | 健康檢查監控 |
| 內存使用率 | < 80% | JVM監控 |

## 🔧 測試工具

### 1. 命令行工具

```bash
# curl測試
curl -X GET "http://localhost:8080/api/transactions/TXN000001"

# Apache Bench壓力測試
ab -n 1000 -c 10 http://localhost:8080/api/transactions/TXN000001

# 健康檢查
curl -X GET "http://localhost:8080/actuator/health"
```

### 2. 監控工具

```bash
# JVM監控
jstat -gc <pid>

# 系統監控
top -p <pid>

# 網絡監控
netstat -an | grep 8080
```

## 📝 測試報告模板

### 測試報告結構

1. **測試概述**
   - 測試目標
   - 測試環境
   - 測試範圍

2. **功能測試結果**
   - 交易記錄查詢測試
   - 黑名單檢查測試
   - 異常處理測試

3. **性能測試結果**
   - 響應時間統計
   - 緩存命中率
   - 並發性能

4. **問題記錄**
   - 發現的問題
   - 問題嚴重程度
   - 修復建議

5. **測試結論**
   - 功能完整性評估
   - 性能達標情況
   - 系統穩定性評估

## 🚀 持續測試

### 1. 自動化測試腳本

```bash
#!/bin/bash
# 自動化測試腳本

echo "開始執行自動化測試..."

# 健康檢查
echo "1. 執行健康檢查..."
curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/actuator/health

# 功能測試
echo "2. 執行功能測試..."
curl -X GET "http://localhost:8080/api/transactions/TXN000001"

# 性能測試
echo "3. 執行性能測試..."
curl -X GET "http://localhost:8080/api/performance/test/transactions?requestCount=100&concurrentThreads=10"

echo "測試完成！"
```

### 2. 監控告警

- 響應時間超過閾值告警
- 緩存命中率低於閾值告警
- 系統錯誤率告警
- 內存使用率告警

---

**注意**: 本測試指南應根據實際項目需求進行調整和擴展。 