# Docker 部署文件说明

本文档说明 Apex 平台 Docker 部署相关的各个文件的作用和配置。

## 文件清单

### 核心配置文件

| 文件 | 说明 |
|------|------|
| `docker-compose.yml` | 开发环境 Docker Compose 配置文件 |
| `docker-compose.prod.yml` | 生产环境 Docker Compose 配置文件 |
| `.dockerignore` | Docker 构建忽略文件配置 |
| `.env.example` | 环境变量配置模板 |
| `docker-manager.sh` | Docker 管理脚本（便捷操作） |

### 后端相关文件

| 文件 | 说明 |
|------|------|
| `apex-platform/Dockerfile` | 后端应用 Docker 镜像构建文件 |

### 前端相关文件

| 文件 | 说明 |
|------|------|
| `apex-web/Dockerfile` | 前端应用 Docker 镜像构建文件 |
| `apex-web/nginx.conf` | 前端 Nginx 配置文件 |

### 配置文件

| 文件 | 说明 |
|------|------|
| `config/redis.conf` | Redis 开发环境配置文件 |
| `config/redis.prod.conf` | Redis 生产环境配置文件 |

### 数据库脚本

| 文文件 | 说明 |
|-------|------|
| `sql/init/01-init-schema.sql` | 数据库初始化脚本 |

## 文件详细说明

### 1. docker-compose.yml

**用途**: 开发环境 Docker Compose 主配置文件

**包含服务**:
- `postgres`: PostgreSQL 16.x 数据库服务
- `redis`: Redis 7.x 缓存服务
- `apex-platform`: 后端 Spring Boot 应用服务
- `apex-web`: 前端 Nginx 应用服务

**主要配置**:
- 端口映射：PostgreSQL(5432), Redis(6379), 后端(8080), 前端(3000)
- 数据持久化：使用本地数据卷挂载
- 网络隔离：使用自定义网络 apex-network
- 健康检查：所有服务都配置了健康检查
- 日志配置：限制日志大小和文件数量

### 2. docker-compose.prod.yml

**用途**: 生产环境 Docker Compose 配置文件

**与开发环境差异**:
- 使用环境变量配置敏感信息
- 资源限制配置（CPU、内存）
- 更高的连接池配置
- 生产级性能调优参数
- 自动重启策略：always

**环境变量**:
- 数据库连接信息
- Redis 密码
- JWT 密钥
- OSS 配置

### 3. .dockerignore

**用途**: 排除不需要复制到 Docker 镜像中的文件

**排除内容**:
- 依赖目录（node_modules/, target/）
- IDE 配置（.idea/, .vscode/）
- 日志文件（*.log）
- 数据目录（data/）
- 环境变量文件（.env）

**作用**: 
- 减小镜像大小
- 加快构建速度
- 提高安全性

### 4. apex-platform/Dockerfile

**用途**: 后端 Spring Boot 应用 Docker 镜像构建文件

**构建阶段**:
1. **构建阶段（builder）**:
   - 使用 Maven 构建应用
   - 下载依赖
   - 编译打包

2. **运行阶段**:
   - 使用最小化的 JRE 镜像
   - 只包含运行时需要的文件
   - 非 root 用户运行
   - JVM 优化参数

**优化特性**:
- 多阶段构建（减小镜像大小）
- 非 root 用户运行（提高安全性）
- 健康检查配置
- JVM 容器感知优化

### 5. apex-web/Dockerfile

**用途**: 前端应用 Docker 镜像构建文件

**构建阶段**:
1. **依赖安装阶段（deps）**:
   - 使用 npm ci 安装依赖
   - 利用 Docker 缓存层

2. **构建阶段（builder）**:
   - 运行 Vite 构建
   - 生成静态资源

3. **运行阶段（runner）**:
   - 使用 Nginx Alpine 镜像
   - 配置 Nginx 服务器
   - 提供 HTTP 服务

**优化特性**:
- 多阶段构建
- 静态资源优化
- Nginx 配置优化

### 6. apex-web/nginx.conf

**用途**: 前端 Nginx 服务器配置文件

**主要功能**:
- 静态资源服务
- API 请求代理
- 前端路由支持
- Gzip 压缩
- 缓存策略
- 健康检查端点

**配置要点**:
- 监听端口：3000
- API 代理到 apex-platform:8080
- 支持前端路由（SPA）
- 静态资源缓存策略
- 安全头部配置

### 7. config/redis.conf

**用途**: Redis 开发环境配置文件

**主要配置**:
- 网络配置
- 持久化配置（RDB + AOF）
- 内存配置
- 慢查询日志
- 开发环境特定配置

### 8. config/redis.prod.conf

**用途**: Redis 生产环境配置文件

**与开发环境差异**:
- 启用保护模式
- 更大的内存限制
- 禁用危险命令（CONFIG, FLUSHDB, FLUSHALL）
- 更严格的连接限制
- 生产级性能优化

### 9. .env.example

**用途**: 环境变量配置模板

**包含配置**:
- 数据库连接信息
- Redis 配置
- JWT 认证配置
- OSS 配置
- 日志配置
- 监控配置

**使用方法**:
```bash
# 复制模板为实际配置文件
cp .env.example .env

# 编辑 .env 文件，填入实际配置
vim .env
```

### 10. docker-manager.sh

**用途**: Docker 管理脚本，提供便捷的 Docker 操作命令

**可用命令**:
- `start`: 启动所有服务
- `start-dev`: 启动开发环境
- `start-prod`: 启动生产环境
- `stop`: 停止所有服务
- `restart`: 重启所有服务
- `logs`: 查看日志
- `build`: 重新构建服务
- `ps`: 查看服务状态
- `clean`: 清理资源
- `db-shell`: 进入数据库容器
- `redis-shell`: 进入 Redis 容器
- `db-backup`: 备份数据库
- `db-restore`: 恢复数据库

**使用示例**:
```bash
# 启动开发环境
./docker-manager.sh start-dev

# 查看后端日志
./docker-manager.sh logs apex-platform

# 重新构建后端
./docker-manager.sh build apex-platform

# 备份数据库
./docker-manager.sh db-backup
```

### 11. sql/init/01-init-schema.sql

**用途**: 数据库初始化脚本

**主要内容**:
- 创建数据库扩展
- 创建自定义枚举类型
- 创建数据表（users, diseases, targets, drugs 等）
- 创建索引
- 创建触发器（自动更新 updated_at）
- 插入初始数据
- 授予权限

**执行时机**: PostgreSQL 容器首次启动时自动执行

## 快速开始

### 开发环境

```bash
# 启动所有服务
docker-compose up -d

# 或使用管理脚本
./docker-manager.sh start
```

### 生产环境

```bash
# 配置环境变量
cp .env.example .env
vim .env

# 启动生产环境
docker-compose -f docker-compose.prod.yml up -d

# 或使用管理脚本
./docker-manager.sh start-prod
```

## 网络架构

```
┌─────────────────────────────────────────────────────────┐
│                    apex-network (桥接网络)                │
│                      172.28.0.0/16                       │
├─────────────────────────────────────────────────────────┤
│                                                           │
│  ┌──────────────┐    ┌──────────────┐                   │
│  │   PostgreSQL  │    │     Redis    │                   │
│  │  172.28.0.2   │    │  172.28.0.3   │                   │
│  │   端口: 5432  │    │   端口: 6379  │                   │
│  └──────────────┘    └──────────────┘                   │
│         │                     │                            │
│         │                     │                            │
│  ┌──────────────┐                                      │
│  │ apex-platform│                                      │
│  │  172.28.0.4   │                                      │
│  │   端口: 8080  │                                      │
│  └──────────────┘                                      │
│         │                                                 │
│  ┌──────────────┐                                      │
│  │   apex-web   │                                      │
│  │  172.28.0.5   │                                      │
│  │   端口: 3000  │                                      │
│  └──────────────┘                                      │
│                                                           │
└─────────────────────────────────────────────────────────┘
```

## 数据流

```
用户浏览器
    │
    │ HTTP 请求
    ▼
┌──────────────┐
│   apex-web   │ (Nginx)
│   端口: 3000 │
└──────────────┘
    │
    │ /api 请求代理
    ▼
┌──────────────┐
│ apex-platform│ (Spring Boot)
│   端口: 8080 │
└──────────────┘
    │
    ├─────► PostgreSQL (5432)
    │
    └─────► Redis (6379)
```

## 端口映射

| 服务 | 容器端口 | 宿主机端口 | 容器 IP |
|------|----------|-----------|---------|
| PostgreSQL | 5432 | 5432 | 172.28.0.2 |
| Redis | 6379 | 6379 | 172.28.0.3 |
| apex-platform | 8080 | 8080 | 172.28.0.4 |
| apex-platform | 8081 | 8081 | 172.28.0.4 |
| apex-web | 3000 | 3000 | 172.28.0.5 |

## 数据持久化

### PostgreSQL 数据

**容器内路径**: `/var/lib/postgresql/data`
**宿主机路径**: `./data/postgres`

### Redis 数据

**容器内路径**: `/data`
**宿主机路径**: `./data/redis`

## 健康检查

所有服务都配置了健康检查，Docker Compose 会自动管理服务依赖关系。

### PostgreSQL 健康检查
```bash
pg_isready -U apex_user -d apex
```

### Redis 健康检查
```bash
redis-cli ping
```

### apex-platform 健康检查
```bash
curl http://localhost:8080/actuator/health
```

### apex-web 健康检查
```bash
curl http://localhost:3000/health
```

## 日志管理

所有服务都配置了日志轮转策略：

```yaml
logging:
  driver: "json-file"
  options:
    max-size: "50m"    # 单个日志文件最大 50MB
    max-file: "3"      # 保留最近 3 个日志文件
```

**查看日志**:
```bash
# 查看所有服务日志
docker-compose logs -f

# 查看特定服务日志
docker-compose logs -f apex-platform

# 查看最近 100 行
docker-compose logs --tail=100
```

## 资源限制

生产环境配置了资源限制：

| 服务 | CPU 限制 | 内存限制 |
|------|---------|----------|
| postgres | 2 核 | 2GB |
| redis | 1 核 | 1GB |
| apex-platform | 4 核 | 4GB |
| apex-web | 1 核 | 512MB |

## 安全配置

### 网络
- 使用自定义网络隔离
- 只暴露必要的端口
- 内部服务间通过服务名通信

### 用户
- 容器内使用非 root 用户运行
- 最小权限原则

### 数据
- 环境变量管理敏感信息
- .env 文件不提交到版本控制
- 生产环境必须修改默认密码

### 镜像
- 使用官方基础镜像
- 定期更新基础镜像
- 使用镜像扫描工具检查安全漏洞

## 故障排查

### 服务无法启动
```bash
# 查看服务日志
docker-compose logs <service-name>

# 查看详细错误
docker-compose logs --tail=50 <service-name>
```

### 数据库连接问题
```bash
# 测试数据库连接
docker-compose exec postgres psql -U apex_user -d apex

# 检查网络连接
docker-compose exec apex-platform ping postgres
```

### 端口冲突
```bash
# 检查端口占用
lsof -i :5432
lsof -i :6379
lsof -i :8080
lsof -i :3000
```

## 维护建议

1. **定期备份**
   - 使用 docker-manager.sh db-backup 备份数据库
   - 保存备份到安全位置

2. **监控资源使用**
   - 使用 docker stats 监控资源使用情况
   - 定期检查磁盘空间

3. **日志管理**
   - 定期清理旧日志
   - 配置日志集中收集

3
5. **安全更新**
   - 定期更新基础镜像
   - 修复安全漏洞
   - 更新依赖版本

## 扩展阅读

- [Docker Compose 官方文档](https://docs.docker.com/compose/)
- [PostgreSQL Docker 官方镜像](https://hub.docker.com/_/postgres)
- [Redis Docker 官方镜像](https://hub.docker.com/_/redis)
- [Nginx 官方文档](https://nginx.org/en/docs/)
