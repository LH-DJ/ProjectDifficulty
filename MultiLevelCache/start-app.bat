@echo off
echo 正在啟動多級緩存應用程序...
echo.

REM 檢查端口 8080 是否可用
netstat -an | findstr ":8080" >nul
if %errorlevel% equ 0 (
    echo 端口 8080 已被佔用，嘗試使用端口 8083...
    set PORT=8083
) else (
    echo 使用默認端口 8080...
    set PORT=8080
)

echo 啟動應用程序在端口 %PORT%...
java -jar target/MultiLevelCache-2.0.0.jar --server.port=%PORT%

pause 