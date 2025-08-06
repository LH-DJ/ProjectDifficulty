@echo off
echo 正在測試啟動多級緩存應用程序...
echo.

REM 設置類路徑
set CLASSPATH=target/classes

REM 檢查classes目錄是否存在
if not exist "target\classes" (
    echo 錯誤：target\classes 目錄不存在，請先編譯項目
    pause
    exit /b 1
)

echo 使用類路徑: %CLASSPATH%
echo 啟動應用程序...

java -cp "%CLASSPATH%" com.multilevelcache.MultiLevelCacheApplication --spring.profiles.active=local

pause 