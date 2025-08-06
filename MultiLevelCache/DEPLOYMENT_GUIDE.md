# MultiLevelCache 部署指南

## 📋 部署概述

本指南詳細介紹 MultiLevelCache 項目的部署流程，包括環境準備、配置管理、部署步驟和監控設置。

## 🏗️ 環境要求

### 1. 系統要求

- **操作系統**: Linux/Windows/macOS
- **JDK**: OpenJDK 8+ 或 Oracle JDK 8+
- **內存**: 最少 2GB，推薦 4GB+
- **磁盤空間**: 最少 1GB 可用空間
- **網絡**: 可訪問 MySQL 數據庫

### 2. 依賴服務

- **MySQL**: 8.0+
- **Maven**: 3.6+（僅開發環境需要）

### 3. 端口要求

- **應用端口**: 8080（可配置）
- **數據庫端口**: 3306
- **管理端口**: 8080（Actuator）

## 🔧 環境準備

### 1. JDK 安裝

#### Linux 環境
```bash
# Ubuntu/Debian
sudo apt update
sudo apt install openjdk-8-jdk

# CentOS/RHEL
sudo yum install java-1.8.0-openjdk-devel

# 驗證安裝
java -version
```

#### Windows 環境
1. 下載 OpenJDK 8 或 Oracle JDK 8
2. 安裝並設置環境變量
3. 驗證安裝：`java -version`

### 2. MySQL 安裝

#### Linux 環境
```bash
# Ubuntu/Debian
sudo apt install mysql-server

# CentOS/RHEL
sudo yum install mysql-server

# 啟動服務
sudo systemctl start mysqld
sudo systemctl enable mysqld
```

#### Windows 環境
1. 下載 MySQL 8.0 安裝包
2. 安裝並配置 root 密碼
3. 啟動 MySQL 服務

### 3. 數據庫初始化

```sql
-- 創建數據庫
CREATE DATABASE multilevel_cache CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 創建用戶（可選）
CREATE USER 'multilevel'@'localhost' IDENTIFIED BY 'your_password';
GRANT ALL PRIVILEGES ON multilevel_cache.* TO 'multilevel'@'localhost';
FLUSH PRIVILEGES;

-- 執行初始化腳本
source /path/to/MultiLevelCache/src/main/resources/sql/init.sql;
```

## 📦 應用部署

### 1. 開發環境部署

#### 步驟 1: 克隆項目
```bash
git clone <repository-url>
cd MultiLevelCache
```

#### 步驟 2: 編譯項目
```bash
mvn clean package -DskipTests
```

#### 步驟 3: 配置數據庫
```yaml
# 修改 src/main/resources/application.yml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/multilevel_cache
    username: your_username
    password: your_password
```

#### 步驟 4: 啟動應用
```bash
# 方式1: 使用 Maven
mvn spring-boot:run

# 方式2: 使用 JAR 文件
java -jar target/MultiLevelCache-2.0.0.jar

# 方式3: 使用啟動腳本（Windows）
start-app.bat
```

### 2. 生產環境部署

#### 步驟 1: 準備部署包
```bash
# 編譯生產版本
mvn clean package -DskipTests -Pprod

# 檢查生成的 JAR 文件
ls -la target/MultiLevelCache-2.0.0.jar
```

#### 步驟 2: 創建部署目錄
```bash
mkdir -p /opt/multilevelcache
cd /opt/multilevelcache
```

#### 步驟 3: 複製應用文件
```bash
# 複製 JAR 文件
cp target/MultiLevelCache-2.0.0.jar /opt/multilevelcache/

# 創建配置文件
mkdir -p /opt/multilevelcache/config
cp src/main/resources/application.yml /opt/multilevelcache/config/
```

#### 步驟 4: 創建啟動腳本
```bash
# 創建啟動腳本
cat > /opt/multilevelcache/start.sh << 'EOF'
#!/bin/bash

APP_NAME="MultiLevelCache"
APP_JAR="MultiLevelCache-2.0.0.jar"
APP_DIR="/opt/multilevelcache"
LOG_DIR="/opt/multilevelcache/logs"
PID_FILE="/opt/multilevelcache/app.pid"

# 創建日誌目錄
mkdir -p $LOG_DIR

# JVM 參數
JVM_OPTS="-Xms1g -Xmx2g -XX:+UseG1GC -XX:+UseStringDeduplication"

# 啟動應用
nohup java $JVM_OPTS \
    -Dspring.config.location=file:./config/application.yml \
    -Dlogging.file.path=$LOG_DIR \
    -jar $APP_JAR > $LOG_DIR/app.log 2>&1 &

echo $! > $PID_FILE
echo "應用啟動成功，PID: $(cat $PID_FILE)"
EOF

chmod +x /opt/multilevelcache/start.sh
```

#### 步驟 5: 創建停止腳本
```bash
cat > /opt/multilevelcache/stop.sh << 'EOF'
#!/bin/bash

PID_FILE="/opt/multilevelcache/app.pid"

if [ -f $PID_FILE ]; then
    PID=$(cat $PID_FILE)
    if ps -p $PID > /dev/null; then
        echo "停止應用 (PID: $PID)..."
        kill $PID
        rm -f $PID_FILE
        echo "應用已停止"
    else
        echo "應用未運行"
        rm -f $PID_FILE
    fi
else
    echo "PID 文件不存在，應用可能未運行"
fi
EOF

chmod +x /opt/multilevelcache/stop.sh
```

#### 步驟 6: 創建服務文件（Linux）
```bash
# 創建 systemd 服務文件
cat > /etc/systemd/system/multilevelcache.service << EOF
[Unit]
Description=MultiLevelCache Application
After=network.target mysql.service

[Service]
Type=forking
User=multilevelcache
Group=multilevelcache
WorkingDirectory=/opt/multilevelcache
ExecStart=/opt/multilevelcache/start.sh
ExecStop=/opt/multilevelcache/stop.sh
PIDFile=/opt/multilevelcache/app.pid
Restart=always
RestartSec=10

[Install]
WantedBy=multi-user.target
EOF

# 創建用戶
sudo useradd -r -s /bin/false multilevelcache
sudo chown -R multilevelcache:multilevelcache /opt/multilevelcache

# 啟用服務
sudo systemctl daemon-reload
sudo systemctl enable multilevelcache
```

### 3. Docker 部署

#### 步驟 1: 創建 Dockerfile
```dockerfile
FROM openjdk:8-jre-alpine

# 創建應用目錄
WORKDIR /app

# 複製 JAR 文件
COPY target/MultiLevelCache-2.0.0.jar app.jar

# 創建配置目錄
RUN mkdir -p /app/config

# 複製配置文件
COPY src/main/resources/application.yml /app/config/

# 暴露端口
EXPOSE 8080

# 啟動命令
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.config.location=file:./config/application.yml"]
```

#### 步驟 2: 構建 Docker 鏡像
```bash
docker build -t multilevelcache:2.0.0 .
```

#### 步驟 3: 運行容器
```bash
docker run -d \
  --name multilevelcache \
  -p 8080:8080 \
  -e SPRING_DATASOURCE_URL=jdbc:mysql://host.docker.internal:3306/multilevel_cache \
  -e SPRING_DATASOURCE_USERNAME=your_username \
  -e SPRING_DATASOURCE_PASSWORD=your_password \
  multilevelcache:2.0.0
```

## ⚙️ 配置管理

### 1. 應用配置

#### 基礎配置 (application.yml)
```yaml
server:
  port: 8080

spring:
  application:
    name: MultiLevelCache
  
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/multilevel_cache
    username: ${DB_USERNAME:root}
    password: ${DB_PASSWORD:your_password}
    hikari:
      maximum-pool-size: 10
      minimum-idle: 5
      connection-timeout: 30000

cache:
  caffeine:
    transaction:
      maximum-size: 1000
      expire-after-write: 30m
      expire-after-access: 10m
    blacklist:
      maximum-size: 500
      expire-after-write: 60m
      expire-after-access: 20m

logging:
  level:
    com.multilevelcache: info
    root: warn
  file:
    path: /opt/multilevelcache/logs

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
  endpoint:
    health:
      show-details: when-authorized
```

#### 環境特定配置

**開發環境 (application-dev.yml)**
```yaml
logging:
  level:
    com.multilevelcache: debug
    root: info

management:
  endpoint:
    health:
      show-details: always
```

**生產環境 (application-prod.yml)**
```yaml
logging:
  level:
    com.multilevelcache: warn
    root: error

management:
  endpoint:
    health:
      show-details: never
```

### 2. JVM 配置

#### 開發環境
```bash
java -Xms512m -Xmx1g -jar MultiLevelCache-2.0.0.jar
```

#### 生產環境
```bash
java -Xms1g -Xmx2g \
  -XX:+UseG1GC \
  -XX:+UseStringDeduplication \
  -XX:+HeapDumpOnOutOfMemoryError \
  -XX:HeapDumpPath=/opt/multilevelcache/logs \
  -jar MultiLevelCache-2.0.0.jar
```

## 📊 監控配置

### 1. 健康檢查

```bash
# 基礎健康檢查
curl http://localhost:8080/actuator/health

# 詳細健康信息
curl http://localhost:8080/actuator/health -H "Accept: application/json"
```

### 2. 指標監控

```bash
# 應用指標
curl http://localhost:8080/actuator/metrics

# 緩存指標
curl http://localhost:8080/actuator/metrics/cache.gets
curl http://localhost:8080/actuator/metrics/cache.miss
```

### 3. 日誌監控

```bash
# 查看應用日誌
tail -f /opt/multilevelcache/logs/app.log

# 查看錯誤日誌
grep ERROR /opt/multilevelcache/logs/app.log
```

## 🔒 安全配置

### 1. 數據庫安全

```sql
-- 創建專用用戶
CREATE USER 'multilevelcache'@'localhost' IDENTIFIED BY 'strong_password';

-- 限制權限
GRANT SELECT, INSERT, UPDATE, DELETE ON multilevel_cache.* TO 'multilevelcache'@'localhost';

-- 撤銷不必要權限
REVOKE ALL PRIVILEGES ON *.* FROM 'multilevelcache'@'localhost';
FLUSH PRIVILEGES;
```

### 2. 網絡安全

```bash
# 防火牆配置（Linux）
sudo ufw allow 8080/tcp
sudo ufw deny 3306/tcp  # 除非需要遠程訪問

# Windows 防火牆
netsh advfirewall firewall add rule name="MultiLevelCache" dir=in action=allow protocol=TCP localport=8080
```

### 3. 應用安全

```yaml
# 禁用敏感端點（生產環境）
management:
  endpoints:
    web:
      exposure:
        include: health,info
        exclude: shutdown,env,configprops
  endpoint:
    health:
      show-details: never
```

## 🚨 故障排除

### 1. 常見問題

#### 問題 1: 應用無法啟動
```bash
# 檢查端口佔用
netstat -tulpn | grep 8080

# 檢查日誌
tail -f /opt/multilevelcache/logs/app.log

# 檢查數據庫連接
mysql -u your_username -p -h localhost multilevel_cache
```

#### 問題 2: 數據庫連接失敗
```bash
# 檢查 MySQL 服務狀態
sudo systemctl status mysqld

# 檢查數據庫連接
mysql -u your_username -p -h localhost

# 檢查網絡連接
telnet localhost 3306
```

#### 問題 3: 緩存不生效
```bash
# 檢查緩存配置
curl http://localhost:8080/actuator/caches

# 檢查緩存統計
curl http://localhost:8080/actuator/metrics/cache.gets
```

### 2. 性能調優

#### JVM 調優
```bash
# 監控 GC 情況
jstat -gc <pid> 1000

# 分析內存使用
jmap -histo <pid>

# 生成堆轉儲
jmap -dump:format=b,file=heap.hprof <pid>
```

#### 數據庫調優
```sql
-- 檢查慢查詢
SHOW VARIABLES LIKE 'slow_query_log';
SHOW VARIABLES LIKE 'long_query_time';

-- 優化表
OPTIMIZE TABLE transaction;
OPTIMIZE TABLE blacklist;
```

## 📈 擴展部署

### 1. 負載均衡

#### Nginx 配置
```nginx
upstream multilevelcache {
    server 192.168.1.10:8080;
    server 192.168.1.11:8080;
    server 192.168.1.12:8080;
}

server {
    listen 80;
    server_name your-domain.com;

    location / {
        proxy_pass http://multilevelcache;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    }
}
```

### 2. 集群部署

#### 應用集群
```bash
# 在多個服務器上部署應用
# 服務器1
ssh server1 "cd /opt/multilevelcache && ./start.sh"

# 服務器2
ssh server2 "cd /opt/multilevelcache && ./start.sh"

# 服務器3
ssh server3 "cd /opt/multilevelcache && ./start.sh"
```

#### 數據庫集群
```sql
-- 主從複製配置
-- 主庫
GRANT REPLICATION SLAVE ON *.* TO 'repl'@'%' IDENTIFIED BY 'repl_password';

-- 從庫
CHANGE MASTER TO
    MASTER_HOST='master_ip',
    MASTER_USER='repl',
    MASTER_PASSWORD='repl_password',
    MASTER_LOG_FILE='mysql-bin.000001',
    MASTER_LOG_POS=154;
```

## 📝 部署檢查清單

### 部署前檢查
- [ ] 環境要求滿足
- [ ] 數據庫已初始化
- [ ] 配置文件正確
- [ ] 端口未被佔用
- [ ] 權限設置正確

### 部署後檢查
- [ ] 應用正常啟動
- [ ] 健康檢查通過
- [ ] 數據庫連接正常
- [ ] 緩存功能正常
- [ ] 日誌記錄正常
- [ ] 性能指標達標

### 監控告警設置
- [ ] 響應時間監控
- [ ] 錯誤率監控
- [ ] 內存使用監控
- [ ] 數據庫連接監控
- [ ] 緩存命中率監控

---

**注意**: 本部署指南應根據實際環境和需求進行調整。 