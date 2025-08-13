@echo off
REM Netty Cluster Client 启动脚本

echo Starting Netty Cluster Client...

REM 检查Java是否安装
java -version >nul 2>&1
if errorlevel 1 (
    echo Error: Java is not installed or not in PATH
    pause
    exit /b 1
)

REM 检查Maven是否安装
mvn -version >nul 2>&1
if errorlevel 1 (
    echo Error: Maven is not installed or not in PATH
    pause
    exit /b 1
)

REM 编译项目
echo Compiling project...
call mvn clean compile

if errorlevel 1 (
    echo Error: Compilation failed
    pause
    exit /b 1
)

REM 获取服务器地址和端口参数
set HOST=%1
if "%HOST%"=="" set HOST=localhost

set PORT=%2
if "%PORT%"=="" set PORT=8080

echo Connecting to server %HOST%:%PORT%...

REM 启动客户端
call mvn exec:java -Dexec.mainClass="com.example.nettycluster.ClusterClient" -Dexec.args="%HOST% %PORT%"

pause
