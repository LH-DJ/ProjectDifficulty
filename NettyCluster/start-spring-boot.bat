@echo off
echo ========================================
echo    Netty Cluster Spring Boot 启动脚本
echo ========================================

REM 检查Java版本
java -version 2>nul
if errorlevel 1 (
    echo 错误: 未找到Java运行时环境
    echo 请确保已安装JDK 17或更高版本
    pause
    exit /b 1
)

REM 检查Maven
mvn -version 2>nul
if errorlevel 1 (
    echo 错误: 未找到Maven
    echo 请确保已安装Maven 3.6或更高版本
    pause
    exit /b 1
)

echo 正在启动Netty集群应用...

REM 启动Spring Boot应用
call mvn spring-boot:run

echo.
echo 应用已停止
pause
