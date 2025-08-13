# 启动问题诊断和修复指南

## 🚨 常见启动问题

### 1. 依赖冲突问题

#### 问题描述
```
Caused by: java.lang.ClassNotFoundException: com.alibaba.cloud.nacos.NacosServiceManager
```

#### 原因分析
- Nacos相关依赖版本不兼容
- Spring Cloud Alibaba版本与Spring Boot版本不匹配
- 依赖缺失或冲突

#### 解决方案
1. **使用简化版本启动**：
   ```bash
   # Windows
   start-simple.bat
   
   # Linux/Mac
   ./start-simple.sh
   ```

2. **检查依赖版本**：
   ```xml
   <!-- 确保版本兼容性 -->
   <spring-boot.version>3.2.0</spring-boot.version>
   <spring-cloud.version>2023.0.0</spring-cloud.version>
   <spring-cloud-alibaba.version>2022.0.0.0</spring-cloud-alibaba.version>
   ```

### 2. 配置绑定问题

#### 问题描述
```
Caused by: org.springframework.boot.context.properties.bind.BindException: Failed to bind properties under 'netty.cluster'
```

#### 原因分析
- 配置文件格式错误
- 配置属性类字段不匹配
- 配置文件路径错误

#### 解决方案
1. **使用简化配置**：
   ```bash
   --spring.profiles.active=simple
   ```

2. **检查配置文件**：
   - 确保`application-simple.yml`存在
   - 验证YAML语法正确
   - 检查配置属性类字段

### 3. Bean创建失败

#### 问题描述
```
Caused by: org.springframework.beans.factory.BeanCreationException: Error creating bean with name 'clusterServer'
```

#### 原因分析
- 构造函数参数不匹配
- 依赖注入失败
- 循环依赖

#### 解决方案
1. **使用简化配置类**：
   - `SimpleDiscoveryService`替代`NacosDiscoveryService`
   - 避免复杂的依赖注入

2. **检查Bean定义**：
   ```java
   @Configuration
   public class AppConfig {
       @Bean
       public ClusterServer clusterServer(ClusterManager clusterManager, 
                                       ClusterConfig clusterConfig, 
                                       SimpleDiscoveryService discoveryService) {
           return new ClusterServer(clusterConfig, clusterManager, discoveryService);
       }
   }
   ```

## 🔧 启动步骤

### 步骤1: 环境检查
```bash
# 检查Java版本
java -version

# 检查Maven版本（如果使用）
mvn -version

# 检查项目结构
ls -la src/main/java/com/example/nettycluster/
```

### 步骤2: 清理和编译
```bash
# 清理项目
mvn clean

# 编译项目
mvn compile

# 打包项目
mvn package
```

### 步骤3: 启动应用
```bash
# 使用简化配置启动
java -jar target/netty-cluster-1.0.0.jar --spring.profiles.active=simple

# 或者使用启动脚本
./start-simple.sh  # Linux/Mac
start-simple.bat   # Windows
```

## 📋 配置文件说明

### 简化配置文件 (application-simple.yml)
```yaml
# 基本配置
server:
  port: 8080

spring:
  application:
    name: netty-cluster-simple
  profiles:
    active: simple

# Netty集群配置
netty:
  cluster:
    server:
      port: 9090
      boss-threads: 1
      worker-threads: 2
      max-connections: 100
    node:
      id: simple-node-001
      name: simple-netty-cluster
      host: localhost
    discovery:
      heartbeat-interval: 10000
      heartbeat-timeout: 30000
    loadbalancer:
      strategy: round-robin
    serialization:
      type: json
```

### 完整配置文件 (application.yml)
```yaml
# 包含Nacos、Gateway等完整功能
spring:
  cloud:
    nacos:
      discovery:
        server-addr: localhost:8848
    gateway:
      # Gateway配置
    loadbalancer:
      # 负载均衡配置
```

## 🐛 调试技巧

### 1. 启用调试日志
```yaml
logging:
  level:
    com.example.nettycluster: DEBUG
    org.springframework: DEBUG
    io.netty: DEBUG
```

### 2. 查看启动日志
```bash
# 启动时查看详细日志
java -jar target/netty-cluster-1.0.0.jar --spring.profiles.active=simple --debug

# 或者设置日志级别
java -jar target/netty-cluster-1.0.0.jar --logging.level.com.example.nettycluster=DEBUG
```

### 3. 健康检查
```bash
# 应用启动后检查健康状态
curl http://localhost:8080/actuator/health

# 查看应用信息
curl http://localhost:8080/actuator/info
```

## 🔄 渐进式启动策略

### 阶段1: 基础功能验证
- 使用`SimpleTestApplication`启动
- 验证Spring Boot基本功能
- 检查配置加载

### 阶段2: Netty集成验证
- 启动`ClusterServer`
- 验证Netty服务器启动
- 检查端口监听

### 阶段3: 集群功能验证
- 启动`ClusterManager`
- 验证节点管理
- 检查心跳机制

### 阶段4: 完整功能验证
- 集成Nacos服务发现
- 启动Spring Cloud Gateway
- 验证完整功能

## 📞 获取帮助

### 1. 查看日志
- 检查控制台输出
- 查看日志文件
- 分析错误堆栈

### 2. 常见错误码
- `BeanCreationException`: Bean创建失败
- `BindException`: 配置绑定失败
- `ClassNotFoundException`: 类找不到
- `NoSuchMethodError`: 方法不存在

### 3. 联系支持
- 查看项目文档
- 检查GitHub Issues
- 提交问题报告

## ✅ 成功启动标志

应用成功启动后，您应该看到：

```
2024-01-XX XX:XX:XX.XXX  INFO  [main] c.e.n.SimpleTestApplication - 启动简化的Netty集群测试应用...
2024-01-XX XX:XX:XX.XXX  INFO  [main] o.s.b.w.e.t.TomcatWebServer - Tomcat started on port(s): 8080 (http)
2024-01-XX XX:XX:XX.XXX  INFO  [main] c.e.n.SimpleTestApplication - 简化应用启动成功！
```

然后可以访问：
- http://localhost:8080/actuator/health - 健康检查
- http://localhost:8080/actuator/info - 应用信息
- http://localhost:8080/api/cluster/status - 集群状态（如果配置了控制器）

---

**注意**: 如果仍然遇到问题，请使用简化配置启动，这样可以避免复杂的依赖问题，专注于核心功能验证。
