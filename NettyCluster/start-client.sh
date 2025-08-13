#!/bin/bash

# Netty Cluster Client 启动脚本

echo "Starting Netty Cluster Client..."

# 检查Java是否安装
if ! command -v java &> /dev/null; then
    echo "Error: Java is not installed or not in PATH"
    exit 1
fi

# 检查Maven是否安装
if ! command -v mvn &> /dev/null; then
    echo "Error: Maven is not installed or not in PATH"
    exit 1
fi

# 编译项目
echo "Compiling project..."
mvn clean compile

if [ $? -ne 0 ]; then
    echo "Error: Compilation failed"
    exit 1
fi

# 获取服务器地址和端口参数
HOST=${1:-localhost}
PORT=${2:-8080}

echo "Connecting to server $HOST:$PORT..."

# 启动客户端
mvn exec:java -Dexec.mainClass="com.example.nettycluster.ClusterClient" -Dexec.args="$HOST $PORT"
