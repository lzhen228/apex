# Apex 平台部署说明

> **版本**: v1.0.0 &nbsp;|&nbsp; **最后更新**: 2026-03-30

---

## 目录

- [环境准备](#环境准备)
- [Docker Compose 部署](#docker-compose-部署)
- [手动部署](#手动部署)
- [数据库初始化](#数据库初始化)
- [环境变量配置](#环境变量配置)
- [Nginx 配置](#nginx-配置)
- [日志管理](#日志管理)
- [监控配置](#监控配置)
- [备份与恢复](#备份与恢复)

)

---

## 环境准备

### JDK 21 安装

```bash
# Ubuntu/Debian
sudo apt update
sudo apt install -y openjdk-21-jdk

# CentOS/RHEL
sudo yum install -y java-21-openjdk-devel

# macOS
brew install openjdk@21

# 验证安装
java -version
```

### Node.js 20 安装

```bash
# 使用 nvm（推荐）
curl -o- https://raw.githubusercontent.com/nvm-sh/nvm/v0.39.0/install.sh | bash
source ~/.bashrc
nvm install 20
nvm use 20

# Ubuntu/Debian
curl -fsSL https://deb.nodesource.com/setup_20.x | sudo -E bash -
sudo apt install -y nodejs

# 验证安装
node -v
npm -v
```

### PostgreSQL 16 安装

```bash
# Ubuntu/Debian
sudo apt install -y postgresql-16 postgresql-client-16

# CentOS/RHEL
sudo yum install -y postgresql16-server postgresql16

# 初始化数据库（仅首次安装）
sudo -u postgres initdb -D /var/lib/pgsql/data

# 启动服务
sudo systemctl start postgresql
sudo systemctl enable postgresql

# 创建数据库和用户
sudo -u postgres psql <<EOF
CREATE USER apex_user WITH PASSWORD 'your_password';
CREATE DATABASE apex OWNER WITH TEMPLATE template0 ENCODING 'UTF8';
GRANT ALL PRIVILEGES ON DATABASE apex TO apex_user;
EOF
```

### Redis 7 安装

```bash
# Ubuntu/Debian
sudo apt install -y redis-server

# CentOS/RHEL
sudo yum install -y redis

# macOS
brew install redis

# 启动服务
sudo systemctl start redis
sudo systemctl enable redis

# 验证安装
redis-cli ping
```

---

## Docker Compose 部署

### 开发环境启动

```bash
# 克隆项目
git clone <repository-url>
cd apex

# 复制环境变量模板
cp .env.example .env

# 编辑 .env 文件，配置必要的环境变量
vim .env

# 启动所有服务
docker-compose up -d

# 查看服务状态
docker-compose ps

# 查看日志
docker-compose logs -f
```

### 生产环境启动

```bash
# 使用生产配置
docker-compose -f docker-compose.prod.yml up -d

# 查看资源使用情况
docker stats

# 停止服务
docker-compose down

# 停止并删除数据卷（谨慎操作）
docker-compose down -v
```

### 配置文件说明

```yaml
# docker-compose.yml
version: '3.8'

services:
  postgres:
    image: postgres:16-alpine
    container_name: apex-postgres
    environment:
      POSTGRES_DB: apex
      POSTGRES_USER: apex_user
      POSTGRES_PASSWORD: ${DB_PASSWORD}
    volumes:
      - postgres_data:/var/lib/postgresql/data
      - ./sql/init.sql:/docker-entrypoint-initdb.d/init.sql
    ports:
      - "5432:5432"
    networks:
      - apex-network

  redis:
    image: redis:7-alpine
    container_name: apex-redis
    command: redis-server --requirepass ${REDIS_PASSWORD}
    volumes:
      - redis_data:/data
    ports:
      - "6379:6379"
    networks:
      - apex-network

  backend:
    image: harbourbiomed/apex-platform:1.0.0
    container_name: apex-backend
    build:
      context: ./apex-platform
      dockerfile: Dockerfile
    environment:
      SPRING_PROFILES_ACTIVE: prod
      DB_URL: jdbc:postgresql://postgres:5432/apex
      DB_PASSWORD: ${DB_PASSWORD}
      REDIS_HOST: redis
      REDIS_PASSWORD: ${REDIS_PASSWORD}
      OSS_ENDPOINT: ${OSS_ENDPOINT}
      OSS_ACCESS_KEY_ID: ${OSS_ACCESS_KEY_ID}
      OSS_ACCESS_KEY_SECRET: ${OSS_ACCESS_KEY_SECRET}
      OSS_BUCKET_NAME: ${OSS_BUCKET_NAME}
      JWT_SECRET: ${JWT_SECRET}
    depends_on:
      - postgres
      - redis
    ports:
      - "8080:8080"
    networks:
      - apex-network

  frontend:
    image: harbourbiomed/apex-web:1.0.0
    container_name: apex-frontend
    build:
      context: ./apex-web
      dockerfile: Dockerfile
    depends_on:
      - backend
    ports:
      - "80:80"
    networks:
      - apex-network

volumes:
  postgres_data:
  redis_data:

networks:
  apex-network:
    driver: bridge
```

---

## 手动部署

### 后端部署

```bash
# 1. 进入后端目录
cd apex-platform

# 2. 打包
./mvnw clean package -DskipTests

# 3. 上传 JAR 包到服务器
scp target/apex-platform-1.0.0.jar user@server:/opt/apex/

# 4. 在服务器上创建启动脚本
cat > /opt/apex/start.sh <<'EOF'
#!/bin/bash
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk
export SPRING_PROFILES_ACTIVE=prod
export DB_URL=jdbc:postgresql://localhost:5432/apex
export DB_PASSWORD=your_password
export REDIS_HOST=localhost
export REDIS_PASSWORD=your_redis_password
export OSS_ENDPOINT=your_oss_endpoint
export OSS_ACCESS_KEY_ID=your_access_key
export OSS_ACCESS_KEY_SECRET=your_secret_key
export OSS_BUCKET_NAME=your_bucket
export JWT_SECRET=your_jwt_secret

nohup java -jar /opt/apex/apex-platform-1.0.0.jar > /opt/apex/logs/app.log 2>&1 &
echo $! > /opt/apex/app.pid
EOF

# 5. 启动服务
chmod +x /opt/apex/start.sh
/opt/apex/start.sh

# 6. 使用 systemd 管理（推荐）
sudo tee /etc/systemd/system/apex-backend.service <<'EOF'
[Unit]
Description=Apex Backend Service
After=network.target postgresql.service redis.service

[Service]
Type=simple
User=apex
WorkingDirectory=/opt/apex
ExecStart=/usr/lib/jvm/java-21-openjdk/bin/java -jar /opt/apex/apex-platform-1.0.0.jar
EnvironmentFile=/opt/apex/.env
Restart=on-failure
RestartSec=10

[Install]
WantedBy=multi-user.target
EOF

sudo systemctl daemon-reload
sudo systemctl enable apex-backend
sudo systemctl start apex-backend
```

### 前端部署

```bash
# 1. 进入前端目录
cd apex-web

# 2. 构建
npm run build

# 3. 上传构建产物
scp -r dist user@server:/var/www/apex/

# 4. 配置 Nginx
sudo tee /etc/nginx/sites-available/apex <<'EOF'
server {
    listen 80;
    server_name apex.harbourbiomed.com;
    
    root /var/www/apex;
    index index.html;
    
    # Gzip 压缩
    gzip on;
    gzip_types text/plain text/css application/json application/javascript text/xml;
    
    # SPA 路由
    location / {
        try_files $uri $uri/ /index.html;
    }
    
    # API 反向代理
    location /api/ {
        proxy_pass http://localhost:8080/api/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
    
    # 静态资源缓存
    location ~* \.(js|css|png|jpg|jpeg|gif|ico|svg|woff2)$ {
        expires 7d;
        add_header Cache-Control "public, immutable";
    }
}
EOF

# 5. 启用站点
sudo ln -s /etc/nginx/sites-available/apex /etc/nginx/sites-enabled/
sudo nginx -t
sudo systemctl reload nginx
```

---

## 数据库初始化

### 执行 SQL 脚本

```bash
# 1. 初始化数据库结构
psql -h localhost -U apex_user -d apex -f sql/init.sql

# 2. 执行迁移脚本
psql -h localhost -U apex_user -d apex -f sql/migrations/V1.0.0__create_tables.sql
psql -h localhost -U apex_user -d apex -f sql/migrations/V1.0.1__create_indexes.sql
psql -h localhost -U apex_user -d apex -f sql/migrations/V1.0.2__create_materialized_views.sql
```

### 初始数据导入

```bash
# 导入测试数据（可选）
psql -h localhost -U apex_user -d apex -f sql/seed_data.sql

# 或通过后端同步任务初始化
# 访问 XXL-Job Admin: http://localhost:8080/xxl-job-admin
# 手动执行数据同步任务
```

---

## 环境变量配置

### 后端环境变量

```bash
# .env
# Spring 配置
SPRING_PROFILES_ACTIVE=prod
SERVER_PORT=8080

# 数据库配置
DB_URL=jdbc:postgresql://localhost:5432/apex
DB_USER=apex_user
DB_PASSWORD=your_password
DB_POOL_SIZE=20

# Redis 配置
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=your_redis_password
REDIS_DATABASE=0

# OSS 配置
OSS_ENDPOINT=oss-cn-shanghai.aliyuncs.com
OSS_ACCESS_KEY_ID=your_access_key_id
OSS_ACCESS_KEY_SECRET=your_access_key_secret
OSS_BUCKET_NAME=pharmcube-data

# JWT 配置
JWT_SECRET=your-jwt-secret-key-change-in-production
JWT_ACCESS_EXPIRATION=7200000
JWT_REFRESH_EXPIRATION=604800000

# XXL-Job 配置
XXL_JOB_ADMIN_ADDRESSES=http://localhost:8080/xxl-job-admin
XXL_JOB_EXECUTOR_PORT=9999
```

### 前端环境变量

```bash
# .env.production
VITE_API_BASE_URL=https://api.harbourbiomed.com/api/v1
VITE_APP_TITLE=Apex 早期靶点情报分析智能体
VITE_DEV_TOOLS=false
VITE_MATRIX_PAGE_SIZE=100
VITE_PIPELINE_PAGE_SIZE=50
```

---

## Nginx 配置

### 生产环境完整配置

```nginx
# /etc/nginx/nginx.conf
user nginx;
worker_processes auto;
error_log /var/log/nginx/error.log warn;
pid /var/run/nginx.pid;

events {
    worker_connections 1024;
    use epoll;
}

http {
    include /etc/nginx/mime.types;
    default_type application/octet-stream;
    
    # 日志格式
    log_format main '$remote_addr - $remote_user [$time_local] "$request" '
                    '$status $body_bytes_sent "$http_referer" '
                    '"$http_user_agent" "$http_x_forwarded_for"';
    
    access_log /var/log/nginx/access.log main;
    
    # 性能优化
    sendfile on;
    tcp_nopush on;
    tcp_nodelay on;
    keepalive_timeout 65;
    types_hash_max_size 2048;
    client_max_body_size 100M;
    
    # Gzip 压缩
    gzip on;
    gzip_vary on;
    gzip_proxied any;
    gzip_comp_level 6;
    gzip_types text/plain text/css text/xml text/javascript 
               application/json application/javascript application/xml+rss 
               application/rss+xml font/truetype font/opentype 
               application/vnd.ms-fontobject image/svg+xml;
    
    # 上游服务器
    upstream apex_backend {
        least_conn;
        server localhost:8080 max_fails=3 fail_timeout=30s;
        keepalive 32;
    }
    
    # HTTP 服务器
    server {
        listen 80;
        server_name apex.harbourbiomed.com;
        
        # 强制 HTTPS
        return 301 https://$server_name$request_uri;
    }
    
    # HTTPS 服务器
    server {
        listen 443 ssl http2;
        server_name apex.harbourbiomed.com;
        
        # SSL 证书
        ssl_certificate /etc/ssl/certs/apex.crt;
        ssl_certificate_key /etc/ssl/private/apex.key;
        ssl_protocols TLSv1.2 TLSv1.3;
        ssl_ciphers HIGH:!aNULL:!MD5;
        ssl_prefer_server_ciphers on;
        ssl_session_cache shared:SSL:10m;
        ssl_session_timeout 10m;
        
        # 安全头
        add_header X-Frame-Options "SAMEORIGIN" always;
        add_header X-Content-Type-Options "nosniff" always;
        add_header X-XSS-Protection "1; mode=block" always;
        add_header Strict-Transport-Security "max-age=31536000; includeSubDomains" always;
        
        # 静态文件
        root /var/www/apex;
        index index.html;
        
        # SPA 路由
        location / {
            try_files $uri $uri/ /index.html;
        }
        
        # API 反向代理
        location /api/ {
            proxy_pass http://apex_backend;
            proxy_http_version 1.1;
            proxy_set_header Connection "";
            proxy_set_header Host $host;
            proxy_set_header X-Real-IP $remote_addr;
            proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
            proxy_set_header X-Forwarded-Proto $scheme;
            
            # 超时配置
            proxy_connect_timeout 60s;
            proxy_send_timeout 60s;
            proxy_read_timeout 60s;
        }
        
        # 静态资源缓存
        location ~* \.(js|css|png|jpg|jpeg|gif|ico|svg|woff2|ttf)$ {
            expires 7d;
            add_header Cache-Control "public, immutable";
            access_log off;
        }
        
        # 禁止访问隐藏文件
        location ~ /\. {
            deny all;
            access_log off;
            log_not_found off;
        }
    }
}
```

---

## 日志管理

### 后端日志配置

```yaml
# logback-spring.xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
    <property name="LOG_PATH" value="/opt/apex/logs"/>
    
    <!-- 控制台日志 -->
    <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>
    
    <!-- 文件日志 -->
    <appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>${LOG_PATH}/apex.log</file>
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <fileNamePattern>${LOG_PATH}/apex-%d{yyyy-MM-dd}.log.gz</fileNamePattern>
            <maxHistory>30</maxHistory>
            <totalSizeCap>10GB</totalSizeCap>
        </rollingPolicy>
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>
    
    <!-- 错误日志 -->
    <appender name="ERROR_FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>${LOG_PATH}/apex-error.log</file>
        <filter class="ch.qos.logback.classic.filter.LevelFilter">
            <level>ERROR</level>
            <onMatch>ACCEPT</onMatch>
            <onMismatch>DENY</onMismatch>
        </filter>
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <fileNamePattern>${LOG_PATH}/apex-error-%d{yyyy-MM-dd}.log.gz</fileNamePattern>
            <maxHistory>90</maxHistory>
        </rollingPolicy>
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>
    
    <root level="INFO">
        <appender-ref ref="CONSOLE"/>
        <appender-ref ref="FILE"/>
        <appender-ref ref="ERROR_FILE"/>
    </root>
</configuration>
```

### 日志轮转

```bash
# /etc/logrotate.d/apex-backend
/opt/apex/logs/*.log {
    daily
    rotate 30
    compress
    delaycompress
    missingok
    notifempty
    create 0640 apex apex
    sharedscripts
    postrotate
        systemctl reload apex-backend > /dev/null 2>&1 || true
    endscript
}
```

---

## 监控配置

### Prometheus + Grafana

```yaml
# prometheus.yml
global:
  scrape_interval: 15s
  evaluation_interval: 15s

scrape_configs:
  - job_name: 'apex-backend'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['localhost:8080']
    relabel_configs:
      - source_labels: [__address__]
        target_label: instance
        replacement: 'apex-backend'

  - job_name: 'postgres'
    static_configs:
      - targets: ['localhost:9187']

  - job_name: 'redis'
    static_configs:
      - targets: ['localhost:9121']
```

### 健康检查

```bash
# 后端健康检查
curl http://localhost:8080/actuator/health

# 预期响应
{"status":"UP"}
```

---

## 备份与恢复

### 数据库备份

```bash
# 创建备份脚本
cat > /opt/scripts/backup.sh <<'EOF'
#!/bin/bash
BACKUP_DIR="/opt/backups/postgres"
DATE=$(date +%Y%m%d_%H%M%S)
BACKUP_FILE="${BACKUP_DIR}/apex_${DATE}.dump"

mkdir -p $BACKUP_DIR

# 执行备份
PGPASSWORD=$DB_PASSWORD pg_dump \
  -h localhost \
  -U apex_user \
  -F c \
  -f $BACKUP_FILE \
  apex

# 压缩
gzip $BACKUP_FILE

# 删除 30 天前的备份
find $BACKUP_DIR -name "*.dump.gz" -mtime +30 -delete

echo "Backup completed: ${BACKUP_FILE}.gz"
EOF

chmod +x /opt/scripts/backup.sh

# 添加到 crontab（每天凌晨 2 点）
crontab -e
# 0 2 * * * /opt/scripts/backup.sh >> /opt/scripts/backup.log 2>&1
```

### 数据库恢复

```bash
# 从备份恢复
PGPASSWORD=$DB_PASSWORD pg_restore \
  -h localhost \
  -U apex_user \
  -d apex \
  /opt/backups/postgres/apex_20260330_020000.dump.gz
```

### Redis 持久化

```bash
# 手动保存 Redis 数据
redis-cli -a $REDIS_PASSWORD BGSAVE

# 确认保存位置
redis-cli -a $REDIS_PASSWORD CONFIG GET dir
redis-cli -a $REDIS_PASSWORD CONFIG GET dbfilename
```

---

**和铂医药（Harbour BioMed）** © 2026
