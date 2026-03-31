# TECH_SPEC — Apex 早期靶点情报分析智能体

> **版本**: v1.0.0 &nbsp;|&nbsp; **最后更新**: 2026-03-30 &nbsp;|&nbsp; **状态**: Draft

---

## 目录

1. [项目背景与目标](#1-项目背景与目标)
2. [核心模块划分](#2-核心模块划分)
3. [技术栈选型](#3-技术栈选型)
4. [核心数据模型设计](#4-核心数据模型设计)
5. [关键接口设计](#5-关键接口设计)
6. [安全设计](#6-安全设计)
7. [性能设计](#7-性能设计)
8. [部署架构与环境规划](#8-部署架构与环境规划)
9. [风险点与 Mitigation 方案](#9-风险点与-mitigation-方案)

---

## 1. 项目背景与目标

### 1.1 项目背景

和铂医药（Harbour BioMed）在药物早期研发阶段需要对靶点组合的竞争格局和研发进展进行系统化分析。当前行业内靶点情报散落在多个数据源中，分析师依赖人工汇总 Excel 表格进行交叉比对，效率低、时效差、无法快速响应决策需求。

数据来源方为「医药魔方」（PharmCube），每天凌晨 5:00 通过全量推送方式将 3 个 Parquet 文件投递到阿里云 OSS：

| 文件名 | 用途 | 当前版本是否使用 |
|--------|------|------------------|
| `pharmcube2harbour_ci_tracking_info_0.parquet` | 竞争情报追踪（含靶点、疾病、研发阶段、药品信息等 55+ 字段） | ✅ 主要数据源 |
| `pharmcube2harbour_drug_pipeline_info_0.parquet` | 药品管线基本信息（含药品分类、研发机构、交易信息等 51 字段） | ⚠️ 当前预留，后续迭代使用 |
| 临床试验详细信息文件 | 临床试验详情 | ❌ 暂不使用 |

### 1.2 项目目标

构建名为 **Apex 早期靶点情报分析智能体** 的 Web 应用，实现：

- **靶点组合竞争格局**：以二维矩阵方式展示靶点 × 疾病的最高研发阶段，支持按治疗领域/疾病/研发阶段多维筛选，悬浮查看药品详情。
- **靶点研发进展格局（疾病视图）**：选定单个疾病后，以管线全景图展示该疾病下所有靶点在各研发阶段的药物分布。
- **数据每日自动刷新**：OSS → PostgreSQL 的定时 ETL 管道，确保分析师每日上班即可看到最新数据。
- **后续扩展预留**：多抗组合详细信息查询、靶点视图、管线历史事件视图等在架构层面做好预留。

### 1.3 为什么这么定目标

> 医药早期研发的竞争情报决策窗口通常在 1–2 周内。如果分析师需要 3 天手动整理 Excel，决策时效性大打折扣。矩阵视图+管线全景图的可视化方式，可以将信息吸收时间从小时级压缩到分钟级，让决策者在一屏之内理解某个靶点组合在全球范围内的竞争态势。

---

## 2. 核心模块划分

### 2.1 模块总览

```
apex-platform/
├── 认证模块 (auth)
│   ├── 登录/登出
│   └── 会话管理
├── 数据同步模块 (data-sync)
│   ├── OSS 文件拉取
│   ├── Parquet 解析
│   └── 数据写入 PostgreSQL
├── 靶点组合竞争格局模块 (competition)
│   ├── 疾病筛选（治疗领域→疾病级联）
│   ├── 研发阶段筛选（多选 + 分值映射）
│   ├── 矩阵查询引擎
│   ├── 矩阵结果渲染
│   ├── 悬浮药品详情
│   ├── 隐藏无组合靶点
│   ├── 筛选条件保存/加载
│   └── 导出功能
├── 靶点研发进展格局模块 (progress)
│   ├── 疾病视图
│   │   ├── 疾病单选
│   │   ├── 靶点多选（与疾病级联）
│   │   ├── 管线全景图渲染
│   │   └── 悬浮药品详情
│   ├── [预留] 靶点视图
│   └── [预留] 管线历史事件视图
├── [预留] 多抗组合详细信息查询模块 (multi-antibody)
├── 查询条件管理模块 (filter-preset)
│   ├── 保存筛选条件
│   ├── 加载筛选条件
│   └── 默认筛选条件
├── 通用基础模块 (common)
│   ├── 统一响应封装
│   ├── 全局异常处理
│   ├── 操作日志
│   └── 数据导出（Excel）
└── 前端应用 (web)
    ├── 布局框架（侧边栏 + 顶栏 + 内容区）
    ├── 靶点组合竞争格局页面
    ├── 靶点研发进展格局页面
    └── 通用组件库
```

### 2.2 为什么这么拆分

> **按业务域拆分，而非按技术层拆分。** 每个模块对应一个独立的业务关注点，模块间通过接口解耦。这样做的好处是：
> - 靶点组合竞争格局和靶点研发进展格局的查询逻辑复杂度差异大，分开可以独立优化；
> - 数据同步模块与业务查询完全解耦，ETL 失败不影响在线查询（使用上一次成功同步的数据）；
> - 预留模块在目录结构和路由层面已就位，后续迭代不需要重构架构。

### 2.3 子模块详细说明

#### 2.3.1 认证模块 (auth)

| 子模块 | 职责 |
|--------|------|
| 登录/登出 | 账号密码认证，返回 JWT Token |
| 会话管理 | Token 刷新、过期处理、Redis 黑名单 |

#### 2.3.2 数据同步模块 (data-sync)

| 子模块 | 职责 |
|--------|------|
| OSS 文件拉取 | 每日 5:30 从阿里云 OSS 拉取 Parquet 文件 |
| Parquet 解析 | 使用 Apache Arrow / parquet-mr 解析文件 |
| 数据写入 | 全量覆盖式写入 PostgreSQL，在事务内完成新旧表切换 |

#### 2.3.3 靶点组合竞争格局模块 (competition)

| 子模块 | 职责 |
|--------|------|
| 疾病筛选 | 治疗领域→疾病的树形级联选择，支持模糊搜索、全选、多选 |
| 研发阶段筛选 | 9 个阶段的多选，每个阶段对应分值（Approved=4.0 ~ PreClinical=0.1） |
| 矩阵查询引擎 | 构建靶点×疾病的交叉聚合 SQL，按最高阶段分值计算 |
| 矩阵结果渲染 | 前端虚拟滚动二维表格，冻结首行首列，分值颜色编码 |
| 悬浮药品详情 | Hover 格子时异步加载药品英文名、原研机构、所有研究机构、最高阶段日期、nctId |
| 隐藏无组合靶点 | 开关控制，过滤掉仅出现在单一疾病中的靶点行 |
| 筛选条件保存/加载 | 用户可保存当前筛选为预设，下次直接加载 |
| 导出功能 | 将当前矩阵结果导出为 Excel |

#### 2.3.4 靶点研发进展格局模块 (progress)

| 子模块 | 职责 |
|--------|------|
| 疾病视图-疾病单选 | 下拉选择单个疾病（默认选第一个，不可不选） |
| 疾病视图-靶点多选 | 与疾病级联，选中疾病后默认全选该疾病下所有靶点，支持模糊搜索和多选 |
| 疾病视图-管线全景图 | 横轴为研发阶段（PreClinical → Approved），纵轴为靶点，药物以色块展示 |
| 疾病视图-悬浮药品详情 | Hover 药物色块时显示药品名、靶点、当前阶段、原研机构、研究机构、日期、nctId |
| [预留] 靶点视图 | 下个迭代实现 |
| [预留] 管线历史事件视图 | 下个迭代实现 |

---

## 3. 技术栈选型

### 3.1 后端技术栈

| 技术 | 版本 | 选型理由 |
|------|------|----------|
| **Java** | 21 (LTS) | Java 21 是最新长期支持版本，支持虚拟线程（Project Loom），在高并发场景下可显著提升吞吐量；医药行业对稳定性要求高，Java 生态成熟、企业级支持完善。 |
| **Spring Boot** | 3.3.x | Spring Boot 3.x 基于 Jakarta EE，原生支持 Java 21 虚拟线程，自动配置减少样板代码，社区活跃、文档完善。 |
| **Spring Security** | 6.x | 与 Spring Boot 3 无缝集成，支持 JWT 无状态认证，过滤器链可灵活配置，避免引入额外安全框架。 |
| **MyBatis-Plus** | 3.5.x | 相比 JPA，MyBatis-Plus 在复杂聚合查询（矩阵交叉计算）场景下 SQL 控制力更强；内置分页、条件构造器提升开发效率；团队普遍熟悉。 |
| **Apache Parquet (parquet-mr)** | 1.14.x | Java 原生 Parquet 读取库，与 Hadoop 生态兼容，能直接在 JVM 中解析医药魔方推送的 Parquet 文件，无需引入 Spark/Flink 等重量级框架。 |
| **Aliyun OSS SDK** | 3.17.x | 官方 SDK，直接对接阿里云 OSS 拉取每日数据文件，支持断点续传和校验。 |
| **XXL-Job** | 2.4.x | 轻量级分布式任务调度框架，用于每日定时 ETL 任务；相比 Quartz 提供可视化管理后台，支持任务失败报警和手动重试。选 XXL-Job 而非 Spring Scheduler 是因为需要可视化监控和失败重试能力。 |
| **MapStruct** | 1.5.x | 编译期对象映射，零反射开销，用于 DTO/VO/Entity 之间转换。 |
| **Knife4j (Swagger)** | 4.x | 基于 OpenAPI 3 的 API 文档，开发联调时前后端共用同一份接口定义，减少沟通成本。 |

### 3.2 前端技术栈

| 技术 | 版本 | 选型理由 |
|------|------|----------|
| **React** | 19 | React 19 支持 React Compiler（自动 memo）和改进的 Suspense，在数据密集型表格渲染场景下减少不必要的重渲染；团队技术栈统一为 React。 |
| **TypeScript** | 5.x | 强类型约束在大量 API 数据结构（55+ 字段的 Parquet 映射）场景下能有效防止类型错误，IDE 自动补全提升开发效率。 |
| **Ant Design** | 5.x | 医药行业 B 端应用，Ant Design 的 Table（虚拟滚动）、Select（远程搜索）、TreeSelect（树形级联选择）等组件与需求高度匹配，主题定制能力强。 |
| **Axios** | 1.x | HTTP 客户端，支持请求/响应拦截器（统一注入 Token、统一处理错误码）、取消请求（页面切换时中断未完成请求）。 |
| **React Query (TanStack Query)** | 5.x | 服务端状态管理，自动缓存矩阵查询结果，切换筛选条件时如果命中缓存则瞬时展示，大幅提升交互体验。比 SWR 多了离线支持和更细粒度的缓存控制。 |
| **Zustand** | 4.x | 轻量客户端状态管理，管理筛选条件、UI 状态等。比 Redux 简洁，比 Context 性能好（选择性订阅，避免无关组件重渲染）。 |
| **AG Grid Community** | 31.x | 矩阵表格渲染引擎，原生支持行列冻结（Pinned Columns/Rows）、虚拟滚动（百万级单元格）、单元格自定义渲染。Ant Design Table 在超大矩阵场景下性能不足，AG Grid 是工业级替代。 |
| **ECharts** | 5.x | 管线全景图的自定义图表渲染。相比 D3 封装程度更高、开发效率更高；相比 Recharts 自定义能力更强（需要自定义药物色块渲染）。 |
| **Vite** | 5.x | 构建工具，开发环境秒级热更新（ESM native），生产构建基于 Rollup，Tree-shaking 优化包体积。 |

### 3.3 数据库

| 技术 | 版本 | 选型理由 |
|------|------|----------|
| **PostgreSQL** | 16.x | 1) 对复杂聚合查询（GROUP BY + CASE WHEN + 多表 JOIN）优化优秀，矩阵计算场景下比 MySQL 的查询计划更智能；2) 原生支持 JSONB 类型，靶点字段（逗号分隔多靶点）可以预处理为数组后使用 GIN 索引加速查询；3) 支持 `ARRAY` 类型和 `unnest()` 函数，处理"多靶点用逗号分割"的字段非常自然；4) 窗口函数能力强，方便计算"每个靶点×疾病组合的最高阶段"。 |

### 3.4 中间件

| 技术 | 版本 | 选型理由 |
|------|------|----------|
| **Redis** | 7.x | 1) 缓存热点查询结果（矩阵数据每日更新一次，缓存 TTL 设为 24h 即可）；2) JWT 黑名单存储；3) 分布式锁（防止 ETL 任务并发执行）。选 Redis 而非 Caffeine 是因为需要跨实例共享缓存。 |
| **Nginx** | 1.25.x | 反向代理 + 静态资源服务 + Gzip 压缩 + HTTPS 终止。 |
| **Docker + Docker Compose** | 24.x / 2.x | 容器化部署，保证开发/测试/生产环境一致性。 |

---

## 4. 核心数据模型设计

### 4.1 为什么这么设计

> 数据模型以"查询效率优先"为原则。原始 Parquet 数据有 55+ 字段，但当前业务只用到其中约 20 个字段。因此我们不做 1:1 映射，而是抽取出面向查询的宽表（`ci_tracking_latest`）+ 维度表（`therapeutic_area`、`disease`）的星型结构。这样做的好处：
> - 矩阵查询只需扫描一张宽表，避免多表 JOIN；
> - 维度表支持治疗领域→疾病的级联选择；
> - 每日全量同步时使用"影子表切换"策略，不影响在线查询。

### 4.2 ER 关系概览

```
┌──────────────────┐       ┌──────────────────────────┐
│ therapeutic_area  │ 1───N │ disease                  │
│──────────────────│       │──────────────────────────│
│ id (PK)          │       │ id (PK)                  │
│ name_en          │       │ ta_id (FK)               │
│ name_cn          │       │ name_en                  │
│ sort_order       │       │ name_cn                  │
└──────────────────┘       │ abbreviation             │
                           └──────────┬───────────────┘
                                      │ 1
                                      │
                                      │ N
                    ┌─────────────────────────────────────────┐
                    │ ci_tracking_latest                      │
                    │─────────────────────────────────────────│
                    │ id (PK, BIGSERIAL)                      │
                    │ drug_id (VARCHAR)                        │
                    │ drug_name_en (VARCHAR)                   │
                    │ drug_name_cn (VARCHAR)                   │
                    │ targets (TEXT[])  ← 数组类型，GIN 索引  │
                    │ targets_raw (VARCHAR)                    │
                    │ disease_id (FK → disease.id)             │
                    │ harbour_indication_name (VARCHAR)        │
                    │ ta (VARCHAR)                             │
                    │ moa (VARCHAR)                            │
                    │ originator (VARCHAR)                     │
                    │ research_institute (VARCHAR)             │
                    │ global_highest_phase (VARCHAR)           │
                    │ global_highest_phase_score (NUMERIC(3,1))│
                    │ indication_top_global_latest_stage       │
                    │ indication_top_global_start_date (DATE)  │
                    │ highest_trial_id (VARCHAR)               │
                    │ highest_trial_phase (VARCHAR)            │
                    │ nct_id (VARCHAR)                         │
                    │ data_source (VARCHAR)                    │
                    │ sync_batch_id (VARCHAR)                  │
                    │ synced_at (TIMESTAMP)                    │
                    │ created_at (TIMESTAMP)                   │
                    └─────────────────────────────────────────┘

                    ┌─────────────────────────────────────────┐
                    │ drug_pipeline_info (预留)                │
                    │─────────────────────────────────────────│
                    │ id (PK, BIGSERIAL)                      │
                    │ drug_name_en (VARCHAR)                   │
                    │ drug_name_cn (VARCHAR)                   │
                    │ targets (TEXT[])                         │
                    │ company_names_originator (VARCHAR)       │
                    │ pharmacological_name (VARCHAR)           │
                    │ ... 其余 45 字段按需补充                 │
                    │ sync_batch_id (VARCHAR)                  │
                    │ synced_at (TIMESTAMP)                    │
                    └─────────────────────────────────────────┘

┌──────────────────────────┐       ┌──────────────────────────┐
│ sys_user                 │       │ filter_preset            │
│──────────────────────────│       │──────────────────────────│
│ id (PK)                  │       │ id (PK)                  │
│ username (UNIQUE)        │       │ user_id (FK)             │
│ password_hash (VARCHAR)  │       │ name (VARCHAR)           │
│ display_name (VARCHAR)   │       │ module (VARCHAR)         │
│ role (VARCHAR)           │       │ conditions (JSONB)       │
│ status (SMALLINT)        │       │ is_default (BOOLEAN)     │
│ created_at (TIMESTAMP)   │       │ created_at (TIMESTAMP)   │
│ updated_at (TIMESTAMP)   │       │ updated_at (TIMESTAMP)   │
└──────────────────────────┘       └──────────────────────────┘
```

### 4.3 关键 DDL

```sql
-- 研发阶段分值映射表
CREATE TABLE phase_score_mapping (
    phase_name   VARCHAR(50) PRIMARY KEY,
    score        NUMERIC(3,1) NOT NULL,
    sort_order   SMALLINT     NOT NULL,
    color_code   VARCHAR(7)   -- 前端颜色编码 如 #10B981
);

INSERT INTO phase_score_mapping VALUES
('Approved',    4.0, 1, '#10B981'),
('BLA',         3.5, 2, '#06B6D4'),
('Phase III',   3.0, 3, '#3B82F6'),
('Phase II/III',2.5, 4, '#6366F1'),
('Phase II',    2.0, 5, '#8B5CF6'),
('Phase I/II',  1.5, 6, '#A855F7'),
('Phase I',     1.0, 7, '#D946EF'),
('IND',         0.5, 8, '#F59E0B'),
('PreClinical', 0.1, 9, '#6B7280');

-- 竞争情报主表（查询宽表）
CREATE TABLE ci_tracking_latest (
    id                              BIGSERIAL PRIMARY KEY,
    drug_id                         VARCHAR(50),
    drug_name_en                    VARCHAR(200),
    drug_name_cn                    VARCHAR(200),
    targets                         TEXT[]       NOT NULL DEFAULT '{}',
    targets_raw                     VARCHAR(500),
    disease_id                      INTEGER      REFERENCES disease(id),
    harbour_indication_name         VARCHAR(300),
    ta                              VARCHAR(100),
    moa                             VARCHAR(500),
    originator                      VARCHAR(300),
    research_institute              VARCHAR(1000),
    global_highest_phase            VARCHAR(50),
    global_highest_phase_score      NUMERIC(3,1),
    indication_top_global_latest_stage VARCHAR(50),
    indication_top_global_start_date   DATE,
    highest_trial_id                VARCHAR(100),
    highest_trial_phase             VARCHAR(50),
    nct_id                          VARCHAR(50),
    data_source                     VARCHAR(100),
    sync_batch_id                   VARCHAR(50)  NOT NULL,
    synced_at                       TIMESTAMP    NOT NULL DEFAULT NOW(),
    created_at                      TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- GIN 索引加速靶点数组查询
CREATE INDEX idx_ci_tracking_targets ON ci_tracking_latest USING GIN (targets);
-- 组合索引加速矩阵查询
CREATE INDEX idx_ci_tracking_matrix ON ci_tracking_latest (ta, harbour_indication_name, global_highest_phase_score DESC);
-- 疾病 + 靶点组合索引
CREATE INDEX idx_ci_tracking_disease_target ON ci_tracking_latest (disease_id, targets);

-- 筛选条件预设
CREATE TABLE filter_preset (
    id          SERIAL PRIMARY KEY,
    user_id     INTEGER      NOT NULL REFERENCES sys_user(id),
    name        VARCHAR(100) NOT NULL,
    module      VARCHAR(50)  NOT NULL, -- 'competition' | 'progress'
    conditions  JSONB        NOT NULL,
    is_default  BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP    NOT NULL DEFAULT NOW()
);
```

### 4.4 阶段分值映射规则

> 需求文档明确定义了研发阶段与分值的对应关系，这是矩阵颜色编码和排序的核心依据。该映射在数据库中维护（`phase_score_mapping` 表），前后端共用，避免硬编码：

| 原始阶段 | 映射后显示 | 分值 |
|----------|-----------|------|
| Approved / Market | Approved | 4.0 |
| BLA / NDA / ANDA / sNDA / sBLA | BLA | 3.5 |
| Phase 3 / Phase III | Phase III | 3.0 |
| Phase 2/3 / Phase II/III | Phase II/III | 2.5 |
| Phase 2 / Phase II | Phase II | 2.0 |
| Phase 1/2 / Phase I/II | Phase I/II | 1.5 |
| Phase 1 / Phase I | Phase I | 1.0 |
| IND | IND | 0.5 |
| Preclinical / Discovery | PreClinical | 0.1 |

> **关键点**：`ci_tracking_info` 表中 `indication_top_global_latest_stage` 字段的原始值需要在 ETL 阶段做映射和归一化，写入 `global_highest_phase_score` 数值字段。靶点组合竞争格局和靶点研发进展格局的数据都基于映射后的值。

---

## 5. 关键接口设计

### 5.1 通用约束

#### 5.1.1 RESTful 规范

- URL 使用名词复数形式：`/api/v1/diseases`、`/api/v1/competition/matrix`
- 动作通过 HTTP Method 表达：GET 查询、POST 创建、PUT 全量更新、PATCH 部分更新、DELETE 删除
- 版本号放在 URL 路径中：`/api/v1/...`

#### 5.1.2 统一响应格式

```json
{
  "code": 0,
  "message": "success",
  "data": { ... },
  "traceId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "timestamp": 1711785600000
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| `code` | Integer | 0=成功，非 0=失败（4 位业务错误码） |
| `message` | String | 人类可读的提示信息 |
| `data` | Object/Array/null | 业务数据 |
| `traceId` | String | 全链路追踪 ID（UUID v4），方便排查问题 |
| `timestamp` | Long | 服务端时间戳（毫秒） |

#### 5.1.3 错误码规则（4 位业务码）

```
格式：XYYY
X   = 模块标识（1=认证, 2=竞争格局, 3=研发进展, 4=数据同步, 9=通用）
YYY = 模块内序号（001-999）

示例：
1001 = 用户名或密码错误
1002 = Token 已过期
1003 = 无权限
2001 = 疾病参数无效
2002 = 矩阵数据为空
3001 = 疾病必选
3002 = 靶点参数无效
9001 = 参数校验失败
9002 = 系统内部错误
9003 = 请求频率过高
```

> **为什么用 4 位码而非 HTTP 状态码**：HTTP 状态码只有有限的几个（400/401/403/404/500），无法精确表达"用户名不存在"和"密码错误"的区别。4 位业务码可以让前端精准匹配错误提示文案，同时 HTTP 状态码仍然正确返回（如 401 搭配 1001/1002）。

### 5.2 认证接口

#### POST `/api/v1/auth/login`

**Request:**
```json
{
  "username": "chenmc",
  "password": "********"
}
```

**Response (200):**
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
    "expiresIn": 7200,
    "tokenType": "Bearer",
    "user": {
      "id": 1,
      "username": "chenmc",
      "displayName": "陈明聪"
    }
  },
  "traceId": "...",
  "timestamp": 1711785600000
}
```

### 5.3 疾病筛选接口

#### GET `/api/v1/diseases/tree`

返回治疗领域→疾病的树形结构，供前端 TreeSelect 组件消费。

**Response:**
```json
{
  "code": 0,
  "data": [
    {
      "id": 1,
      "label": "GI",
      "children": [
        { "id": 101, "label": "Celiac Disease" },
        { "id": 102, "label": "Gastroesophageal Reflux Disease (GERD)" },
        { "id": 103, "label": "Hepatic Fibrosis (HF)" },
        { "id": 104, "label": "Primary Biliary Cholangitis (PBC)" }
      ]
    },
    {
      "id": 2,
      "label": "Dermatology",
      "children": [
        { "id": 201, "label": "Hidradenitis Suppurativa (HS)" },
        { "id": 202, "label": "Pemphigus Vulgaris" },
        { "id": 203, "label": "Psoriasis (PsO)" },
        { "id": 204, "label": "Rosacea" }
      ]
    }
  ]
}
```

> **为什么一次返回全量树**：治疗领域+疾病的总量级在百量级以内，单次请求返回（约 5KB）远比分页懒加载多次请求更高效。前端使用 React Query 缓存，只在首次加载时请求。

### 5.4 靶点组合竞争格局 — 矩阵查询

#### POST `/api/v1/competition/matrix`

> **为什么用 POST 而非 GET**：筛选条件（疾病 ID 列表、研发阶段列表）可能很长，URL 有长度限制。且条件包含数组，放 Body 中更清晰。

**Request:**
```json
{
  "diseaseIds": [101, 102, 103, 104, 201, 203, 204],
  "phases": ["Approved", "BLA", "Phase III", "Phase II/III", "Phase II", "Phase I/II", "Phase I", "IND", "PreClinical"],
  "hideNoComboTargets": true
}
```

**Response:**
```json
{
  "code": 0,
  "data": {
    "columns": [
      { "diseaseId": 101, "diseaseName": "Celiac Disease" },
      { "diseaseId": 102, "diseaseName": "GERD" },
      { "diseaseId": 203, "diseaseName": "Psoriasis (PsO)" }
    ],
    "rows": [
      {
        "target": "TNF",
        "maxScore": 4.0,
        "sumScore": 7.1,
        "cells": [
          {
            "diseaseId": 101,
            "score": 0,
            "phaseName": null,
            "drugCount": 0
          },
          {
            "diseaseId": 203,
            "score": 4.0,
            "phaseName": "Approved",
            "drugCount": 3
          }
        ]
      }
    ],
    "totalTargets": 12,
    "totalDiseases": 7,
    "updatedAt": "2026-03-30T05:30:00Z"
  }
}
```

### 5.5 矩阵格子 — 药品详情（悬浮查看）

#### GET `/api/v1/competition/cell-drugs?target=TNF&diseaseId=203&phases=Approved,Phase III`

**Response:**
```json
{
  "code": 0,
  "data": {
    "target": "TNF",
    "diseaseName": "Psoriasis (PsO)",
    "drugs": [
      {
        "drugNameEn": "etanercept",
        "originator": "基因泰克",
        "researchInstitute": "Immunex,Pfizer,Genentech,Takeda Pharmaceuticals",
        "highestPhase": "Approved",
        "highestPhaseDate": "2005-09-01",
        "nctId": "NCT00141791"
      },
      {
        "drugNameEn": "adalimumab",
        "originator": "雅培/艾伯维",
        "researchInstitute": "AbbVie",
        "highestPhase": "Approved",
        "highestPhaseDate": "2008-01-16",
        "nctId": "NCT00195689"
      }
    ]
  }
}
```

### 5.6 靶点研发进展格局 — 疾病视图

#### GET `/api/v1/progress/targets?diseaseId=203`

获取指定疾病下的所有靶点列表（供前端多选组件消费）。

**Response:**
```json
{
  "code": 0,
  "data": [
    { "target": "IL-17A", "drugCount": 8 },
    { "target": "TNF", "drugCount": 5 },
    { "target": "IL-23", "drugCount": 7 },
    { "target": "JAK1", "drugCount": 6 }
  ]
}
```

#### POST `/api/v1/progress/disease-view`

**Request:**
```json
{
  "diseaseId": 203,
  "targets": ["IL-17A", "TNF", "IL-23", "JAK1", "IL-4Rα", "TYK2", "PDE4", "IL-36R"]
}
```

**Response:**
```json
{
  "code": 0,
  "data": {
    "diseaseName": "Psoriasis (PsO)",
    "phases": ["PreClinical", "IND", "Phase I", "Phase I/II", "Phase II", "Phase II/III", "Phase III", "BLA", "Approved"],
    "targetRows": [
      {
        "target": "IL-17A",
        "phaseDrugs": {
          "PreClinical": [
            { "drugNameEn": "ABY-035", "originator": "Affibody", "researchInstitute": "Affibody AB", "highestPhaseDate": "2019-03-15", "nctId": null }
          ],
          "Approved": [
            { "drugNameEn": "secukinumab", "originator": "诺华", "researchInstitute": "Novartis", "highestPhaseDate": "2015-01-21", "nctId": "NCT01365455" },
            { "drugNameEn": "ixekizumab", "originator": "礼来", "researchInstitute": "Eli Lilly", "highestPhaseDate": "2016-03-22", "nctId": "NCT01474512" }
          ]
        }
      }
    ],
    "totalDrugs": 32,
    "updatedAt": "2026-03-30T05:30:00Z"
  }
}
```

### 5.7 筛选条件预设

#### POST `/api/v1/filter-presets`（保存）

```json
{
  "name": "GI领域查询",
  "module": "competition",
  "conditions": {
    "diseaseIds": [101, 102, 103, 104],
    "phases": ["Approved", "BLA", "Phase III"],
    "hideNoComboTargets": true
  },
  "isDefault": false
}
```

#### GET `/api/v1/filter-presets?module=competition`（列表）

#### DELETE `/api/v1/filter-presets/{id}`（删除）

### 5.8 数据导出

#### POST `/api/v1/competition/export`

请求参数同矩阵查询，返回 Excel 文件流。

```
Content-Type: application/vnd.openxmlformats-officedocument.spreadsheetml.sheet
Content-Disposition: attachment; filename="competition_matrix_20260330.xlsx"
```

---

## 6. 安全设计

### 6.1 认证方案

| 方面 | 设计 | 理由 |
|------|------|------|
| 认证方式 | JWT（Access Token + Refresh Token） | 无状态认证，后端不存 Session，天然支持水平扩展。Access Token 有效期 2h，Refresh Token 有效期 7d。 |
| 密码存储 | BCrypt（cost=12） | BCrypt 内置盐值，抗彩虹表；cost=12 在当前硬件下单次哈希约 200ms，兼顾安全和登录体验。 |
| Token 黑名单 | Redis SET | 用户登出时将 Access Token 加入 Redis 黑名单（TTL = Token 剩余有效期），每次请求校验。 |
| 登录防暴力破解 | Redis 滑动窗口限流 | 同一用户名 5 分钟内连续失败 5 次，锁定 15 分钟。同一 IP 1 分钟内最多 20 次登录尝试。 |

### 6.2 授权方案

| 方面 | 设计 | 理由 |
|------|------|------|
| 角色模型 | RBAC（admin / analyst） | 当前用户量小（<50 人），两级角色足够。admin 可管理用户和查看系统状态，analyst 只有业务查询权限。 |
| 接口鉴权 | Spring Security 过滤器链 + `@PreAuthorize` | 统一在 Gateway 层校验 Token 有效性，业务接口用注解控制角色。 |
| 数据权限 | 暂不需要 | 当前所有用户看到的数据范围一致（全部疾病、全部靶点），无行级数据隔离需求。 |

### 6.3 数据安全

| 方面 | 设计 | 理由 |
|------|------|------|
| 传输加密 | HTTPS (TLS 1.3) | Nginx 终止 TLS，内部服务间通信走内网，暂不加密（可后续升级 mTLS）。 |
| 敏感字段 | 密码哈希存储，Token 不存库 | 数据库中不存储任何明文密码。 |
| SQL 注入 | MyBatis 参数化查询（`#{}` 而非 `${}`） | MyBatis-Plus 的条件构造器天然使用预编译参数。 |
| XSS 防护 | 前端 React 默认转义 + CSP 头 | React 的 JSX 默认对变量进行 HTML 转义，Content-Security-Policy 限制内联脚本。 |
| CORS | 白名单域名 | 只允许前端部署域名的跨域请求。 |

---

## 7. 性能设计

### 7.1 性能目标

| 指标 | 目标 | 依据 |
|------|------|------|
| 矩阵查询 P95 延迟 | ≤ 800ms | 筛选条件变化后用户可接受等待时间 |
| 悬浮详情 P95 延迟 | ≤ 200ms | 鼠标悬浮场景要求低延迟 |
| 疾病视图查询 P95 延迟 | ≤ 1000ms | 管线全景图数据量更大 |
| 并发用户 | 50 | 当前分析师团队规模 |
| 每日数据同步 | ≤ 10min | ETL 在 5:00–5:30 完成，不影响上班使用 |

### 7.2 缓存策略

```
┌─────────┐    ┌──────────┐    ┌───────────┐    ┌────────────┐
│ Browser  │───▶│ React    │───▶│ Redis     │───▶│ PostgreSQL │
│ Cache    │    │ Query    │    │ (L2)      │    │            │
│          │    │ (L1)     │    │           │    │            │
└─────────┘    └──────────┘    └───────────┘    └────────────┘
  HTTP Cache     staleTime:      TTL: 24h         数据源
  (疾病树)      5min (矩阵)     (矩阵结果)
               Infinity (树)    (疾病树)
```

| 缓存层 | 对象 | 策略 | 理由 |
|--------|------|------|------|
| **React Query (L1)** | 疾病树 | `staleTime: Infinity`，直到刷新页面 | 疾病树一天最多变化一次 |
| **React Query (L1)** | 矩阵结果 | `staleTime: 5min`，`gcTime: 30min` | 同一筛选条件短时间内多次查看不重复请求 |
| **Redis (L2)** | 矩阵查询 | Key = 筛选条件 MD5，TTL = 24h | 不同用户相同筛选条件共享缓存 |
| **Redis (L2)** | 疾病树 | Key = `disease:tree`，TTL = 24h | 全局共享 |
| **Redis (L2)** | 阶段映射 | Key = `phase:mapping`，TTL = 24h | 全局共享 |
| **HTTP Cache** | 静态资源 | `Cache-Control: max-age=31536000, immutable` | Vite 构建产物带 hash，可永久缓存 |

**缓存失效策略**：每日 ETL 完成后，发布 Redis `PUBLISH data:refreshed` 事件，后端监听后清除所有 `competition:*` 和 `progress:*` 缓存 key。

### 7.3 数据库优化

| 优化点 | 方案 | 理由 |
|--------|------|------|
| 矩阵查询 | 预计算物化视图 `mv_competition_matrix` | 矩阵的核心查询是 `GROUP BY target, disease ORDER BY MAX(score)`，每日数据更新后 REFRESH 一次物化视图，查询直接读视图。 |
| 靶点数组查询 | GIN 索引 + `@>` 操作符 | `targets @> '{TNF}'` 利用 GIN 索引，比 `LIKE '%TNF%'` 快一个数量级。 |
| 全量同步 | 影子表切换 | 写入 `ci_tracking_latest_shadow` 表 → 校验行数 → `ALTER TABLE ... RENAME` 原子切换，0 停机。 |
| 连接池 | HikariCP（max=20） | Spring Boot 默认连接池，20 连接足以支撑 50 并发用户（多数请求命中缓存）。 |

### 7.4 前端优化

| 优化点 | 方案 | 理由 |
|--------|------|------|
| 矩阵虚拟渲染 | AG Grid 虚拟滚动 | 矩阵可能有 100+ 靶点 × 50+ 疾病 = 5000+ 单元格，DOM 全量渲染会卡顿，虚拟滚动只渲染可视区域。 |
| 代码分割 | React.lazy + Suspense | 靶点组合/靶点进展两个页面独立 chunk，首屏只加载当前页面代码。 |
| 悬浮防抖 | 200ms debounce | Hover 事件 200ms 后才发起请求，避免鼠标快速划过矩阵时产生大量请求。 |
| 图片/图表 | 管线全景图使用 Canvas 渲染 | ECharts Canvas 模式在大量药物色块场景下比 SVG 性能更好。 |

---

## 8. 部署架构与环境规划

### 8.1 架构图

```
                    ┌──────────────────┐
                    │    用户浏览器     │
                    └────────┬─────────┘
                             │ HTTPS
                    ┌────────▼─────────┐
                    │    Nginx         │
                    │  (TLS 终止)      │
                    │  (静态资源)      │
                    │  (反向代理)      │
                    │  (Gzip)         │
                    └───┬─────────┬───┘
                 /api/* │         │  /*
                    ┌───▼───┐  ┌──▼──────────┐
                    │ Spring│  │ React SPA   │
                    │ Boot  │  │ (静态文件)   │
                    │ :8080 │  └─────────────┘
                    └───┬───┘
                        │
              ┌─────────┼─────────┐
              │         │         │
         ┌────▼───┐ ┌───▼────┐ ┌──▼──────────┐
         │ Redis  │ │ PgSQL  │ │ Aliyun OSS  │
         │ :6379  │ │ :5432  │ │ (数据源)     │
         └────────┘ └────────┘ └─────────────┘
```

### 8.2 环境规划

| 环境 | 用途 | 配置 | 域名 |
|------|------|------|------|
| **开发 (dev)** | 本地开发联调 | Docker Compose 一键启动所有依赖 | localhost:3000 / :8080 |
| **测试 (test)** | 功能测试 + UAT | 1C2G × 1 应用 + 共享 PG/Redis | test-apex.harbourbiomed.com |
| **生产 (prod)** | 正式运行 | 2C4G × 2 应用（负载均衡）+ 2C4G PG + 1C2G Redis | ai-harby.harbourbiomed.com |

### 8.3 Docker Compose 结构（开发环境）

```yaml
services:
  postgres:
    image: postgres:16-alpine
    ports: ["5432:5432"]
    environment:
      POSTGRES_DB: apex
      POSTGRES_USER: apex
      POSTGRES_PASSWORD: ${PG_PASSWORD}
    volumes:
      - pgdata:/var/lib/postgresql/data
      - ./sql/init:/docker-entrypoint-initdb.d

  redis:
    image: redis:7-alpine
    ports: ["6379:6379"]
    command: redis-server --maxmemory 256mb --maxmemory-policy allkeys-lru

  backend:
    build: ./apex-backend
    ports: ["8080:8080"]
    depends_on: [postgres, redis]
    environment:
      SPRING_PROFILES_ACTIVE: dev
      DB_URL: jdbc:postgresql://postgres:5432/apex
      REDIS_HOST: redis

  frontend:
    build: ./apex-frontend
    ports: ["3000:3000"]
    depends_on: [backend]

volumes:
  pgdata:
```

### 8.4 CI/CD

| 阶段 | 工具 | 说明 |
|------|------|------|
| 代码托管 | GitLab / GitHub | 主干开发，feature 分支 PR |
| CI | GitLab CI / GitHub Actions | 自动运行单元测试、Lint、构建 Docker 镜像 |
| 镜像仓库 | 阿里云 ACR | 与部署环境同区域，拉取快 |
| CD | Docker Compose（当前） / K8s（后续） | 当前规模用 Compose 足够，50 人以上再迁 K8s |
| 监控 | Spring Boot Actuator + Prometheus + Grafana | JVM 指标、接口延迟、缓存命中率 |
| 日志 | SLF4J + Logback → ELK（后续） | 当前阶段文件日志 + JSON 格式，后续接入 ELK |

---

## 9. 风险点与 Mitigation 方案

### 9.1 风险矩阵

| # | 风险 | 影响 | 概率 | 等级 | Mitigation |
|---|------|------|------|------|------------|
| R1 | 医药魔方推送延迟或文件损坏 | 当日数据不更新，分析师看到过期数据 | 中 | 高 | 1) ETL 任务增加文件完整性校验（行数 > 阈值、字段数匹配）；2) 失败时保留上一次成功数据不覆盖；3) XXL-Job 失败报警通知运维。 |
| R2 | Parquet 文件 Schema 变更 | ETL 解析失败 | 低 | 高 | 1) 解析时做字段存在性检查，缺失字段赋默认值；2) 新增字段自动忽略；3) Schema 变更发布变更通知。 |
| R3 | 矩阵查询在大数据量下性能退化 | 用户选择全部疾病 + 全部阶段时查询超时 | 中 | 中 | 1) 预计算物化视图，查询直接读视图；2) Redis 缓存热点查询；3) 设置查询超时 30s + 前端提示"数据量较大，请缩小筛选范围"。 |
| R4 | 靶点字段逗号分隔导致查询不精确 | `targets LIKE '%IL-1%'` 会误匹配 IL-17A | 高 | 中 | ETL 阶段将逗号分隔字符串转为 PostgreSQL `TEXT[]` 数组，使用 `@>` 操作符精确匹配，配合 GIN 索引。 |
| R5 | 研发阶段名称不统一 | 同一阶段在数据源中有多种写法（Phase 3 / Phase III / P3） | 高 | 中 | ETL 阶段统一做映射归一化，维护一张 `phase_alias_mapping` 表，所有变体映射到标准名称。 |
| R6 | 全量同步期间表锁导致查询阻塞 | 同步期间用户查询超时 | 低 | 中 | 影子表策略：写入 shadow 表 → 校验 → `ALTER TABLE RENAME` 原子切换，整个过程对在线查询零影响。 |
| R7 | 前端矩阵渲染性能问题 | 100×50 矩阵导致页面卡顿 | 中 | 中 | AG Grid 虚拟滚动，只渲染可视区域（约 20×15 单元格），总 DOM 节点控制在 500 以内。 |
| R8 | Redis 宕机 | 缓存失效，所有请求穿透到数据库 | 低 | 中 | 1) 本地 Caffeine 作为 L1 缓存兜底（TTL=5min）；2) Redis 配置持久化（RDB+AOF）；3) 数据库连接池能承受短时间全量请求。 |
| R9 | JWT Token 泄露 | 攻击者可冒充用户 | 低 | 高 | 1) Access Token 有效期仅 2h；2) 传输层 HTTPS 加密；3) Token 存 httpOnly Cookie（非 localStorage）防 XSS 窃取；4) 支持手动吊销（Redis 黑名单）。 |

### 9.2 未来扩展风险预判

| 风险 | 预防措施 |
|------|----------|
| 用户量从 50 增长到 500+ | 后端无状态设计，可水平扩展；数据库读写分离预留接口（MyBatis-Plus 多数据源）。 |
| 数据量从 10 万条增长到 1000 万条 | 物化视图 + 分区表（按 `ta` 治疗领域分区）；查询层增加分页/限制返回行数。 |
| 新增靶点视图、管线历史事件等模块 | 模块化架构已预留路由和接口命名空间；`drug_pipeline_info` 表已建好。 |
| 从 Docker Compose 迁移到 K8s | 镜像和配置已容器化，只需编写 K8s Deployment/Service YAML。 |

---

## 附录 A：ci_tracking_info 字段清单（当前使用）

| # | 字段名 | 含义 | 是否使用 |
|---|--------|------|----------|
| 1 | ta | 治疗领域 | ✅ |
| 2 | harbour_indication_name | 疾病种类 | ✅ |
| 3 | drug_name_cn | 药品中文名 | ✅ |
| 4 | drug_id | 药品 ID | ✅ |
| 5 | drug_name_en | 药品英文名 | ✅ |
| 6 | all_name_for_search | 药品异名 | ⚠️ 搜索用 |
| 7 | targets | 靶点（多个用逗号分割） | ✅ |
| 8 | moa | 作用机制 | ✅ |
| 9 | originator | 原研机构 | ✅ |
| 10 | research_institute | 所有研究机构 | ✅ |
| 11 | global_highest_phase | 全球最高阶段 | ✅ |
| 12 | highest_trial_id | 最高临床实验 ID | ✅ |
| 13 | highest_trial_phase | 最高临床实验阶段 | ✅ |
| 14 | nct_id | NCT 编号 | ✅ |
| 55 | indication_top_global_latest_stage | 疾病全球最高阶段 | ✅ 核心 |
| 56 | indication_top_global_start_date | 最高阶段开始时间 | ✅ |
| — | 其余字段 | 详见飞书需求文档 2.1 节 | ❌ 暂不使用 |

## 附录 B：开发阶段映射表

原始数据中 `indication_top_global_latest_stage` 字段的值存在多种写法。ETL 阶段需要按以下规则归一化：

| 原始值（部分示例） | 映射后标准值 | 分值 |
|--------------------|-------------|------|
| Approved, Market, Marketed | Approved | 4.0 |
| BLA, NDA, ANDA, sNDA, sBLA | BLA | 3.5 |
| Phase 3, Phase III, P3, Phase 3 (Pivotal) | Phase III | 3.0 |
| Phase 2/3, Phase II/III | Phase II/III | 2.5 |
| Phase 2, Phase II, P2 | Phase II | 2.0 |
| Phase 1/2, Phase I/II | Phase I/II | 1.5 |
| Phase 1, Phase I, P1 | Phase I | 1.0 |
| IND, IND Filed, CTA Filed | IND | 0.5 |
| Preclinical, Discovery, Lead Optimization | PreClinical | 0.1 |

---

*文档结束*
