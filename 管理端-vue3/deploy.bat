@echo off
chcp 65001
echo 🚀 智慧党建管理端部署脚本
echo ================================

echo 📦 正在构建项目...
call npm run build

if %errorlevel% neq 0 (
    echo ❌ 构建失败！
    pause
    exit /b 1
)

echo ✅ 构建成功！
echo 📁 构建文件位于 dist 目录
echo.
echo 📋 部署步骤：
echo 1. 将 dist 目录中的所有文件复制到 Nginx 的网站目录
echo 2. 配置 Nginx 服务器
echo 3. 重启 Nginx 服务
echo.
echo 💡 提示：请参考 deploy.md 文件获取详细的部署说明
echo.
pause
