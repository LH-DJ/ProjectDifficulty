@echo off
echo 正在啟動多級緩存應用程序（本地模式）...
echo 使用本地配置，禁用 Redis 連接
echo.

java -jar target/MultiLevelCache-2.0.0.jar --spring.profiles.active=local

pause 