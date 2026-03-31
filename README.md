# Apex 早期靶点情报分析智能体

> **版本**: v1.0.0 &nbsp;|&nbsp; **最后更新**: 2026-03-30

---

## 目录

- [项目简介](#项目简介)
- [技术架构](#技术架构)
- [快速开始](#快速开始)
- [项目结构](#项目结构)
- [开发指南](#开发指南)
- [部署指南](#部署指南)
- [API 文档](#api-文档)
- [常见问题](#常见问题)
- [联系方式](#联系方式)

---

## 项目简介

**Apex 早期靶点情报分析智能体** 是和铂医药（Harbour BioMed）药物早期研发阶段的靶点情报分析平台，提供靶点组合竞争格局和研发进展的系统化分析能力。

### 核心功能模块

| 模块                 | 功能描述                                                                          |
| -------------------- | --------------------------------------------------------------------------------- |
| **靶点组合竞争格局** | 以二维矩阵方式展示靶点 × 疾病的最高研发阶段，支持按治疗领域/疾病/研发阶段多维筛选 |
| **靶点研发进展格局** | 选定单个疾病后，以管线全景图展示该疾病下所有靶点在各研发阶段的药物分布            |
| **数据自动刷新**     | 每日凌晨 5:00 从医药魔方（PharmCube）自动同步最新数据到平台                       |
| **筛选条件预设**     | 支持保存和加载常用筛选条件，提升分析效率                                          |

---

## 技术架构

### 前后端分离架构

```
┌──────────────────┐
│   用户浏览器      │
└────────┬─────────┘
         │ HTTPS
┌────────▼─────────┐
│    Nginx         │
│  (TLS 终止)      │
│  (静态资源)      │
│  (反向代理)      │
└───┬─────────┬───┘
/api/* │         │  /*
┌─────▼─────┐ ┌──▼──────────┐
│ Spring    │ │ React SPA   │
│ Boot      │ │ (静态文件)   │
│ :8080     │ └─────────────┘
└─────┬─────┘
      │
┌─────┼─────────┐
│     │         │
┌▼───┐ ┌▼────┐ ┌▼──────────┐
│Redis│ │PgSQL │ │ Aliyun OSS│
│:6379│ │:5432 │ │ (数据源)   │
└─────┘ └─────┘ └───────────┘
```

### 技术栈

#### 后端

| 技术            | 版本     | 说明                           |
| --------------- | -------- | ------------------------------ |
| Java            | 21 (LTS) | 最新长期支持版本，支持虚拟线程 |
| Spring Boot     | 3.3.x    | 企业级应用框架                 |
| Spring Security | 6.x      | 安全认证与授权                 |
| MyBatis-Plus    | 3.5.x    | 持久层框架                     |
| Apache Parquet  | 1.14.x   | Parquet 文件解析               |
| Aliyun OSS SDK  | 3.17.x   | 阿里云 OSS 对象存储            |
| XXL-Job         | 2.4.x    | 分布式任务调度                 |
| MapStruct       | 1.5.x    | 对象映射                       |
| Knife4j         | 4.x      | (Swagger) API 文档             |

#### 前端

| 技术       | 版本  | 说明         |
| ---------- | ----- | ------------ |
| React      | 19    | UI 框架      |
| TypeScript | 5.x   | 类型安全     |
| Ant Design | 5.x   | UI 组件库    |
| AG Grid    | 31.x  | 表格渲染引擎 |
| ECharts    | 5.x   | 图表库       |
| Vite       | 6.3.5 | 构建工具     |

#### 数据库与中间件

| 技术       | 版本   | 说明                   |
| ---------- | ------ | ---------------------- |
| PostgreSQL | 16.x   | 主数据库               |
| Redis      | 7.x    | 缓存与会话管理         |
| Nginx      | 1.25.x | 反向代理与静态资源服务 |
| Docker     | 24.x   | 容器化部署             |

---

## 快速开始

### 环境要求

- JDK 21+
- Node.js 20+
- PostgreSQL 16.x
- Redis 7.x
- Docker & Docker Compose（可选）

### Docker Compose 启动（推荐）

```bash
# 克隆项目
git clone <repository-url>
cd apex

# 启动所有服务（开发环境）
docker-compose up -d

# 查看服务状态
docker-compose ps

# 查看日志
docker-compose logs -f backend
docker-compose logs -f frontend
```

### 手动启动步骤

```bash
# 1. 启动数据库服务
docker-compose up -d postgres redis

# 2. 初始化数据库
psql -h localhost -U apex -d apex -f sql/init.sql

# 3. 启动后端
cd apex-platform
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# 4. 启动前端
cd apex-web
npm install
npm run dev
```

### 访问应用

| 服务          | 地址                                | 说明         |
| ------------- | ----------------------------------- | ------------ |
| 前端应用      | http://localhost:3000               | React SPA    |
| 后端 API      | http://localhost:8080               | Spring Boot  |
| Swagger 文档  | http://localhost:8080/doc.html      | API 文档     |
| XXL-Job Admin | http://localhost:8080/xxl-job-admin | 任务调度管理 |

---

## 项目结构

```
apex/
├── apex-platform/              # 后端主模块
│   ├── apex-platform-auth/      # 认证模块
│   ├── apex-platform-common/    # 通用基础模块
│   ├── apex-platform-data-sync/ # 数据同步模块
│   ├── apex-platform-competition/ # 竞争格局模块
│   ├── apex-platform-progress/   # 研发进展模块
│   └── apex-platform-filter-preset/ # 筛选预设模块
├── apex-web/                   # 前端应用
│   ├── src/
│   │   ├── components/          # 通用组件
│   │   ├── pages/              # 页面组件
│   │   │   ├── Competition/    # 竞争格局页面
│   │   │   └── Progress/       # 研发进展页面
│   │   ├── services/           # API 服务
│   │   ├── store/              # 状态管理
│   │   └── utils/              # 工具函数
│   ├── public/
│   └── package.json
├── sql/                        # 数据库脚本
│   ├── init.sql                # 初始化脚本
│   └── migrations/             # 迁移脚本
├── docker-compose.yml          # Docker Compose 配置
├── README.md                   # 本文件
├── DEPLOYMENT.md               # 部署说明
├── API.md                      # API 文档
└── DEVELOPMENT.md              # 开发规范
```

---

## 开发指南

### 后端开发

```bash
cd apex-platform

# 运行单元测试
./mvnw test

# 打包
./mvnw clean package

# 跳过测试打包
./mvnw clean package -DskipTests
```

详见 [后端 README.md](./apex-platform/README.md)

### 前端开发

```bash
cd apex-web

# 安装依赖
npm install

# 开发模式（热更新）
npm run dev

# 类型检查
npm run type-check

# 代码检查
npm run lint

# 构建
npm run build

# 预览构建结果
npm run preview
```

详见 [前端 README.md](./apex-web/README.md)

---

## 部署指南

### 开发环境部署

```bash
# 使用 Docker Compose 一键启动
docker-compose -f docker-compose.dev.yml up -d
```

### 生产环境部署

```bash
# 使用生产配置启动
docker-compose -f docker-compose.prod.yml up -d

# 或手动部署
# 详见 DEPLOYMENT.md
```

详细的部署步骤、环境配置、Nginx 配置、监控和备份策略，请参考 [DEPLOYMENT.md](./DEPLOYMENT.md)

---

## API 文档

### Swagger 访问

- 开发环境: http://localhost:8080/doc.html
- 测试环境: http://test-apex.harbourbiomed.com/api/doc.html
- 生产环境: http://ai-harby.harbourbiomed.com/api/doc.html

### 认证机制

所有 API 请求需要在 Header 中携带 JWT Token：

```http
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

### 接口示例

| 接口                             | 方法 | 说明         |
| -------------------------------- | ---- | ------------ |
| `/api/v1/auth/login`             | POST | 用户登录     |
| `/api/v1/diseases/tree`          | GET  | 疾病树形结构 |
| `/api/v1/competition/matrix`     | POST | 矩阵查询     |
| `/api/v1/competition/cell-drugs` | GET  | 格子药品详情 |
| `/api/v1/progress/disease-view`  | POST | 疾病视图查询 |

完整的 API 文档，请参考 [API.md](./API.md)

---

## 常见问题

### Q1: 数据如何同步？

A: 系统每天凌晨 5:30 自动从阿里云 OSS 拉取 Parquet 文件并同步到 PostgreSQL，由 XXL-Job 定时任务调度。

### Q2: 矩阵查询性能如何？

A: 使用物化视图预计算 + GIN 索引 + Redis 二级缓存，P95 延迟 ≤ 800ms。

### Q3: 如何添加新的筛选预设？

A: 在界面上配置筛选条件后，点击"保存预设"，输入预设名称即可。

### Q4: 支持多语言吗？

A: 当前版本支持中文，后续版本计划增加英文支持。
