@echo off
chcp 65001 >nul
echo ========================================
echo 🧪 多級緩存功能測試腳本
echo ========================================

echo.
echo 📋 測試項目:
echo 1. 緩存健康檢查
echo 2. 緩存統計信息
echo 3. 緩存操作測試
echo 4. 性能測試
echo.

REM 等待應用啟動
echo ⏳ 等待應用啟動...
timeout /t 5 /nobreak >nul

echo.
echo 🔍 1. 緩存健康檢查
curl -s http://localhost:8080/api/cache/health
echo.

echo.
echo 📊 2. 緩存統計信息
curl -s http://localhost:8080/api/cache/stats
echo.

echo.
echo 🧪 3. 緩存操作測試
echo 存入測試數據...
curl -s -X POST http://localhost:8080/api/cache/testCache/testKey -H "Content-Type: application/json" -d "{\"message\":\"Hello Cache!\"}"
echo.

echo 讀取測試數據...
curl -s http://localhost:8080/api/cache/testCache/testKey
echo.

echo 檢查緩存項是否存在...
curl -s http://localhost:8080/api/cache/testCache/testKey/exists
echo.

echo 刪除測試數據...
curl -s -X DELETE http://localhost:8080/api/cache/testCache/testKey
echo.

echo.
echo ⚡ 4. 性能測試
echo 交易查詢性能測試...
curl -s http://localhost:8080/api/performance/test/transaction/100
echo.

echo 黑名單查詢性能測試...
curl -s http://localhost:8080/api/performance/test/blacklist/100
echo.

echo.
echo 🧪 5. LocalDateTime 序列化測試
echo 測試 LocalDateTime 序列化...
curl -s http://localhost:8080/api/test/datetime
echo.

echo 測試 JSON 序列化...
curl -s -X POST http://localhost:8080/api/test/json -H "Content-Type: application/json" -d "{\"testTime\":\"2024-01-01T12:00:00\",\"message\":\"測試 LocalDateTime\"}"
echo.

echo.
echo ✅ 測試完成！
echo 📊 查看詳細結果請訪問: http://localhost:8080
echo.

pause 