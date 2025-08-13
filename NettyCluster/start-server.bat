@echo off
REM Netty Cluster Server 启动脚本

echo Starting Netty Cluster Server...

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

REM 获取端口参数
set PORT=%1
if "%PORT%"=="" set PORT=8080

echo Starting server on port %PORT%...

REM 启动服务器
call mvn exec:java -Dexec.mainClass="com.example.nettycluster.ClusterServer" -Dexec.args="%PORT%"

pause
