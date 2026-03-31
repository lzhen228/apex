# Apex 平台前端 Dockerfile
# 使用多阶段构建优化镜像大小

# ============================================
# 阶段 1：依赖安装阶段
# ============================================
FROM node:20-alpine AS deps

# 设置工作目录
WORKDIR /app

# 设置时区
RUN apk add --no-cache tzdata && \
    cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && \
    echo "Asia/Shanghai" > /etc/timezone

# 复制 package.json 和 package-lock.json
COPY package.json package-lock.json ./

# 安装依赖（使用 npm ci 确保版本一致性）
RUN npm ci --only=production --silent

# ============================================
# 阶段 2：构建阶段
# ============================================
FROM node:20-alpine AS builder

# 设置工作目录
WORKDIR /app

# 设置时区
RUN apk add --no-cache tzdata && \
    cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && \
    echo "Asia/Shanghai" > /etc/timezone

# 复制所有源码
COPY . .

# 复制依赖从 deps 阶段
COPY --from=deps /app/node_modules ./node_modules

# 构建应用
RUN npm run build

# ============================================
# 阶段 3：Nginx 运行阶段
# ============================================
FROM nginx:1.25-alpine AS runner

# 设置时区
RUN apk add --no-cache tzdata && \
    cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && \
    echo "Asia/Shanghai" > /etc/timezone

# 移除默认的 nginx 配置
RUN rm /etc/nginx/conf.d/default.conf

# 复制自定义 nginx 配置
COPY nginx.conf /etc/nginx/conf.d/default.conf

# 从构建阶段复制构建产物
COPY --from=builder /app/dist /usr/share/nginx/html

# 创建 nginx 日志目录
RUN mkdir -p /var/log/nginx && \
    chown -R nginx:nginx /var/log/nginx

# 暴露端口
EXPOSE 3000

# 健康检查
HEALTHCHECK --interval=30s --timeout=10s --start-period=10s --retries=3 \
    CMD wget --quiet --tries=1 --spider http://localhost:3000/health || exit 1

# 启动 nginx
CMD ["nginx", "-g", "daemon off;"]
