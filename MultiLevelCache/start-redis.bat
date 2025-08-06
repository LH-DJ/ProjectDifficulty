@echo off
chcp 65001 >nul
echo ========================================
echo 🚀 多級緩存應用啟動腳本 (Redis 模式)
echo ========================================

REM 檢查端口是否被佔用
netstat -an | findstr :8080 >nul
if %errorlevel% equ 0 (
    echo ❌ 端口 8080 已被佔用，請檢查是否有其他應用正在運行
    pause
    exit /b 1
)

echo ✅ 端口檢查通過

REM 檢查 Redis 是否運行
echo 🔍 檢查 Redis 服務狀態...
redis-cli ping >nul 2>&1
if %errorlevel% neq 0 (
    echo ⚠️  Redis 服務未運行或無法連接
    echo 💡 請確保 Redis 服務已啟動 (localhost:6379)
    echo 💡 可以使用以下命令啟動 Redis:
    echo    - Windows: redis-server
    echo    - Docker: docker run -d -p 6379:6379 redis:latest
    echo.
    set /p choice="是否繼續啟動應用？(y/n): "
    if /i "%choice%" neq "y" (
        echo 啟動已取消
        pause
        exit /b 1
    )
) else (
    echo ✅ Redis 服務正常
)

echo.
echo 📦 正在啟動多級緩存應用 (Redis 模式)...
echo 🌐 訪問地址: http://localhost:8080
echo 🏥 健康檢查: http://localhost:8080/actuator/health
echo 📊 緩存監控: http://localhost:8080/actuator/caches
echo.

REM 啟動應用
java -jar target/MultiLevelCache-2.0.0.jar --spring.profiles.active=redis

echo.
echo 🛑 應用已停止
pause 