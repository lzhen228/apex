# Apex 早期靶点情报分析智能体 - 技术方案大纲

## 一、项目背景与目标

### 1.1 项目背景
**为什么这么设计/选型**：
医药研发领域存在大量的靶点竞争情报和研发管线数据，传统检索方式效率低下，无法快速洞察：
- 靶点组合竞争格局（哪些靶点组合已有研发管线）
- 靶点在不同疾病领域的研发进展
- 管线历史事件追踪

医药魔方每天定时推送最新的临床试验和药品管线数据，需要构建智能分析平台，帮助研发人员快速获取决策支持信息。

### 1.2 项目目标
- 构建Web应用，提供靶点情报的快速查询和可视化分析
- 支持靶点组合竞争格局的二维矩阵展示
- 支持靶点研发进展的疾病视图/靶点视图分析
- 支持用户自定义筛选条件并保存查询结果
- 为后续AI助手（百科问答、文档精读等）预留扩展接口

---

## 二、核心模块划分

### 2.1 用户认证模块
**为什么这么设计/选型**：
- 子模块：登录认证、会话管理、权限控制
- 采用标准的JWT Token机制，满足医药行业数据安全需求
- 支持账号密码登录，预留SSO单点登录能力

### 2.2 数据接入模块
**为什么这么设计/选型**：
- 子模块：OSS文件监听、Parquet文件解析、数据清洗、数据入库
- 医药魔方每天5点定时推送Parquet文件到阿里云OSS，需要自动化处理
- 解耦数据获取与业务逻辑，支持数据源扩展

### 2.3 靶点组合竞争格局模块
**为什么这么设计/选型**：
- 子模块：疾病筛选、研发阶段筛选、矩阵计算、结果导出
- 核心算法：计算任意两个靶点在同一疾病下的最高阶段分值
- 二维矩阵可视化，颜色编码快速识别竞争热度
- 支持悬浮查看药品详情（drug_name, company_names, nct_id等）

### 2.4 靶点研发进展格局模块
**为什么这么设计/选型**：
- 子模块：疾病视图、靶点视图（预留）、管线历史视图（预留）
- 疾病视图：展示选定疾病下，各靶点的研发管线分布
- 表格化展示不同研发阶段的药品卡片
- 支持靶点级联筛选、模糊查询、全选操作

### 2.5 查询配置管理模块
**为什么这么设计/选型**：
- 子模块：预设筛选、自定义筛选、筛选保存/加载
- 支持按治疗领域（GI、Dermatology等）预设筛选条件
- 用户可保存常用查询条件，提升复用效率

### 2.6 AI助手模块（预留）
**为什么这么设计/选型**：
- 子模块：百科问答、文档精读、知产报告、序列推荐、医学智能
- 预留智能体接口，未来集成大语言模型能力
- 原型已包含Chatbot FAB按钮

---

## 三、技术栈选型

### 3.1 后端技术栈
**为什么这么设计/选型**：

| 组件 | 选型 | 设计理由 |
|------|------|----------|
| 语言 | Python 3.10+ | 丰富的数据处理库（pandas、pyarrow），医药行业主流选择 |
| Web框架 | FastAPI | 高性能异步框架，自动生成OpenAPI文档，易于集成AI能力 |
| ORM | SQLAlchemy 2.0 | 异步ORM支持，便于数据模型管理 |
| 任务队列 | Celery + Redis | 处理定时任务（OSS文件解析）、异步数据计算 |
| 数据处理 | Pandas + PyArrow | 高效处理Parquet文件，内存优化 |

### 3.2 前端技术栈
**为什么这么设计/选型**：

| 组件 | 选型 | 设计理由 |
|------|------|----------|
| 框架 | Vue 3 + TypeScript | 渐进式框架，适合复杂交互（矩阵悬浮、筛选联动） |
| UI库 | Ant Design Vue | 企业级UI组件库，表格、筛选、弹窗等组件丰富 |
| 图表 | ECharts | 二维矩阵可视化，支持自定义交互 |
| 状态管理 | Pinia | Vue官方推荐，轻量级状态管理 |
| 构建 | Vite | 快速冷启动，HMR性能优异 |

### 3.3 数据库选型
**为什么这么设计/选型**：

| 组件 | 选型 | 设计理由 |
|------|------|----------|
| 主数据库 | PostgreSQL 14+ | 支持JSONB存储靶点数组、全文搜索、复杂查询优化 |
| 缓存 | Redis 7.0 | 热点数据缓存、会话管理、任务队列 |
| 搜索引擎 | Elasticsearch 8.x（可选） | 支持药品名称、疾病名称的模糊搜索，高并发检索 |

### 3.4 中间件选型
**为什么这么设计/选型**：

| 组件 | 选型 | 设计理由 |
|------|------|----------|
| 消息队列 | RabbitMQ | 可靠的任务投递，支持优先级队列 |
| 对象存储 | 阿里云OSS | 原始数据存储，已对接医药魔方数据源 |
| 反向代理 | Nginx | 静态资源服务、负载均衡、SSL终端 |
| 日志 | ELK Stack | 集中日志收集与分析，便于问题排查 |

---

## 四、核心数据模型设计

### 4.1 核心表结构

#### 表1：ci_tracking_info（竞争情报追踪表）
**为什么这么设计/选型**：
- 映射自`pharmcube2harbour_ci_tracking_info_0.parquet`
- 采用JSONB存储`targets`、`company_names`等数组字段，便于查询
- 添加索引优化常用查询（disease_names、phase_revised、drug_id）

```sql
CREATE TABLE ci_tracking_info (
    id BIGSERIAL PRIMARY KEY,
    nct_id VARCHAR(50) UNIQUE,
    highest_trial_id VARCHAR(50),
    drug_id BIGINT,
    drug_name_cn VARCHAR(200),
    all_name_for_search TEXT,
    drug_type_1 VARCHAR(100),
    drug_type_2 VARCHAR(100),
    drug_type_3 VARCHAR(100),
    drug_type_3_alias VARCHAR(100),
    drug_tag VARCHAR(100),
    drug_tag_alias VARCHAR(100),
    targets JSONB,              -- ["IL-4Rα", "TSLP"]
    targets_revised TEXT,
    pharmacological_name VARCHAR(500),
    company_names JSONB,        -- ["Novartis", "Amgen"]
    company_names_revised TEXT,
    group_id VARCHAR(50),
    is_pivotal BOOLEAN,
    phase_revised VARCHAR(50),
    overall_status_cn VARCHAR(100),
    first_posted_date DATE,
    disease_names JSONB,         -- ["Acne", "Psoriasis"]
    indication_desc TEXT,
    target_population TEXT,
    concomitant_drugs TEXT,
    title TEXT,
    start_datetime TIMESTAMP,
    completion_date DATE,
    is_combo BOOLEAN,
    intervention TEXT,
    design_allocation_revised VARCHAR(100),
    intervention_model_revised VARCHAR(100),
    design_primary_purpose VARCHAR(100),
    design_masking_revised VARCHAR(100),
    age_type VARCHAR(50),
    mini_age INT,
    max_age INT,
    gender_revised VARCHAR(50),
    actual_enrollment_global INT,
    anticipated_enrollment_global INT,
    actual_enrollment_cn INT,
    anticipated_enrollment_cn INT,
    experimental_intervention TEXT,
    control_intervention TEXT,
    pri_outcome_measures TEXT,
    sec_outcome_measures TEXT,
    inclusion_criteria TEXT,
    exclusion_criteria TEXT,
    outcome TEXT,
    relative_risk FLOAT,
    data_source VARCHAR(100),
    trial_url VARCHAR(500),
    indication_top_cn_latest_stage VARCHAR(50),
    indication_top_cn_start_date DATE,
    indication_top_global_latest_stage VARCHAR(50),
    indication_top_global_start_date DATE,
    summary TEXT,
    journal VARCHAR(200),
    full_article_link VARCHAR(500),
    pm_id VARCHAR(50),
    key_evidence TEXT,
    record_update_time TIMESTAMP,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

-- 索引
CREATE INDEX idx_ci_tracking_drug_id ON ci_tracking_info(drug_id);
CREATE INDEX idx_ci_tracking_disease_names ON ci_tracking_info USING GIN(disease_names);
CREATE INDEX idx_ci_tracking_targets ON ci_tracking_info USING GIN(targets);
CREATE INDEX idx_ci_tracking_global_stage ON ci_tracking_info(indication_top_global_latest_stage);
```

#### 表2：drug_pipeline_info（药品管线信息表）
**为什么这么设计/选型**：
- 映射自`pharmcube2harbour_drug_pipeline_info_0.parquet`
- 预留用于后续功能，当前阶段主要查询ci_tracking_info表

```sql
CREATE TABLE drug_pipeline_info (
    id BIGSERIAL PRIMARY KEY,
    drug_id BIGINT,
    drug_name_cn VARCHAR(200),
    all_name_for_search TEXT,
    drug_type_1 VARCHAR(100),
    drug_type_2 VARCHAR(100),
    drug_type_3 VARCHAR(100),
    drug_type_3_alias VARCHAR(100),
    drug_tag VARCHAR(100),
    drug_tag_alias VARCHAR(100),
    targets JSONB,
    targets_revised TEXT,
    pharmacological_name VARCHAR(500),
    company_names JSONB,
    company_names_revised TEXT,
    company_names_originator TEXT,
    company_names_with_interest TEXT,
    company_type_1 VARCHAR(100),
    company_type_2 VARCHAR(100),
    diseases JSONB,
    disease_area VARCHAR(100),
    latest_phase VARCHAR(50),
    latest_phase_cn VARCHAR(50),
    latest_phase_us VARCHAR(50),
    latest_phase_update_eu VARCHAR(50),
    latest_phase_jp VARCHAR(50),
    latest_phase_other VARCHAR(50),
    latest_phase_start_date DATE,
    latest_phase_start_date_cn DATE,
    latest_phase_start_date_us DATE,
    latest_phase_start_date_eu DATE,
    latest_phase_start_date_jp DATE,
    latest_phase_start_date_other DATE,
    first_approval_date DATE,
    first_approval_date_cn DATE,
    first_approval_date_us DATE,
    first_approval_date_eu DATE,
    first_approval_date_jp DATE,
    first_approval_date_other DATE,
    first_nda_date DATE,
    first_nda_date_cn DATE,
    first_trial_date DATE,
    is_supplement BOOLEAN,
    regn VARCHAR(50),
    status VARCHAR(50),
    latest_update_time TIMESTAMP,
    latest_update_type VARCHAR(50),
    moa_ranking INT,
    moa_ranking_cn INT,
    deal_num INT,
    total_deal_value FLOAT,
    total_upfront_payment FLOAT,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);
```

#### 表3：users（用户表）
**为什么这么设计/选型**：
- 支持账号密码登录
- 预留角色权限控制

```sql
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    email VARCHAR(100),
    full_name VARCHAR(100),
    role VARCHAR(20) DEFAULT 'user',
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);
```

#### 表4：query_presets（查询预设表）
**为什么这么设计/选型**：
- 保存用户的自定义筛选条件
- 支持系统预设和用户自定义

```sql
CREATE TABLE query_presets (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id),
    name VARCHAR(100) NOT NULL,
    is_system_default BOOLEAN DEFAULT FALSE,
    preset_data JSONB NOT NULL,  -- 存储筛选条件（疾病列表、阶段列表等）
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);
```

### 4.2 阶段分值映射表
**为什么这么设计/选型**：
- 用于靶点组合竞争格局计算分值
- 在代码中配置，便于维护

| 原始值（indication_top_global_latest_stage） | 靶点进展格局显示 | 竞争格局显示 | 分值 |
|---------------------------------------------|------------------|--------------|------|
| 临床前 | PreC | PreClinical | 0.1 |
| 申报临床 | IND | IND | 0.5 |
| I期临床 | Phase 1 | Phase I | 1.0 |
| I/II期临床 | Phase 1 | Phase I/II | 1.5 |
| II期临床 | Phase 2 | Phase II | 2.0 |
| II/III期临床 | Phase 2 | Phase II/III | 2.5 |
| III期临床 | Phase 3 | Phase III | 3.0 |
| 申请上市 | BLA | BLA | 3.5 |
| 批准上市 | Market | Approved | 4.0 |

---

## 五、关键接口设计

### 5.1 认证接口

#### POST /api/v1/auth/login
**为什么这么设计/选型**：
- 标准JWT认证流程
- 返回Token供前端后续请求携带

**请求结构**：
```json
{
  "username": "user001",
  "password": "encrypted_password"
}
```

**响应结构**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "access_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "token_type": "Bearer",
    "expires_in": 7200,
    "user": {
      "id": 1,
      "username": "user001",
      "full_name": "张三",
      "role": "user"
    }
  }
}
```

### 5.2 靶点组合竞争格局接口

#### POST /api/v1/target-combo/query
**为什么这么设计/选型**：
- 支持复杂筛选条件（疾病、研发阶段）
- 返回二维矩阵数据，前端渲染

**请求结构**：
```json
{
  "disease_names": ["Alcoholic Liver Disease", "Gastroesophageal Reflux"],
  "phases": ["Phase I", "Phase II", "Phase III", "BLA", "Approved"],
  "hide_empty_target": true,
  "hide_single_target": false
}
```

**响应结构**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "targets": [
      {
        "name": "IL-4Rα",
        "highest_phase": "Approved",
        "highest_score": 4.0
      },
      {
        "name": "IL-5",
        "highest_phase": "Phase III",
        "highest_score": 3.0
      }
    ],
    "matrix": [
      {
        "row_target": "IL-4Rα",
        "col_target": "IL-5",
        "score": 3.0,
        "drug_info": {
          "drug_name": "dupilumab",
          "company_names": ["Regeneron", "Sanofi"],
          "nct_id": "NCT02277743",
          "phase_date": "2017-03-28"
        }
      }
    ],
    "total_count": 156
  }
}
```

#### POST /api/v1/target-combo/save-preset
**为什么这么设计/选型**：
- 保存用户自定义筛选条件
- 支持命名和覆盖

**请求结构**：
```json
{
  "name": "GI领域常用筛选",
  "disease_names": ["Alcoholic Liver Disease", "Gastroesophageal Reflux"],
  "phases": ["Phase II", "Phase III"],
  "is_default": false
}
```

### 5.3 靶点研发进展格局接口

#### POST /api/v1/target-progress/disease-view
**为什么这么设计/选型**：
- 按疾病维度查询管线分布
- 返回表格化数据，每个单元格包含多个药品卡片

**请求结构**：
```json
{
  "disease_name": "Acne",
  "targets": ["IL-36R", "AR", "CGRP receptor"],
  "include_all_targets": false
}
```

**响应结构**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "disease": "Acne",
    "pipeline": [
      {
        "target": "AR",
        "drugs_by_phase": {
          "Phase 2": [
            {
              "drug_name": "GT20029",
              "company_names": ["开拓药业"],
              "nct_id": "CTR20240799",
              "phase_date": "2024-03-22"
            }
          ]
        }
      },
      {
        "target": "IL-36R",
        "drugs_by_phase": {
          "PreClinical": [
            {
              "drug_name": "HBM-036",
              "company_names": ["和铂医药"],
              "nct_id": null,
              "phase_date": null
            }
          ]
        }
      }
    ]
  }
}
```

### 5.4 预设管理接口

#### GET /api/v1/presets
**为什么这么设计/选型**：
- 获取用户的预设列表
- 包含系统默认预设

**响应结构**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "system_presets": [
      {
        "id": 1,
        "name": "系统默认筛选",
        "preset_data": {
          "disease_names": [],
          "phases": ["Phase I", "Phase II", "Phase III", "BLA", "Approved"]
        }
      },
      {
        "id": 2,
        "name": "GI领域查询",
        "preset_data": {
          "disease_names": ["Alcoholic Liver Disease", "GERD"],
          "phases": ["Phase II", "Phase III"]
        }
      }
    ],
    "user_presets": [
      {
        "id": 10,
        "name": "我的筛选1",
        "preset_data": {...}
      }
    ]
  }
}
```

---

## 六、安全设计

### 6.1 认证机制
**为什么这么设计/选型**：
- 采用JWT（JSON Web Token）无状态认证
- Token有效期2小时，支持刷新Token机制
- 密码采用bcrypt加盐哈希存储

### 6.2 授权机制
**为什么这么设计/选型**：
- 基于RBAC（Role-Based Access Control）
- 角色层级：admin > user > guest
- 接口级别权限校验（@permission_required装饰器）

### 6.3 数据加密
**为什么这么设计/选型**：
- 传输层：HTTPS（TLS 1.2+）
- 存储层：敏感字段（密码）bcrypt哈希
- 数据库连接：SSL加密

### 6.4 审计日志
**为什么这么设计/选型**：
- 记录用户登录、查询、数据修改操作
- 日志包含：用户ID、操作类型、IP、时间戳、请求参数
- 支持日志脱敏（隐藏敏感字段）

---

## 七、性能设计

### 7.1 吞吐量目标
**为什么这么设计/选型**：
- 目标QPS：100+（并发用户数20-30）
- 靶点组合查询响应时间：< 2秒
- 靶点进展查询响应时间：< 1.5秒

### 7.2 延迟优化策略
**为什么这么设计/选型**：
- 数据库连接池：最大连接数50，预加载常用查询
- 异步IO：FastAPI异步处理请求
- 数据预计算：定时任务预计算热点查询结果

### 7.3 缓存策略
**为什么这么设计/选型**：

| 缓存层级 | 缓存对象 | TTL | 失效策略 |
|---------|---------|-----|---------|
| Redis L1 | 用户会话 | 2小时 | 主动过期 |
| Redis L2 | 查询结果（完整参数） | 10分钟 | 主动过期 |
| Redis L3 | 热点疾病列表 | 1小时 | 主动过期 |
| PostgreSQL | 靶点组合矩阵（预计算） | 24小时 | 定时刷新 |

**缓存击穿防护**：
- 使用Redis SETNX分布式锁，防止缓存失效时并发查询数据库
- 永久缓存基础数据（疾病列表、靶点列表）

### 7.4 数据库优化
**为什么这么设计/选型**：
- 复合索引：`(drug_id, indication_top_global_latest_stage)`
- GIN索引：JSONB字段（disease_names、targets、company_names）
- 分页查询：使用Cursor-based分页，避免OFFSET性能问题
- 读写分离：查询走只读副本，写入走主库

---

## 八、部署架构与环境规划

### 8.1 部署架构图
**为什么这么设计/选型**：
- 采用微服务化部署，便于扩展
- 数据层与应用层分离
- 引入消息队列解耦数据摄入与业务逻辑

```
┌─────────────────────────────────────────────────────────────┐
│                         客户端                                │
│                    (Vue3 + TypeScript)                       │
└──────────────────────────┬──────────────────────────────────┘
                           │ HTTPS
                           ▼
┌─────────────────────────────────────────────────────────────┐
│                      Nginx（反向代理）                        │
│                   SSL终端 + 负载均衡 + 静态资源              │
└──────┬───────────────────────┬───────────────────────┬──────┘
       │                       │                       │
       ▼                       ▼                       ▼
┌──────────────┐      ┌──────────────┐      ┌──────────────┐
│ FastAPI实例1 │      │ FastAPI实例2 │      │ FastAPI实例3 │
│   (Worker)   │      │   (Worker)   │      │   (Worker)   │
└──────┬───────┘      └──────┬───────┘      └──────┬───────┘
       │                     │                     │
       └─────────────────────┼─────────────────────┘
                             │
            ┌────────────────┼────────────────┐
            ▼                ▼                ▼
     ┌─────────────┐  ┌─────────────┐  ┌─────────────┐
     │ PostgreSQL  │  │  Redis      │  │  RabbitMQ   │
     │ (主+从)     │  │  (缓存+队列) │  │  (任务队列) │
     └─────────────┘  └─────────────┘  └──────┬──────┘
                                            │
                              ┌─────────────┴─────────────┐
                              ▼                          ▼
                     ┌─────────────┐          ┌─────────────┐
                     │  Celery     │          │  Celery    │
                     │  Worker1    │          │  Worker2   │
                     └──────┬──────┘          └──────┬──────┘
                            │                        │
                            └──────────┬─────────────┘
                                       │
                                       ▼
                              ┌─────────────┐
                              │  阿里云OSS  │
                              │  (数据源)   │
                              └─────────────┘
```

### 8.2 环境规划
**为什么这么设计/选型**：

| 环境 | 用途 | 配置 |
|------|------|------|
| 开发环境 | 本地开发调试 | Docker Compose单机部署 |
| 测试环境 | 功能测试、集成测试 | 阿里云ECS 2核4G × 2 |
| 预发布环境 | 性能测试、灰度发布 | 阿里云ECS 4核8G × 2 |
| 生产环境 | 正式服务 | 阿里云ECS 8核16G × 3 + RDS PostgreSQL + Redis |

### 8.3 容器化部署
**为什么这么设计/选型**：
- 使用Docker封装应用，保证环境一致性
- 编排工具：Kubernetes（生产环境）或Docker Compose（开发/测试）
- 镜像仓库：阿里云容器镜像服务（ACR）

### 8.4 CI/CD流程
**为什么这么设计/选型**：
- GitLab CI/CD流水线
- 流程：代码提交 → 单元测试 → 构建镜像 → 推送ACR → 自动部署到测试环境
- 生产环境需要人工触发或批准

---

## 九、风险点与 Mitigation 方案

### 9.1 数据质量风险
**风险描述**：
医药魔方推送的Parquet文件可能存在：
- 字段缺失或格式不一致
- 重复记录（同一drug_id有多条最新记录）
- 阶段映射规则变更

**Mitigation 方案**：
- **数据校验层**：入库前进行字段完整性校验，缺失字段填充默认值
- **去重策略**：按`(drug_id, disease_names)`分组，保留`nct_id = highest_trial_id`的记录
- **版本管理**：阶段映射规则配置化，支持动态调整
- **数据质量报告**：每日生成数据入库报告，异常数据告警

### 9.2 性能瓶颈风险
**风险描述描述**：
- 靶点组合矩阵计算涉及笛卡尔积，数据量大时响应慢
- 并发查询可能导致数据库连接耗尽
- Parquet文件解析占用大量内存

**Mitigation 方案**：
- **预计算策略**：定时任务预计算热点查询（常见疾病组合）的结果，存入Redis
- **查询限流**：使用Redis + 滑动窗口算法，限制单用户QPS ≤ 10
- **分页加载**：前端分批加载矩阵数据，虚拟滚动
- **流式处理**：使用PyArrow流式读取Parquet，避免全量加载到内存

### 9.3 安全风险
**风险描述**：
- 未授权访问医药敏感数据
- SQL注入、XSS攻击
- 数据传输泄露

**Mitigation 方案**：
- **最小权限原则**：数据库用户仅授予SELECT权限
- **输入校验**：使用Pydantic严格校验请求参数
- **WAF防护**：部署Web应用防火墙（阿里云WAF）
- **安全审计**：定期扫描漏洞，启用HTTPS + HSTS

### 9.4 可用性风险
**风险描述**：
- 数据库宕机导致服务不可用
- OSS访问失败导致数据摄入中断
- Celery任务堆积导致数据处理延迟

**Mitigation 方案**：
- **高可用架构**：PostgreSQL主从复制 + 自动故障转移
- **降级策略**：OSS访问失败时，从缓存返回旧数据 + 告警
- **监控告警**：Prometheus + Grafana监控资源使用，异常时发送钉钉告警
- **任务重试**：Celery任务配置指数退避重试机制

### 9.5 扩展性风险
**风险描述**：
- 数据量增长导致查询性能下降
- 新功能需求增加系统复杂度

**Mitigation 方案**：
- **分库分表**：按`drug_id`哈希分表，水平扩展
- **读写分离**：查询走只读副本，减轻主库压力
- **服务拆分**：预留接口边界，未来可拆分为独立服务（如AI助手服务）
- **版本兼容**：API版本化管理（/api/v1/、/api/v2/），支持多版本共存

---

## 十、附录

### 10.1 开发阶段分值映射代码示例
```python
PHASE_SCORE_MAPPING = {
    "临床前": (0.1, "PreClinical"),
    "申报临床": (0.5, "IND"),
    "I期临床": (1.0, "Phase I"),
    "I/II期临床": (1.5, "Phase I/II"),
    "II期临床": (2.0, "Phase II"),
    "II/III期临床": (2.5, "Phase II/III"),
    "III期临床": (3.0, "Phase III"),
    "申请上市": (3.5, "BLA"),
    "批准上市": (4.0, "Approved"),
}

def get_phase_score(stage: str) -> tuple[float, str]:
    """获取阶段分值和显示名称"""
    return PHASE_SCORE_MAPPING.get(stage, (0.0, "Unknown"))
```

### 10.2 参考文档
- 需求文档-v1.0.0 - 飞书云文档.md
- apex_prototype.html（原型设计）
- 医药魔方数据推送规范（由数据提供方维护）

---

**文档版本**：v1.0.0
**创建日期**：2026-03-30
**编写人**：InfCode
**审核状态**：待审核
