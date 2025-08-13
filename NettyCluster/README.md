# Netty集群系统

一个基于Netty的高性能集群系统，集成Spring Boot 3+、Spring Cloud Alibaba、Nacos、Gateway等现代技术栈。

## 🚀 技术栈

### 核心框架
- **JDK 17** - 最新的LTS版本，支持现代Java特性
- **Spring Boot 3.2.0** - 最新的Spring Boot版本，基于Spring 6
- **Spring Cloud 2023.0.0** - 最新的Spring Cloud版本
- **Spring Cloud Alibaba 2022.0.0.0** - 阿里巴巴Spring Cloud生态

### 网络通信
- **Netty 4.1.100.Final** - 高性能异步事件驱动网络应用框架
- **Spring Cloud Gateway** - 基于Spring WebFlux的API网关

### 服务治理
- **Nacos** - 动态服务发现、配置管理和服务管理平台
- **Spring Cloud LoadBalancer** - 客户端负载均衡器

### 监控和运维
- **Spring Boot Actuator** - 生产就绪特性
- **Prometheus + Grafana** - 监控和可视化
- **Docker + Docker Compose** - 容器化部署

## 🔍 Netty集群原理详解

### 1. 核心概念

#### EventLoop和EventLoopGroup
- **EventLoop**: 一个事件循环，负责处理一个或多个Channel上的所有事件
- **EventLoopGroup**: 包含多个EventLoop的组，用于管理多个连接
- **BossGroup**: 负责接受客户端连接，通常只有一个EventLoop
- **WorkerGroup**: 负责处理已接受连接的I/O操作，通常有多个EventLoop

#### Channel和ChannelPipeline
- **Channel**: 代表一个网络连接，可以是Socket连接
- **ChannelPipeline**: 包含多个ChannelHandler的处理器链
- **ChannelHandler**: 处理I/O事件的具体逻辑

### 2. 集群通信机制

#### 消息编解码
使用LengthFieldBasedFrameDecoder和LengthFieldPrepender实现消息帧的自动分割和组装

#### 心跳机制
- **心跳间隔**: 定期发送心跳包检测节点存活状态
- **心跳超时**: 超过指定时间未收到心跳则认为节点离线
- **自动清理**: 定期清理死亡节点，维护集群健康

#### 节点发现和注册
- **自动注册**: 节点启动时自动注册到Nacos
- **服务发现**: 通过Nacos发现其他集群节点
- **动态更新**: 节点状态变化时自动更新注册信息

### 3. 负载均衡策略

#### 轮询策略 (Round Robin)
按顺序选择可用节点，实现负载的均匀分布

#### 最少连接策略 (Least Connections)
选择当前连接数最少的节点，实现负载的智能分配

#### 随机策略 (Random)
随机选择可用节点，适用于节点性能相近的场景

### 4. 集群状态管理

#### 节点状态
- **ONLINE**: 节点在线，可以接收请求
- **OFFLINE**: 节点离线，不参与负载均衡
- **SUSPECT**: 节点可疑，心跳超时但未确认离线
- **FAILED**: 节点失败，多次心跳超时

#### 状态同步
- **实时更新**: 节点状态变化时实时更新
- **一致性保证**: 使用读写锁保证状态一致性
- **故障转移**: 节点故障时自动切换到其他节点

## 📋 系统要求

- **JDK**: 17+
- **Maven**: 3.6+
- **Docker**: 20.10+ (可选)

## 🚀 快速开始

### 1. 环境准备
确保已安装JDK 17和Maven

### 2. 启动Nacos
使用Docker Compose启动Nacos：
```bash
docker-compose up nacos mysql -d
```

### 3. 构建项目
```bash
mvn clean package
```

### 4. 启动应用
```bash
mvn spring-boot:run
```

### 5. 验证部署
- **健康检查**: http://localhost:8080/actuator/health
- **集群状态**: http://localhost:8080/api/cluster/status
- **Nacos控制台**: http://localhost:8848/nacos

## 🔧 配置说明

### 主要配置项
```yaml
netty:
  cluster:
    server:
      port: 9090
      boss-threads: 1
      worker-threads: 4
      max-connections: 1000
    node:
      id: ${random.uuid}
      name: netty-cluster
    discovery:
      heartbeat-interval: 30000
      heartbeat-timeout: 90000
    loadbalancer:
      strategy: round-robin
```

## 📊 监控和运维

### 健康检查
系统提供完整的健康检查端点，包括集群状态、节点健康等

### 指标监控
集成Prometheus指标收集，支持Grafana可视化

### 日志管理
支持结构化日志输出和自动轮转

## 🧪 测试

### 单元测试
```bash
mvn test
```

### 集成测试
```bash
mvn verify
```

## 🔄 扩展开发

### 添加新的负载均衡策略
1. 实现`LoadBalancer`接口
2. 在`LoadBalancerFactory`中注册
3. 配置文件中指定策略名称

### 添加新的序列化方式
1. 实现`MessageSerializer`接口
2. 在`MessageSerializerFactory`中注册
3. 配置文件中指定序列化类型

## 🐛 故障排除

### 常见问题
1. **节点无法连接**: 检查Nacos服务和网络配置
2. **心跳超时**: 调整心跳参数和检查网络稳定性
3. **负载均衡不生效**: 验证策略配置和节点状态

## 📈 性能优化

### JVM调优
```bash
java -Xms2g -Xmx4g -XX:+UseG1GC -jar netty-cluster.jar
```

### Netty调优
- Boss线程通常保持为1
- Worker线程数为CPU核心数的2倍
- 根据内存调整最大连接数

## 📄 许可证

本项目采用MIT许可证

## 📝 更新日志

### v1.0.0 (2024-01-XX)
- ✨ 初始版本发布
- 🚀 集成Spring Boot 3+和JDK 17
- 🔧 集成Spring Cloud Alibaba生态
- 📡 支持Nacos服务发现和配置管理
- 🌐 集成Spring Cloud Gateway
- 📊 完整的监控和健康检查
- 🐳 Docker容器化支持
