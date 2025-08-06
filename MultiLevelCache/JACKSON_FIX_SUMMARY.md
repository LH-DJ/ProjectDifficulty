# Jackson LocalDateTime 序列化問題修復總結

## 🐛 問題描述

在二級緩存架構中，當嘗試序列化包含 `LocalDateTime` 字段的實體類時，出現以下錯誤：

```
Could not write JSON: Java 8 date/time type `java.time.LocalDateTime` not supported by default: add Module "com.fasterxml.jackson.datatype:jackson-datatype-jsr310" to enable handling
```

## 🔍 問題原因

1. **Jackson 默認不支持 Java 8 時間類型**: Jackson 默認只支持舊的 `java.util.Date` 類型
2. **實體類使用 LocalDateTime**: `Transaction` 和 `Blacklist` 實體類都使用了 `LocalDateTime` 字段
3. **Redis 序列化需要**: 二級緩存需要將對象序列化為 JSON 存儲到 Redis

## ✅ 修復方案

### 1. 添加 Jackson JSR310 依賴

在 `pom.xml` 中添加：

```xml
<!-- Jackson JSR310 for Java 8 Date/Time -->
<dependency>
    <groupId>com.fasterxml.jackson.datatype</groupId>
    <artifactId>jackson-datatype-jsr310</artifactId>
</dependency>
```

### 2. 創建全局 Jackson 配置

新增 `JacksonConfig.java`：

```java
@Configuration
public class JacksonConfig {

    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        
        // 設置可見性
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        
        // 啟用默認類型處理
        objectMapper.activateDefaultTyping(
            LaissezFaireSubTypeValidator.instance, 
            ObjectMapper.DefaultTyping.NON_FINAL
        );
        
        // 註冊 Java 8 時間模塊
        objectMapper.registerModule(new JavaTimeModule());
        
        return objectMapper;
    }
}
```

### 3. 更新 Redis 配置

修改 `RedisCacheConfig.java` 使用全局配置的 ObjectMapper：

```java
@Configuration
@EnableCaching
public class RedisCacheConfig {

    @Autowired
    private ObjectMapper objectMapper;

    @Bean("redisCacheManager")
    public CacheManager redisCacheManager(RedisConnectionFactory connectionFactory) {
        // 使用全局配置的 ObjectMapper
        GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer(objectMapper);
        // ... 其他配置
    }
}
```

### 4. 添加測試控制器

新增 `TestController.java` 來驗證修復效果：

```java
@RestController
@RequestMapping("/api/test")
public class TestController {

    @GetMapping("/datetime")
    public ApiResponse<Map<String, Object>> testDateTime() {
        // 創建包含 LocalDateTime 的測試數據
        Transaction transaction = new Transaction();
        transaction.setCreatedAt(LocalDateTime.now());
        // ... 其他字段設置
        
        return ApiResponse.builder()
                .success(true)
                .message("LocalDateTime 序列化測試成功")
                .data(result)
                .build();
    }
}
```

## 📋 修改的文件

### 新增文件
- `src/main/java/com/multilevelcache/config/JacksonConfig.java` - 全局 Jackson 配置
- `src/main/java/com/multilevelcache/controller/TestController.java` - 測試控制器

### 修改文件
- `pom.xml` - 添加 Jackson JSR310 依賴
- `src/main/java/com/multilevelcache/config/RedisCacheConfig.java` - 使用全局 ObjectMapper
- `test-cache.bat` - 添加 LocalDateTime 測試

## 🧪 測試驗證

### 1. 直接測試
```bash
# 測試 LocalDateTime 序列化
curl http://localhost:8080/api/test/datetime

# 測試 JSON 序列化
curl -X POST http://localhost:8080/api/test/json \
  -H "Content-Type: application/json" \
  -d '{"testTime":"2024-01-01T12:00:00","message":"測試 LocalDateTime"}'
```

### 2. 緩存測試
```bash
# 測試緩存操作
curl -X POST http://localhost:8080/api/cache/transactionCache/test \
  -H "Content-Type: application/json" \
  -d '{"transactionId":"TEST001","createdAt":"2024-01-01T12:00:00"}'

curl http://localhost:8080/api/cache/transactionCache/test
```

### 3. 自動化測試
```bash
# 運行完整測試腳本
test-cache.bat
```

## 📊 修復效果

### 修復前
- ❌ LocalDateTime 序列化失敗
- ❌ Redis 緩存無法存儲包含時間字段的對象
- ❌ 二級緩存功能受限

### 修復後
- ✅ LocalDateTime 正常序列化
- ✅ Redis 緩存可以存儲完整對象
- ✅ 二級緩存功能完整可用
- ✅ 支持所有 Java 8 時間類型 (LocalDateTime, LocalDate, LocalTime 等)

## 🔧 配置說明

### Jackson 配置特點
1. **JavaTimeModule**: 支持 Java 8 時間類型
2. **可見性設置**: 允許序列化所有字段
3. **類型處理**: 支持多態序列化
4. **全局配置**: 確保所有地方使用相同配置

### 支持的 Java 8 時間類型
- `LocalDateTime` - 日期時間
- `LocalDate` - 日期
- `LocalTime` - 時間
- `ZonedDateTime` - 帶時區的日期時間
- `Instant` - 時間戳

## 🚀 使用示例

### 實體類中的 LocalDateTime
```java
@Data
public class Transaction {
    private String transactionId;
    private LocalDateTime createdAt;  // ✅ 現在可以正常序列化
    private LocalDateTime updatedAt;  // ✅ 現在可以正常序列化
}
```

### 緩存操作
```java
// 存入緩存 (會自動序列化 LocalDateTime)
cacheService.put("transactionCache", "key", transaction);

// 從緩存讀取 (會自動反序列化 LocalDateTime)
Transaction transaction = cacheService.get("transactionCache", "key", Transaction.class);
```

## ✅ 修復完成

現在您的 MultiLevelCache 項目已經完全支持 Java 8 時間類型的序列化，二級緩存功能可以正常處理包含 `LocalDateTime` 字段的實體類。

---

**🎉 Jackson LocalDateTime 序列化問題已成功修復！** 