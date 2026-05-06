# 🚀 Vue3项目部署到Nginx完整指南

## 📋 部署步骤

### 1. 构建生产版本

首先，在项目根目录下构建生产版本：

```bash
# 安装依赖（如果还没安装）
npm install

# 构建生产版本
npm run build
```

构建完成后，会在 `dist` 目录下生成静态文件。

### 2. 准备Nginx环境

#### 2.1 安装Nginx

**Windows:**
```bash
# 使用Chocolatey
choco install nginx

# 或下载安装包：https://nginx.org/en/download.html
```

**Linux (Ubuntu/Debian):**
```bash
sudo apt update
sudo apt install nginx
```

**CentOS/RHEL:**
```bash
sudo yum install nginx
# 或
sudo dnf install nginx
```

#### 2.2 启动Nginx服务

```bash
# Windows
nginx

# Linux
sudo systemctl start nginx
sudo systemctl enable nginx
```

### 3. 配置Nginx

#### 3.1 创建网站目录

```bash
# 创建网站目录
sudo mkdir -p /var/www/smart-party-admin

# 复制构建文件
sudo cp -r dist/* /var/www/smart-party-admin/

# 设置权限
sudo chown -R www-data:www-data /var/www/smart-party-admin
sudo chmod -R 755 /var/www/smart-party-admin
```

#### 3.2 创建Nginx配置文件

创建配置文件 `/etc/nginx/sites-available/smart-party-admin`：

```nginx
server {
    listen 80;
    server_name your-domain.com;  # 替换为您的域名或IP
    
    root /var/www/smart-party-admin;
    index index.html;
    
    # 启用gzip压缩
    gzip on;
    gzip_vary on;
    gzip_min_length 1024;
    gzip_types text/plain text/css text/xml text/javascript application/javascript application/xml+rss application/json;
    
    # 静态资源缓存
    location ~* \.(js|css|png|jpg|jpeg|gif|ico|svg)$ {
        expires 1y;
        add_header Cache-Control "public, immutable";
    }
    
    # 处理Vue Router的history模式
    location / {
        try_files $uri $uri/ /index.html;
    }
    
    # API代理（如果需要）
    location /api/ {
        proxy_pass http://localhost:8080/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
    
    # 安全头
    add_header X-Frame-Options "SAMEORIGIN" always;
    add_header X-XSS-Protection "1; mode=block" always;
    add_header X-Content-Type-Options "nosniff" always;
    add_header Referrer-Policy "no-referrer-when-downgrade" always;
    add_header Content-Security-Policy "default-src 'self' http: https: data: blob: 'unsafe-inline'" always;
}
```

#### 3.3 启用站点配置

```bash
# 创建软链接
sudo ln -s /etc/nginx/sites-available/smart-party-admin /etc/nginx/sites-enabled/

# 测试配置
sudo nginx -t

# 重新加载Nginx
sudo systemctl reload nginx
```

### 4. HTTPS配置（推荐）

#### 4.1 使用Let's Encrypt免费SSL证书

```bash
# 安装Certbot
sudo apt install certbot python3-certbot-nginx

# 获取SSL证书
sudo certbot --nginx -d your-domain.com

# 自动续期
sudo crontab -e
# 添加：0 12 * * * /usr/bin/certbot renew --quiet
```

#### 4.2 手动配置HTTPS

更新Nginx配置文件：

```nginx
server {
    listen 80;
    server_name your-domain.com;
    return 301 https://$server_name$request_uri;
}

server {
    listen 443 ssl http2;
    server_name your-domain.com;
    
    ssl_certificate /path/to/your/certificate.crt;
    ssl_certificate_key /path/to/your/private.key;
    
    # SSL配置
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers ECDHE-RSA-AES128-GCM-SHA256:ECDHE-RSA-AES256-GCM-SHA384;
    ssl_prefer_server_ciphers off;
    
    root /var/www/smart-party-admin;
    index index.html;
    
    # 其他配置同上...
}
```

### 5. 部署脚本

创建自动化部署脚本 `deploy.sh`：

```bash
#!/bin/bash

echo "🚀 开始部署智慧党建管理端..."

# 构建项目
echo "📦 构建项目..."
npm run build

if [ $? -ne 0 ]; then
    echo "❌ 构建失败！"
    exit 1
fi

# 备份当前版本
echo "💾 备份当前版本..."
sudo cp -r /var/www/smart-party-admin /var/www/smart-party-admin.backup.$(date +%Y%m%d_%H%M%S)

# 部署新版本
echo "📤 部署新版本..."
sudo rm -rf /var/www/smart-party-admin/*
sudo cp -r dist/* /var/www/smart-party-admin/

# 设置权限
echo "🔐 设置权限..."
sudo chown -R www-data:www-data /var/www/smart-party-admin
sudo chmod -R 755 /var/www/smart-party-admin

# 测试Nginx配置
echo "🔍 测试Nginx配置..."
sudo nginx -t

if [ $? -eq 0 ]; then
    # 重新加载Nginx
    echo "🔄 重新加载Nginx..."
    sudo systemctl reload nginx
    echo "✅ 部署成功！"
else
    echo "❌ Nginx配置测试失败！"
    exit 1
fi
```

给脚本执行权限：
```bash
chmod +x deploy.sh
```

### 6. 环境变量配置

创建生产环境配置文件 `.env.production`：

```env
# 生产环境配置
VITE_API_BASE_URL=https://your-api-domain.com/api
VITE_APP_TITLE=智慧党建管理端
VITE_APP_VERSION=1.0.0
```

### 7. 监控和日志

#### 7.1 配置日志

在Nginx配置中添加日志：

```nginx
access_log /var/log/nginx/smart-party-admin.access.log;
error_log /var/log/nginx/smart-party-admin.error.log;
```

#### 7.2 监控脚本

创建监控脚本 `monitor.sh`：

```bash
#!/bin/bash

# 检查Nginx状态
if ! systemctl is-active --quiet nginx; then
    echo "❌ Nginx服务未运行，正在重启..."
    sudo systemctl restart nginx
fi

# 检查网站可访问性
if ! curl -f http://localhost > /dev/null 2>&1; then
    echo "❌ 网站无法访问，请检查配置"
fi
```

### 8. 常见问题解决

#### 8.1 404错误
- 检查Vue Router的history模式配置
- 确保Nginx配置中有 `try_files $uri $uri/ /index.html;`

#### 8.2 静态资源加载失败
- 检查文件权限
- 确认文件路径正确
- 检查Nginx配置中的静态文件处理

#### 8.3 API请求失败
- 检查代理配置
- 确认后端服务运行正常
- 检查CORS配置

### 9. 性能优化

#### 9.1 启用缓存
```nginx
# 静态资源缓存
location ~* \.(js|css|png|jpg|jpeg|gif|ico|svg)$ {
    expires 1y;
    add_header Cache-Control "public, immutable";
}
```

#### 9.2 启用压缩
```nginx
gzip on;
gzip_vary on;
gzip_min_length 1024;
gzip_types text/plain text/css text/xml text/javascript application/javascript application/xml+rss application/json;
```

### 10. 安全配置

#### 10.1 隐藏Nginx版本
```nginx
server_tokens off;
```

#### 10.2 添加安全头
```nginx
add_header X-Frame-Options "SAMEORIGIN" always;
add_header X-XSS-Protection "1; mode=block" always;
add_header X-Content-Type-Options "nosniff" always;
```

## 🎉 部署完成！

现在您的Vue3项目已经成功部署到Nginx上。访问您的域名即可看到智慧党建管理端。

### 快速测试命令

```bash
# 测试网站可访问性
curl -I http://your-domain.com

# 检查Nginx状态
sudo systemctl status nginx

# 查看错误日志
sudo tail -f /var/log/nginx/smart-party-admin.error.log
```
