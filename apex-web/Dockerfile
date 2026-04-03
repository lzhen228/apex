# Apex 平台前端 Dockerfile
# 使用多阶段构建优化镜像大小

# ============================================
# 阶段 1：依赖安装阶段
# ============================================
FROM node:20-alpine AS deps

WORKDIR /app

RUN corepack enable

COPY package.json pnpm-lock.yaml ./
RUN pnpm install --frozen-lockfile --prefer-offline

# ============================================
# 阶段 2：构建阶段
# ============================================
FROM node:20-alpine AS builder

WORKDIR /app

RUN corepack enable

# 接收构建时环境变量
ARG VITE_FEISHU_APP_ID
ARG VITE_FEISHU_REDIRECT_URI
ENV VITE_FEISHU_APP_ID=$VITE_FEISHU_APP_ID
ENV VITE_FEISHU_REDIRECT_URI=$VITE_FEISHU_REDIRECT_URI

COPY . .
COPY --from=deps /app/node_modules ./node_modules

RUN pnpm run build

# ============================================
# 阶段 3：Nginx 运行阶段
# ============================================
FROM nginx:1.25-alpine AS runner

COPY nginx.conf /etc/nginx/nginx.conf

COPY --from=builder /app/dist /usr/share/nginx/html

RUN mkdir -p /var/log/nginx && \
    chown -R nginx:nginx /var/log/nginx

EXPOSE 3000

HEALTHCHECK --interval=30s --timeout=10s --start-period=10s --retries=3 \
    CMD wget --quiet --tries=1 --spider http://localhost:3000/health || exit 1

CMD ["nginx", "-g", "daemon off;"]
