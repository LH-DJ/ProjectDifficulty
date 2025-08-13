#!/bin/bash

echo "========================================"
echo "   Netty Cluster 简化启动脚本"
echo "========================================"

# 检查Java版本
if ! command -v java &> /dev/null; then
    echo "错误: 未找到Java运行时环境"
    echo "请确保已安装JDK 17或更高版本"
    exit 1
fi

echo "正在启动简化的Netty集群应用..."
echo "使用配置文件: application-simple.yml"

# 设置环境变量
export SPRING_PROFILES_ACTIVE=simple

# 启动应用
java -jar target/netty-cluster-1.0.0.jar --spring.profiles.active=simple

echo ""
echo "应用已停止"
