#!/bin/bash

# Netty Cluster Server 启动脚本

echo "Starting Netty Cluster Server..."

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

# 获取端口参数
PORT=${1:-8080}
echo "Starting server on port $PORT..."

# 启动服务器
mvn exec:java -Dexec.mainClass="com.example.nettycluster.ClusterServer" -Dexec.args="$PORT"
