#!/bin/bash

echo "========================================"
echo "   Netty Cluster Spring Boot 启动脚本"
echo "========================================"

# 检查Java版本
if ! command -v java &> /dev/null; then
    echo "错误: 未找到Java运行时环境"
    echo "请确保已安装JDK 17或更高版本"
    exit 1
fi

# 检查Maven
if ! command -v mvn &> /dev/null; then
    echo "错误: 未找到Maven"
    echo "请确保已安装Maven 3.6或更高版本"
    exit 1
fi

echo "正在启动Netty集群应用..."

# 启动Spring Boot应用
mvn spring-boot:run

echo ""
echo "应用已停止"
