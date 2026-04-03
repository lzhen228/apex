# Apex 平台前端 Dockerfile
# 多阶段构建：builder(node:20-alpine) → runner(nginx:alpine)

# ============================================
# 阶段 1：安装依赖 + 构建
# ============================================
FROM node:20-alpine AS builder

WORKDIR /app

RUN corepack enable

# 单独复制依赖描述文件，利用 Docker 层缓存：
# 只要 package.json / pnpm-lock.yaml 不变，pnpm install 就不会重跑
COPY package.json pnpm-lock.yaml ./
RUN pnpm install --frozen-lockfile --prefer-offline

# 接收构建时环境变量
ARG VITE_FEISHU_APP_ID
ARG VITE_FEISHU_REDIRECT_URI
ENV VITE_FEISHU_APP_ID=$VITE_FEISHU_APP_ID
ENV VITE_FEISHU_REDIRECT_URI=$VITE_FEISHU_REDIRECT_URI

COPY . .
RUN pnpm run build \
    && rm -rf node_modules   # 构建完成后立即删除 node_modules，缩小中间层

# ============================================
# 阶段 2：Nginx 运行阶段（最终镜像）
# ============================================
FROM nginx:alpine AS runner

COPY nginx.conf /etc/nginx/nginx.conf
COPY --from=builder /app/dist /usr/share/nginx/html

RUN mkdir -p /var/log/nginx && \
    chown -R nginx:nginx /var/log/nginx

EXPOSE 3000

HEALTHCHECK --interval=30s --timeout=10s --start-period=10s --retries=3 \
    CMD wget --quiet --tries=1 --spider http://localhost:3000/health || exit 1

CMD ["nginx", "-g", "daemon off;"]
