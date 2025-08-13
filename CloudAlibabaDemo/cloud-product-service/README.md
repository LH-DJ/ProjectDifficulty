# 商品服務 (Product Service)

## 概述

商品服務是微服務架構中的核心服務之一，負責管理商品的生命週期，包括商品的創建、更新、刪除、庫存管理等操作。

## 功能特性

### 商品管理
- 商品的增刪改查
- 商品狀態管理（啟用、停用、缺貨、停產）
- 商品分類管理
- 商品品牌和型號管理

### 庫存管理
- 庫存查詢和更新
- 庫存增減操作
- 低庫存預警
- 庫存充足性檢查

### 搜索功能
- 按名稱、描述、品牌、型號搜索
- 按價格範圍搜索
- 按庫存範圍搜索
- 按狀態和類別組合搜索
- 分頁查詢支援

### 數據驗證
- 輸入參數驗證
- 業務規則驗證
- 統一的錯誤處理

## 技術架構

- **框架**: Spring Boot 2.7.18
- **數據庫**: MySQL + Spring Data JPA
- **服務發現**: Nacos Discovery
- **配置管理**: Nacos Config
- **流量控制**: Sentinel
- **分佈式事務**: Seata
- **API文檔**: OpenAPI 3.0

## API 端點

### 商品基本操作
- `POST /api/products` - 創建商品
- `PUT /api/products/{id}` - 更新商品
- `GET /api/products/{id}` - 根據ID獲取商品
- `GET /api/products/name/{name}` - 根據名稱獲取商品
- `DELETE /api/products/{id}` - 刪除商品

### 商品查詢
- `GET /api/products` - 獲取所有商品
- `GET /api/products/page` - 分頁獲取商品
- `GET /api/products/status/{status}` - 根據狀態獲取商品
- `GET /api/products/category/{category}` - 根據類別獲取商品
- `GET /api/products/brand/{brand}` - 根據品牌獲取商品

### 庫存管理
- `PUT /api/products/{id}/stock` - 更新商品庫存
- `PUT /api/products/{id}/stock/increase` - 增加商品庫存
- `PUT /api/products/{id}/stock/decrease` - 減少商品庫存
- `GET /api/products/{id}/stock/check` - 檢查庫存是否充足

### 商品狀態管理
- `PUT /api/products/{id}/activate` - 啟用商品
- `PUT /api/products/{id}/deactivate` - 停用商品
- `PUT /api/products/{id}/out-of-stock` - 設置商品缺貨
- `PUT /api/products/{id}/discontinued` - 設置商品停產

### 搜索功能
- `GET /api/products/search/name?keyword={keyword}` - 按名稱搜索
- `GET /api/products/search/description?keyword={keyword}` - 按描述搜索
- `GET /api/products/search/brand?keyword={keyword}` - 按品牌搜索
- `GET /api/products/search/model?keyword={keyword}` - 按型號搜索

### 高級查詢
- `GET /api/products/price-range?minPrice={min}&maxPrice={max}` - 按價格範圍查詢
- `GET /api/products/stock-range?minStock={min}&maxStock={max}` - 按庫存範圍查詢
- `GET /api/products/low-stock?threshold={threshold}` - 查找低庫存商品
- `GET /api/products/below-price/{price}` - 查找低於指定價格的商品
- `GET /api/products/above-price/{price}` - 查找高於指定價格的商品

## 數據模型

### Product 實體
```java
@Entity
public class Product {
    private Long id;                    // 商品ID
    private String name;                // 商品名稱
    private String description;         // 商品描述
    private BigDecimal price;           // 商品價格
    private Integer stock;              // 庫存數量
    private String imageUrl;            // 商品圖片URL
    private String brand;               // 品牌
    private String model;               // 型號
    private BigDecimal weight;          // 重量
    private String weightUnit;          // 重量單位
    private String dimensionUnit;       // 尺寸單位
    private BigDecimal length;          // 長度
    private BigDecimal width;           // 寬度
    private BigDecimal height;          // 高度
    private ProductStatus status;       // 商品狀態
    private ProductCategory category;   // 商品類別
    private LocalDateTime createTime;   // 創建時間
    private LocalDateTime updateTime;   // 更新時間
}
```

### ProductStatus 枚舉
- `ACTIVE` - 啟用
- `INACTIVE` - 停用
- `OUT_OF_STOCK` - 缺貨
- `DISCONTINUED` - 停產

### ProductCategory 枚舉
- `ELECTRONICS` - 電子產品
- `CLOTHING` - 服裝
- `BOOKS` - 圖書
- `HOME_AND_GARDEN` - 家居園藝
- `SPORTS` - 運動用品
- `BEAUTY` - 美妝護理
- `AUTOMOTIVE` - 汽車用品
- `OTHER` - 其他

## 配置說明

### 數據庫配置
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/product_db?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
    username: root
    password: password
    driver-class-name: com.mysql.cj.jdbc.Driver
```

### Nacos 配置
```yaml
spring:
  cloud:
    nacos:
      discovery:
        server-addr: localhost:8848
      config:
        server-addr: localhost:8848
        file-extension: yml
```

### Sentinel 配置
```yaml
spring:
  cloud:
    sentinel:
      transport:
        dashboard: localhost:8080
      datasource:
        ds:
          nacos:
            server-addr: localhost:8848
            dataId: sentinel-rules
            groupId: DEFAULT_GROUP
            rule-type: flow
```

## 部署說明

### 環境要求
- JDK 8+
- MySQL 5.7+
- Nacos 2.0+
- Sentinel 1.8+

### 啟動步驟
1. 確保 MySQL 數據庫已啟動並創建數據庫
2. 確保 Nacos 服務已啟動
3. 修改 `application-dev.yml` 中的數據庫連接信息
4. 運行 `ProductServiceApplication.java`

### 端口配置
- 默認端口: 8082
- 可通過 `server.port` 配置修改

## 監控和日誌

### 日誌配置
- 使用 SLF4J + Logback
- 日誌級別: INFO
- 日誌格式: 包含時間戳、線程、類名等信息

### 健康檢查
- 健康檢查端點: `/actuator/health`
- 數據庫連接檢查
- 服務狀態檢查

## 錯誤處理

### 業務異常
- `BusinessException`: 業務邏輯異常
- 統一的錯誤碼和錯誤信息
- 結構化的錯誤響應

### 驗證異常
- 參數驗證失敗
- 數據格式錯誤
- 業務規則違反

## 性能優化

### 數據庫優化
- 索引優化
- 查詢優化
- 連接池配置

### 緩存策略
- 商品信息緩存
- 庫存信息緩存
- 查詢結果緩存

## 安全考慮

### 輸入驗證
- 參數驗證
- SQL 注入防護
- XSS 防護

### 權限控制
- 接口權限控制
- 數據權限控制
- 操作日誌記錄

## 擴展性

### 水平擴展
- 無狀態設計
- 負載均衡支援
- 服務實例擴展

### 功能擴展
- 插件化架構
- 配置化功能
- 擴展點設計

## 聯繫方式

如有問題或建議，請聯繫開發團隊。
