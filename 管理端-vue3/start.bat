@echo off
echo 正在启动智慧党建管理端...
echo.

REM 检查Node.js是否安装
node --version >nul 2>&1
if errorlevel 1 (
    echo 错误：未检测到Node.js，请先安装Node.js
    pause
    exit /b 1
)

REM 检查npm是否安装
npm --version >nul 2>&1
if errorlevel 1 (
    echo 错误：未检测到npm，请先安装npm
    pause
    exit /b 1
)

echo 检测到Node.js和npm，正在安装依赖...
npm install

if errorlevel 1 (
    echo 错误：依赖安装失败
    pause
    exit /b 1
)

echo 依赖安装完成，正在启动开发服务器...
echo.
echo 项目将在浏览器中自动打开
echo 如果没有自动打开，请手动访问：http://localhost:3000
echo.
echo 按 Ctrl+C 停止服务器
echo.

npm run dev

pause
