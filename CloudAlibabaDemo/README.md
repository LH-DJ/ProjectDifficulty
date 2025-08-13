# 阿里雲微服務架構演示項目

## 項目概述

這是一個基於Spring Boot 3、Spring Cloud 2023和Spring Cloud Alibaba 2022的微服務架構演示項目，展示了現代微服務架構的最佳實踐。

## 技術棧

- **Java版本**: JDK 17
- **Spring Boot**: 3.2.0
- **Spring Cloud**: 2023.0.0
- **Spring Cloud Alibaba**: 2022.0.0.0-RC2
- **數據庫**: MySQL 8.0
- **服務註冊與配置**: Nacos 2.2.3
- **流量控制**: Sentinel 1.8.6
- **分佈式事務**: Seata 1.7.0
- **緩存**: Redis 7
- **API網關**: Spring Cloud Gateway
- **構建工具**: Maven 3.8+

## 項目結構

```
cloud-alibaba-demo/
├── cloud-common/           # 通用模組
├── cloud-user-service/     # 用戶服務
├── cloud-product-service/  # 商品服務
├── cloud-order-service/    # 訂單服務
├── cloud-gateway/          # API網關
├── sql/                    # 數據庫腳本
├── docker-compose.yml      # Docker編排文件
└── README.md              # 項目文檔
```

## 快速開始

### 環境要求

- JDK 17+
- Maven 3.8+
- Docker & Docker Compose
- MySQL 8.0+
- 至少4GB可用內存

### 1. 啟動基礎服務

```bash
# 克隆項目
git clone <repository-url>
cd cloud-alibaba-demo

# 啟動基礎服務（MySQL、Nacos、Sentinel、Seata、Redis）
docker-compose up -d

# 檢查服務狀態
docker-compose ps
```

### 2. 初始化數據庫

```bash
# 等待MySQL啟動完成後，執行初始化腳本
# 腳本會自動創建數據庫和表結構，並插入測試數據
```

### 3. 編譯項目

```bash
# 編譯整個項目
mvn clean compile

# 或者編譯特定模組
mvn clean compile -pl cloud-common
mvn clean compile -pl cloud-user-service
mvn clean compile -pl cloud-product-service
```

### 4. 啟動微服務

```bash
# 啟動用戶服務
mvn spring-boot:run -pl cloud-user-service

# 啟動商品服務
mvn spring-boot:run -pl cloud-product-service

# 啟動訂單服務
mvn spring-boot:run -pl cloud-order-service

# 啟動API網關
mvn spring-boot:run -pl cloud-gateway
```

## 服務端口配置

| 服務 | 端口 | 說明 |
|------|------|------|
| 用戶服務 | 8081 | 用戶管理相關API |
| 商品服務 | 8082 | 商品管理相關API |
| 訂單服務 | 8083 | 訂單管理相關API |
| API網關 | 8080 | 統一入口 |
| Nacos | 8848 | 服務註冊與配置中心 |
| Sentinel | 8080 | 流量控制控制台 |
| Seata | 8091 | 分佈式事務協調器 |
| MySQL | 3306 | 數據庫 |
| Redis | 6379 | 緩存 |

## 完整測試流程

### 第一階段：基礎服務驗證

#### 1.1 檢查基礎服務狀態

```bash
# 檢查Docker容器狀態
docker-compose ps

# 檢查各服務日誌
docker-compose logs mysql
docker-compose logs nacos
docker-compose logs sentinel
docker-compose logs seata
docker-compose logs redis
```

#### 1.2 驗證Nacos服務註冊

1. 訪問 http://localhost:8848/nacos
2. 默認賬號密碼：nacos/nacos
3. 檢查服務列表是否正常顯示

#### 1.3 驗證Sentinel控制台

1. 訪問 http://localhost:8080
2. 檢查是否能正常顯示服務監控信息

#### 1.4 驗證數據庫連接

```bash
# 使用MySQL客戶端連接
mysql -h localhost -P 3306 -u cloud_user -p cloud_demo

# 檢查表結構
SHOW TABLES;
SELECT * FROM users LIMIT 5;
SELECT * FROM products LIMIT 5;
```

### 第二階段：微服務功能測試

#### 2.1 用戶服務測試

```bash
# 啟動用戶服務
mvn spring-boot:run -pl cloud-user-service

# 測試用戶註冊
curl -X POST http://localhost:8081/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "password123",
    "email": "test@example.com",
    "realName": "測試用戶"
  }'

# 測試用戶查詢
curl http://localhost:8081/api/users/1

# 測試用戶列表
curl http://localhost:8081/api/users
```

#### 2.2 商品服務測試

```bash
# 啟動商品服務
mvn spring-boot:run -pl cloud-product-service

# 測試創建商品
curl -X POST http://localhost:8082/api/products \
  -H "Content-Type: application/json" \
  -d '{
    "name": "測試商品",
    "description": "這是一個測試商品",
    "price": 99.99,
    "stock": 100,
    "brand": "測試品牌",
    "category": "ELECTRONICS"
  }'

# 測試商品查詢
curl http://localhost:8082/api/products/1

# 測試商品列表
curl http://localhost:8082/api/products

# 測試商品搜索
curl "http://localhost:8082/api/products/search/name?keyword=iPhone"
```

#### 2.3 訂單服務測試

```bash
# 啟動訂單服務
mvn spring-boot:run -pl cloud-order-service

# 測試創建訂單
curl -X POST http://localhost:8083/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1,
    "items": [
      {
        "productId": 1,
        "quantity": 2
      }
    ],
    "shippingAddress": "北京市朝陽區測試地址",
    "contactPhone": "13800138000",
    "contactName": "張三"
  }'

# 測試訂單查詢
curl http://localhost:8083/api/orders/1

# 測試訂單列表
curl http://localhost:8083/api/orders
```

### 第三階段：集成測試

#### 3.1 API網關測試

```bash
# 啟動API網關
mvn spring-boot:run -pl cloud-gateway

# 通過網關訪問用戶服務
curl http://localhost:8080/user-service/api/users

# 通過網關訪問商品服務
curl http://localhost:8080/product-service/api/products

# 通過網關訪問訂單服務
curl http://localhost:8080/order-service/api/orders
```

#### 3.2 服務間調用測試

```bash
# 測試用戶服務調用商品服務
# 這需要實現Feign客戶端

# 測試訂單服務調用用戶服務和商品服務
# 驗證分佈式事務是否正常工作
```

### 第四階段：性能與穩定性測試

#### 4.1 負載測試

```bash
# 使用Apache Bench進行簡單負載測試
ab -n 1000 -c 10 http://localhost:8080/user-service/api/users

ab -n 1000 -c 10 http://localhost:8080/product-service/api/products
```

#### 4.2 熔斷器測試

```bash
# 模擬服務不可用情況
# 檢查Sentinel熔斷器是否正常工作
```

#### 4.3 配置中心測試

```bash
# 在Nacos中修改配置
# 檢查服務是否能夠動態更新配置
```

## 監控與日誌

### 服務監控

- **Nacos**: 服務註冊與發現狀態
- **Sentinel**: 流量控制、熔斷器狀態
- **Seata**: 分佈式事務狀態
- **Spring Boot Actuator**: 應用健康檢查

### 日誌查看

```bash
# 查看服務日誌
docker-compose logs -f nacos
docker-compose logs -f sentinel

# 查看應用日誌
tail -f cloud-user-service/logs/application.log
tail -f cloud-product-service/logs/application.log
```

## 常見問題與解決方案

### 1. 服務啟動失敗

**問題**: 服務無法啟動，提示端口被占用
**解決**: 
```bash
# 檢查端口占用
netstat -tulpn | grep :8081

# 殺死佔用進程
kill -9 <PID>
```

### 2. 數據庫連接失敗

**問題**: 無法連接到MySQL數據庫
**解決**:
```bash
# 檢查MySQL容器狀態
docker-compose ps mysql

# 檢查MySQL日誌
docker-compose logs mysql

# 重啟MySQL服務
docker-compose restart mysql
```

### 3. Nacos服務註冊失敗

**問題**: 微服務無法註冊到Nacos
**解決**:
```bash
# 檢查Nacos容器狀態
docker-compose ps nacos

# 檢查網絡連接
docker network ls
docker network inspect cloud-alibaba-demo_cloud-network
```

### 4. 依賴版本衝突

**問題**: Maven依賴版本不兼容
**解決**:
```bash
# 清理Maven緩存
mvn clean

# 重新下載依賴
mvn dependency:resolve
```

## 開發指南

### 添加新的微服務

1. 在根目錄創建新的模組目錄
2. 在根pom.xml中添加模組
3. 創建模組的pom.xml文件
4. 實現業務邏輯
5. 在docker-compose.yml中添加相關配置

### 配置管理

- 開發環境配置：`application-dev.yml`
- 生產環境配置：`application-prod.yml`
- 公共配置：`application-common.yml`

### 數據庫設計原則

- 使用UTF8MB4字符集
- 添加適當的索引
- 使用外鍵約束保證數據完整性
- 遵循第三範式設計原則

## 部署說明

### 開發環境

```bash
# 使用Docker Compose啟動所有服務
docker-compose up -d

# 本地啟動微服務
mvn spring-boot:run -pl <module-name>
```

### 生產環境

1. 使用Docker Swarm或Kubernetes進行容器編排
2. 配置負載均衡器
3. 設置監控和告警
4. 配置日誌收集和分析
5. 實施安全策略

## 貢獻指南

1. Fork項目
2. 創建功能分支
3. 提交更改
4. 發起Pull Request

## 聯繫方式

如有問題或建議，請聯繫開發團隊或提交Issue。

## 許可證

本項目採用MIT許可證。
