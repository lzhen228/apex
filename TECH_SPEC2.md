# Apex 早期靶点情报分析智能体 - 技术方案

**文档版本**：v1.0.0
**创建日期**：2026-03-30
**编写人**：InfCode
**审核状态**：待审核

---

## 目录

1. [项目背景与目标](#一项目背景与目标)
2. [核心模块划分](#二核心模块划分)
3. [系统架构图](#三系统架构图)
4. [技术栈选型](#四技术栈选型)
5. [核心数据模型设计](#五核心数据模型设计)
6. [关键接口设计](#六关键接口设计)
7. [安全设计](#七安全设计)
8. [性能设计](#八性能设计)
9. [部署架构与环境规划](#九部署架构与环境规划)
10. [风险点与 Mitigation 方案](#十风险点与-mitigation-方案)
11. [附录](#十一附录)

---

# 一、项目背景与目标

## 1.1 项目背景

**为什么这么设计/选型**：
医药研发领域存在大量的靶点竞争情报和研发管线数据，传统检索方式效率低下，无法快速洞察：
- 靶点组合竞争格局（哪些靶点组合已有研发管线）
- 靶点在不同疾病领域的研发进展
- 管线历史事件追踪

医药魔方每天定时推送最新的临床试验和药品管线数据，需要构建智能分析平台，帮助研发人员快速获取决策支持信息。

## 1.2 项目目标

- 构建Web应用，提供靶点情报的快速查询和可视化分析
- 支持靶点组合竞争格局的二维矩阵展示
- 支持靶点研发进展的疾病视图/靶点视图分析
- 支持用户自定义筛选条件并保存查询结果
- 为后续AI助手（百科问答、文档精读等）预留扩展接口

---

# 二、核心模块划分

## 2.1 用户认证模块

### 模块说明
负责用户身份认证、会话管理和权限控制。

### 子模块
| 子模块 | 功能描述 |
|--------|----------|
| 登录认证 | JWT Token生成与验证 |
| 会话管理 | Token刷新、会话状态维护 |
| 权限控制 | RBAC角色权限校验 |

### 输入输出
**输入**：用户凭证（username/password）或JWT Token
**输出**：用户信息、Token、权限标识

**性能指标**：
- 登录响应时间 < 500ms
- Token验证响应时间 < 50ms

**安全约束**：
- 密码bcrypt加密存储
- Token有效期2小时
- 支持HTTPS传输

### 为什么这么设计/选型
采用标准的JWT Token机制，满足医药行业数据安全需求；支持账号密码登录，预留SSO单点登录能力。

---

## 2.2 数据接入模块

### 模块说明
负责从阿里云OSS拉取医药魔方推送的Parquet数据，进行解析、清洗和入库。

### 子模块
| 子模块 | 功能描述 |
|--------|----------|
| OSS文件监听 | 监听OSS Bucket新文件事件 |
| Parquet文件解析 | PyArrow流式读取Parquet |
| 数据清洗 | 字段校验、格式转换、去重 |
| 数据入库 | 批量UPSERT到PostgreSQL |

### 输入输出
**输入**：OSS文件路径、文件类型标识
**输出**：入库记录数、数据质量报告

**性能指标**：
- 单个Parquet文件处理时间 < 5分钟
- 数据入库速度 > 1000条/秒

**安全约束**：
- 仅限内网访问OSS
- 数据传输加密
- 操作日志记录

### 为什么这么设计/选型
医药魔方每天5点定时推送Parquet文件到阿里云OSS，需要自动化处理；解耦数据获取与业务逻辑，支持数据源扩展。

---

## 2.3 靶点组合竞争格局模块

### 模块说明
计算并展示靶点组合在不同疾病领域的竞争格局矩阵。

### 子模块
| 子模块 | 功能描述 |
|--------|----------|
| 疾病筛选 | 支持多疾病组合查询 |
| 研发阶段筛选 | 支持多阶段过滤 |
| 矩阵计算 | 靶点对分值计算 |
| 结果导出 | Excel导出 |

### 输入输出
**输入**：疾病列表、阶段列表、筛选选项
**输出**：靶点列表、矩阵数据、分值映射

**性能指标**：
- 查询响应时间 < 2秒（缓存命中 < 100ms）
- 支持最多50个靶点的矩阵计算

**安全约束**：
- 需要认证访问
- 单用户QPS限流 ≤ 10
- 查询日志审计

### 为什么这么设计/选型
核心算法计算任意两个靶点在同一疾病下的最高阶段分值；二维矩阵可视化，颜色编码快速识别竞争热度；支持悬浮查看药品详情。

---

## 2.4 靶点研发进展格局模块

### 模块说明
按疾病或靶点维度展示研发管线分布情况。

### 子模块
| 子模块 | 功能描述 |
|--------|----------|
| 疾病视图 | 展示选定疾病下各靶点管线 |
| 靶点视图（预留） | 展示选定靶点跨疾病管线 |
| 管线历史视图（预留） | 展示管线演进时间轴 |

### 输入输出
**输入**：疾病名称/靶点列表、筛选条件
**输出**：管线分布数据、药品卡片列表

**性能指标**：
- 查询响应时间 < 1.5秒
- 支持模糊搜索、分页加载

**安全约束**：
- 需要认证访问
- 查询参数严格校验

### 为什么这么设计/选型
疾病视图展示选定疾病下各靶点的研发管线分布；表格化展示不同研发阶段的药品卡片；支持靶点级联筛选、模糊查询、全选操作。

---

## 2.5 查询配置管理模块

### 模块说明
管理用户的查询预设和筛选条件配置。

### 子模块
| 子模块 | 功能描述 |
|--------|----------|
| 预设筛选 | 按治疗领域预设条件 |
| 自定义筛选 | 用户自定义配置 |
| 筛选保存/加载 | 持久化存储 |

### 输入输出
**输入**：预设名称、筛选条件（疾病、阶段等）
**输出**：预设列表、保存状态

**性能指标**：
- 预设加载响应时间 < 200ms
- 支持最多100个用户预设

**安全约束**：
- 用户隔离（仅访问自己的预设）
- 系统预设只读

### 为什么这么设计/选型
支持按治疗领域（GI、Dermatology等）预设筛选条件；用户可保存常用查询条件，提升复用效率。

---

## 2.6 AI助手模块（预留）

### 模块说明
未来集成大语言模型，提供智能问答和文档分析能力。

### 子模块
| 子模块 | 功能描述 |
|--------|----------|
| 百科问答 | 靶点/疾病知识问答 |
| 文档精读 | 文档摘要和关键信息提取 |
| 知产报告 | 专利分析报告生成 |
| 序列推荐 | 基于序列相似性推荐 |
| 医学智能 | 医学文本智能分析 |

### 输入输出
**输入**：用户问题、文档内容、查询条件
**输出**：答案、摘要、报告

**性能指标**：
- 问答响应时间 < 5秒
- 支持流式输出

**安全约束**：
- 敏感数据脱敏
- 输出内容审核

### 为什么这么设计/选型
预留智能体接口，未来集成大语言模型能力；原型已包含Chatbot FAB按钮。

---

# 三、系统架构图

## 3.1 模块依赖图

**以下图表展示核心模块之间的依赖关系：**

```mermaid
graph TD
    subgraph "前端层"
        UI[Vue3 + TypeScript]
        CHAT[Chatbot FAB]
    end
    
    subgraph "API网关层"
        NGINX[Nginx反向代理]
    end
    
    subgraph "应用服务层"
        AUTH[用户认证模块]
        DATA_INGEST[数据接入模块]
        TARGET_COMBO[靶点组合竞争格局模块]
        TARGET_PROGRESS[靶点研发进展格局模块]
        QUERY_CONFIG[查询配置管理模块]
        AI_MODULE[AI助手模块-预留]
    end
    
    subgraph "数据处理层"
        CELERY[Celery任务队列]
        WORKER1[Worker-数据解析]
        WORKER2[Worker-预计算]
    end
    
    subgraph "数据存储层"
        PG[(PostgreSQL)]
        REDIS[(Redis)]
        ES[(Elasticsearch)]
        OSS[阿里云OSS]
    end
    
    subgraph "基础设施层"
        RBMQ[RabbitMQ]
    end
    
    %% 前端依赖
    UI --> NGINX
    CHAT --> AI_MODULE
    
    %% API网关路由
    NGINX --> AUTH
    NGINX --> TARGET_COMBO
    NGINX --> TARGET_PROGRESS
    NGINX --> QUERY_CONFIG
    NGINX --> AI_MODULE
    
    %% 模块间依赖
    TARGET_COMBO --> AUTH
    TARGET_COMBO --> QUERY_CONFIG
    TARGET_COMBO --> REDIS
    TARGET_COMBO --> PG
    
    TARGET_PROGRESS --> AUTH
    TARGET_PROGRESS --> REDIS
    TARGET_PROGRESS --> PG
    
    QUERY_CONFIG --> AUTH
    QUERY_CONFIG --> PG
    
    AI_MODULE --> AUTH
    AI_MODULE --> PG
    AI_MODULE --> REDIS
    
    %% 数据接入依赖
    DATA_INGEST --> RBMQ
    DATA_INGEST --> OSS
    CELERY --> DATA_INGEST
    CELERY --> WORKER1
    CELERY --> WORKER2
    
    WORKER1 --> PG
    WORKER2 --> REDIS
    
    %% 数据存储依赖
    PG --> ES
    PG --> REDIS
    
    %% 样式
    classDef frontend fill:#e1f5fe,stroke:#01579b,stroke-width:2px
    classDef api fill:#fff3e0,stroke:#e65100,stroke-width:2px
    classDef service fill:#f3e5f5,stroke:#4a148c,stroke-width:2px
    classDef process fill:#e8f5e9,stroke:#1b5e20,stroke-width:2px
    classDef storage fill:#fce4ec,stroke:#880e4f,stroke-width:2px
    classDef infra fill:#efebe9,stroke:#3e2723,stroke-width:2px
    
    class UI,CHAT frontend
    class NGINX api
    class AUTH,DATA_INGEST,TARGET_COMBO,TARGET_PROGRESS,QUERY_CONFIG,AI_MODULE service
    class CELERY,WORKER1,WORKER2 process
    class PG,REDIS,ESS,OSS storage
    class RBMQ infra
```

## 3.2 数据流图

**以下图表展示从数据源到用户查询的完整数据流转：**

```mermaid
flowchart TD
    subgraph "数据源"
        OSS_DATA[医药魔方 OSS推送<br/>parquet文件]
    end
    
    subgraph "数据摄入流程"
        SCHEDULER[定时任务<br/>每天5:00]
        OSS_LISTENER[OSS文件监听器]
        PARQUET_PARSE[Parquet解析器<br/>PyArrow]
        DATA_CLEAN[数据清洗与校验]
        DEDUP[去重处理<br/>nct_id=highest_trial_id]
        PHASE_MAP[阶段分值映射]
        BATCH_INSERT[批量入库]
    end
    
    subgraph "预计算流程"
        PRECALC_TRIGGER[预计算触发器]
        HOT_QUERY_CACHE[热点查询预计算]
        REDIS_CACHE[写入Redis缓存]
    end
    
    subgraph "用户查询流程"
        USER_REQ[用户请求]
        AUTH_CHECK[认证校验]
        CACHE_LOOKUP[缓存查询<br/>Redis]
        DB_QUERY[数据库查询<br/>PostgreSQL]
        RESULT_CALC[结果计算<br/>矩阵/管线]
        RESPONSE[返回响应]
    end
    
    subgraph "存储"
        CI_DB[(ci_tracking_info)]
        DRUG_DB[(drug_pipeline_info)]
        REDIS_L2[(Redis L2缓存)]
        REDIS_L3[(Redis L3缓存)]
    end
    
    %% 数据摄入流
    OSS_DATA -->|推送| OSS_LISTENER
    SCHEDULER -->|触发| OSS_LISTENER
    OSS_LISTENER --> PARQUET_PARSE
    PARQUET_PARSE --> DATA_CLEAN
    DATA_CLEAN --> DEDUP
    DEDUP --> PHASE_MAP
    PHASE_MAP --> BATCH_INSERT
    BATCH_INSERT --> CI_DB
    BATCH_INSERT --> DRUG_DB
    
    %% 预计算流
    BATCH_INSERT -->|数据更新事件| PRECALC_TRIGGER
    PRECALC_TRIGGER --> HOT_QUERY_CACHE_CACHE: 写入L2缓存
    HOT_QUERY_CACHE --> REDIS_L3
    HOT_QUERY_CACHE --> REDIS_L2
    
    %% 用户查询流
    USER_REQ --> AUTH_CHECK
    AUTH_CHECK --> CACHE_LOOKUP
    CACHE_LOOKUP -->|未命中| DB_QUERY
    CACHE_LOOKUP -->|命中| RESPONSE
    DB_QUERY --> CI_DB
    DB_QUERY --> DRUG_DB
    CI_DB --> RESULT_CALC
    DRUG_DB --> RESULT_CALC
    RESULT_CALC --> RESPONSE
    RESPONSE -->|更新缓存| REDIS_L2
    
    %% 样式
    classDef source fill:#e3f2fd,stroke:#1565c0,stroke-width:2px
    classDef ingest fill:#fff9c4,stroke:#f9a825,stroke-width:2px
    classDef precalc fill:#e8f5e9,stroke:#2e7d32,stroke-width:2px
    classDef query fill:#fce4ec,stroke:#c62828,stroke-width:2px
    classDef db fill:#f3e5f5,stroke:#6a1b9a,stroke-width:2px
    
    class OSS_DATA source
    class SCHEDULER,OSS_LISTENER,PARQUET_PARSE,DATA_CLEAN,DEDUP,PHASE_MAP,BATCH_INSERT ingest
    class PRECALC_TRIGGER,HOT_QUERY_CACHE,REDIS_CACHE precalc
    class USER_REQ,AUTH_CHECK,CACHE_LOOKUP,DB_QUERY,RESULT_CALC,RESPONSE query
    class CI_DB,DRUG_DB,REDIS_L2,REDIS_L3 db
```

## 3.3 关键业务时序图

### 3.3.1 靶点组合竞争格局查询时序

```mermaid
sequenceDiagram
    autonumber
    participant User as 用户浏览器
    participant Nginx as Nginx
    participant API as FastAPI服务
    participant Auth as 认证中间件
    participant Redis as Redis缓存
    participant DB as PostgreSQL
    participant RedisL3 as Redis热点缓存
    
    Note over User,RedisL3: 场景：用户查询GI领域靶点组合竞争格局
    
    User->>Nginx: POST /api/v1/target-combo/query<br/>{diseases, phases}
    Nginx->>API: 转发请求
    
    API->>Auth: 验证JWT Token
    Auth-->>API: Token有效, 返回用户信息
    
    API->>Redis: 检查L2缓存<br/>(完整查询参数hash)
    
    alt 缓存命中
        Redis-->>API: 返回缓存结果
        API-->>Nginx: 返回矩阵数据
        Nginx-->>User: 渲染竞争格局矩阵
    else 缓存未命中
        API->>RedisL3: 检查L3热点缓存<br/>(疾病组合预计算)
        
        alt 热点缓存命中
            RedisL3-->>API: 返回预计算结果
        else 热点缓存未命中
            API->>DB: 查询ci_tracking_info<br/>WHERE disease_names IN [...]<br/>AND phase IN [...]<br/>AND nct_id=highest_trial_id
            DB-->>API: 返回药品记录列表
            
            API->>API: 计算靶点组合矩阵<br/>遍历所有靶点对(t1, t2)<br/>查找同一疾病下的最高分值
        end
        
        API->>API: 构建响应数据<br/>targets[], matrix[]
        
        API->>Redis: 写入L2缓存<br/>TTL=10分钟
        Redis-->>API: 缓存写入成功
        
        API-->>Nginx: 返回矩阵数据
        Nginx-->>User: 渲染竞争格局矩阵<br/>显示颜色编码的分数格子
    end
    
    User->>User: 悬浮点击分数格子
    User->>API: GET /api/v1/drug/detail?nct_id=xxx
    API->>DB: 查询药品详情
    DB-->>API: 返回药品详细信息
    API-->>User: 显示悬浮卡片<br/>{drug_name, company_names, nct_id, phase}
```

### 3.3.2 数据摄入与处理时序

```mermaid
sequenceDiagram
    autonumber
    participant OSS as 阿里云OSS
    participant Scheduler as 定时调度器
    participant Listener as OSS监听器
    participant RBMQ as RabbitMQ
    participant Worker as Celery Worker
    participant Parser as Parquet解析器
    participant Validator as 数据校验器
    participant Transformer as 数据转换器
    participant DB as PostgreSQL
    participant Precalc as 预计算服务
    participant Redis as Redis缓存
    participant Alert as 告警服务
    
    Note over OSS,Alert: 场景：每天5点医药魔方推送最新数据
    
    OSS->>Scheduler: 推送3个parquet文件<br/>- ci_tracking_info<br/>- drug_pipeline_info<br/>- clinical_detail
    Scheduler->>Listener: 触发文件监听任务
    Listener->>OSS: 获取文件列表
    
    loop 遍历每个parquet文件
        Listener->>RBMQ: 发布数据摄入任务<br/>{file_path, file_type}
        RBMQ->>Worker: 任务队列消费
        
        Worker->>Parser: 读取Parquet文件<br/>PyArrow流式读取
        Parser-->>Worker: 返回RecordBatch
        
        Worker->>Validator: 校验字段完整性<br/>检查必填字段
        Validator-->>Worker: 校验结果
        
        alt 校验通过
            Worker->>Transformer: 数据转换<br/>- JSON字段序列化<br/>- 阶段分值映射<br/>- 去重处理
            Transformer-->>Worker: 转换后数据
            
            Worker->>DB: 批量INSERT/UPDATE<br/>UPSERT操作
            DB-->>Worker: 写入成功
            

            
            alt 新增或更新数据
                Worker->>Precalc: 触发预计算任务
                Precalc->>DB: 查询热点疾病组合
                DB-->>Precalc: 返回查询结果
                Precalc->>Precalc: 计算靶点组合矩阵
                Precalc->>Redis: 写入L3热点缓存
                Redis-->>Precalc: 缓存更新成功
            end
        else 校验失败
            Worker->>Alert: 发送数据质量告警<br/>{file_path, error_details}
            Alert-->>Worker: 告警已发送
            Worker->>Worker: 记录错误日志
        end
    end
    
    Worker->>DB: 生成数据质量报告<br/>{total_records, success_count, error_count}
    DB-->>Worker: 报告保存成功
    Worker->>Alert: 发送数据入库完成通知
```

---

# 四、技术栈选型

## 4.1 后端技术栈

| 组件 | 选型 | 设计理由 |
|------|------|----------|
| 语言 | Java 17+ | 企业级应用主流语言，生态成熟，适合医药行业高可靠需求 |
| Web框架 | Spring Boot 3.x | 微服务框架，自动配置，开箱即用，社区活跃 |
| 数据访问 | Spring Data JPA + Hibernate | 简化数据访问，支持实体映射和查询 |
| 任务调度 | Spring Scheduler | 内置任务调度，支持Cron表达式，处理定时数据摄入 |
| 异步处理 | @Async + ThreadPoolExecutor | 异步任务处理，提高并发能力 |
| 文档处理 | Apache POI + PyArrow JDBC | 处理Parquet文件（通过PyArrow JDBC）和Excel导出 |
| 对象映射 | MapStruct | 类型安全的对象映射，减少样板代码 |
| 参数校验 | Jakarta Validation API | 标准参数校验，自动触发校验规则 |
| API文档 | SpringDoc OpenAPI | 自动生成OpenAPI 3.0文档，Swagger UI集成 |
| 日志 | SLF4J + Logback | 高性能日志框架，支持异步日志输出 |
| 监控 | Spring Boot Actuator + Micrometer | 健康检查、指标监控、链路追踪 |

## 4.2 前端技术栈

| 组件 | 选型 | 设计理由 |
|------|------|----------|
| 框架 | React 19 + TypeScript | 企业级前端框架，类型安全，适合复杂交互（矩阵悬浮、筛选联动） |
| UI库 | Ant Design 5.x | 企业级UI组件库，组件丰富，设计统一，表格、筛选、弹窗等组件支持完善 |
| HTTP客户端 | Axios | Promise-based HTTP客户端，支持请求拦截、响应拦截，便于统一处理 |
| 状态管理 | Zustand / Redux Toolkit | 轻量级状态管理（Zustand）或标准状态管理（Redux Toolkit），支持持久化 |
| 图表 | ECharts + React ECharts | 二维矩阵可视化，支持自定义交互，React集成友好 |
| 路由 | React Router 6.x | 官方路由库，支持路由守卫、嵌套路由 |
| 表单 | React Hook Form | 高性能表单库，与Zod/Yup集成，支持复杂验证 |
| UI组件增强 | ahooks | React Hooks工具库，提供常用Hooks，减少代码重复 |
| 构建工具 | Vite 5.x | 快速冷启动，HMR性能优异，构建速度快 |
| 代码规范 | ESLint + Prettier | 代码风格统一，自动格式化 |
| 类型检查 | TypeScript 5.x | 静态类型检查，提前发现错误，提高代码质量 |

## 4.3 数据库选型

| 组件 | 选型 | 设计理由 |
|------|------|----------|
| 主数据库 | PostgreSQL 14+ | 支持JSONB存储靶点数组、全文搜索、复杂查询优化 |
| 缓存 | Redis 7.0 | 热点数据缓存、会话管理、任务队列 |
| 搜索引擎 | Elasticsearch 8.x（可选） | 支持药品名称、疾病名称的模糊搜索，高并发检索 |

## 4.4 中间件选型

| 组件 | 选型 | 设计理由 |
|------|------|----------|
| 消息队列 | RabbitMQ / Apache Kafka | 可靠的消息投递，RabbitMQ适合任务队列，Kafka适合高吞吐日志 |
| 对象存储 | 阿里云OSS | 原始数据存储，已对接医药魔方数据源，Java SDK支持完善 |
| 反向代理 | Nginx | 静态资源服务、负载均衡、SSL终端，支持WebSocket |
| 日志收集 | ELK Stack（Elasticsearch + Logstash + Kibana） | 集中日志收集与分析，支持Logback Kafka Appender |
| 分布式追踪 | Zipkin / SkyWalking | 微服务调用链路追踪，性能分析 |
| 任务调度 | Spring ShedLock | 分布式任务调度锁，防止多实例重复执行定时任务 |
| 服务注册发现 | Nacos / Consul | 微服务注册与发现，配置中心（如采用微服务架构） |

---

# 五、核心数据模型设计

## 5.1 核心表结构

### 5.1.1 ci_tracking_info（竞争情报追踪表）

**为什么这么设计/选型**：
- 映射自 `pharmcube2harbour_ci_tracking_info_0.parquet`
- 采用JSONB存储 `targets`、`company_names` 等数组字段，便于查询
- 添加索引优化常用查询（disease_names、phase_revised、drug_id）

**表结构**：

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
CREATE INDEX idx_ci_tracking_highest_trial ON ci_tracking_info(highest_trial_id);
```

### 5.1.2 drug_pipeline_info（药品管线信息表）

**为什么这么设计/选型**：
- 映射自 `pharmcube2harbour_drug_pipeline_info_0.parquet`
- 预留用于后续功能，当前阶段主要查询 ci_tracking_info 表

**表结构**：

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

-- 索引
CREATE INDEX idx_drug_pipeline_drug_id ON drug_pipeline_info(drug_id);
CREATE INDEX idx_drug_pipeline_diseases ON drug_pipeline_info USING GIN(diseases);
CREATE INDEX idx_drug_pipeline_targets ON drug_pipeline_info USING GIN(targets);
```

### 5.1.3 users（用户表）

**为什么这么设计/选型**：
- 支持账号密码登录
- 预留角色权限控制

**表结构**：

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

-- 索引
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email ON users(email);
```

### 5.1.4 query_presets（查询预设表）

**为什么这么设计/选型**：
- 保存用户的自定义筛选条件
- 支持系统预设和用户自定义

**表结构**：

```sql
CREATE TABLE query_presets (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL,
    is_system_default BOOLEAN DEFAULT FALSE,
    preset_data JSONB NOT NULL,  -- 存储筛选条件（疾病列表、阶段列表等）
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

-- 索引
CREATE INDEX idx_presets_user_id ON query_presets(user_id);
CREATE INDEX idx_presets_system ON query_presets(is_system_default);
```

## 5.2 阶段分值映射表

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

# 六、关键接口设计

## 6.1 认证接口

### 6.1.1 用户登录

**接口信息**：
- **请求方法**：`POST`
- **接口URL**：`/api/v1/auth/login`
- **认证方式**：无需认证
- **功能描述**：用户账号密码登录，返回JWT Token

**请求参数**：

```json
{
  "username": "user001",
  "password": "encrypted_password"
}
```

| 字段名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| username | string | 是 | 用户名，最长50字符 |
| password | string | 是 | 密码（前端加密传输） |

**响应字段**：

**成功响应（200 OK）**：
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

| 字段名 | 类型 | 说明 |
|--------|------|------|
| code | int | 响应码 |
| message | string | 响应消息 |
| data.access_token | string | JWT访问令牌 |
| data.token_type | string | 令牌类型，固定为"Bearer" |
| data.expires_in | int | 令牌过期时间（秒） |
| data.user.id | int | 用户ID |
| data.user.username | string | 用户名 |
| data.user.full_name | string | 用户全名 |
| data.user.role | string | 用户角色（admin/user/guest） |

**错误响应**：

| HTTP状态码 | 错误码 | 说明 |
|-----------|--------|------|
| 400 | 4001 | 参数缺失或格式错误 |
| 401 | 4011 | 用户名或密码错误 |
| 403 | 4031 | 账号已被禁用 |
| 500 | 5001 | 服务器内部错误 |

---

# 七、安全设计

## 7.1 认证机制

**为什么这么设计/选型**：
- 采用JWT（JSON Web Token）无状态认证，无需服务端存储会话
- Token有效期2小时，支持刷新Token机制
- 密码采用bcrypt加盐哈希存储，安全性高

**认证流程**：
1. 用户提交用户名和密码
2. 服务端验证密码（bcrypt对比）
3. 生成Token（包含用户ID、角色、过期时间）
4. 客户端在后续请求的Header中携带Token：`Authorization: Bearer {token}`

**安全约束**：
- Token签名使用HS256算法，密钥长度至少32字符
- Token过期后后要重新登录或使用refresh_token刷新
- 支持Token黑名单机制（用户登出时将Token加入黑名单）

## 7.2 授权机制

**为什么这么设计/选型**：
- 基于RBAC（Role-Based Access Control），角色权限清晰
- 角色层级：admin > user > guest
- 接口级别权限校验（@permission_required装饰器）

**角色权限定义**：

| 角色 | 权限范围 |
|------|----------|
| admin | 全部权限（包括用户管理、系统配置） |
| user | 查询权限、预设管理权限 |
| guest | 仅查询权限（无法保存预设） |

**权限控制策略**：
- 接口路由声明所需角色：`@router.get("/api/admin/users", permissions=["admin"])`
- JWT Token解析后从payload中获取用户角色
- 权限校验失败返回403 Forbidden

## 7.3 数据加密

**为什么这么设计/选型**：
- 传输层：HTTPS（TLS 1.2+），防止中间人攻击
- 存储层：敏感字段（密码）bcrypt哈希，不可逆
- 数据库连接：SSL加密，防止数据泄露

**加密配置**：
- SSL证书：Let's Encrypt自动续期
- TLS版本：仅支持TLS 1.2、TLS 1.3
- 加密套件：ECDHE-RSA-AES128-GCM-SHA256、ECDHE-RSA-AES256-GCM-SHA384

## 7.4 审计日志

**为什么这么设计/选型**：
- 记录用户登录、查询、数据修改操作，便于问题追溯
- 日志包含：用户ID、操作类型、IP、时间戳、请求参数
- 支持日志脱敏（隐藏敏感字段）

**日志格式**：

```json
{
  "timestamp": "2026-03-30T10:00:00Z",
  "user_id": 1,
  "username": "user001",
  "action": "QUERY_TARGET_COMBO",
  "request_id": "uuid-xxx",
  "ip": "192.168.1.100",
  "user_agent": "Mozilla/5.0...",
  "params": {
    "disease_names": ["Acne"],
    "phases": ["Phase II"]
  },
  "response_time": 1200,
  "status": "success"
}
```

**日志保留策略**：
- 实时日志：保留7天
- 审计日志：保留90天
- 异常日志：永久保留

---

# 八、性能设计

## 8.1 吞吐量目标

**为什么这么设计/选型**：
基于用户规模估算（20-30并发用户），设定合理的性能目标。

| 指标 | 目标值 | 说明 |
|------|--------|------|
| QPS | 100+ | 每秒请求数 |
| 靶点组合查询响应时间 | < 2秒 | 矩阵计算 |
| 靶点进展查询响应时间 | < 1.5秒 | 管线查询 |
| 缓存命中响应时间 | < 100ms | Redis缓存 |
| 数据摄入处理速度 | > 1000条/秒 | Parquet入库 |

## 8.2 延迟优化策略

**为什么这么设计/选型**：
- 数据库连接池：最大连接数50，预加载常用查询，减少连接建立开销
- 异步IO：FastAPI异步处理请求，提高并发能力
- 数据预计算：定时任务预计算热点查询结果，减少实时计算

**优化配置**：
```java
// application.yml
spring:
  application:
    name: apex-target-intelligence
  datasource:
    url: jdbc:postgresql://localhost:5432/apex
    username: apex_user
    password: ${DB_PASSWORD}
    hikari:
      maximum-pool-size: 50
      minimum-idle: 20
      connection-timeout: 30000
      max-lifetime: 600000
      pool-name: ApexHikariCP
  
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        format_sql: true
        jdbc:
          batch_size: 50
        order_inserts: true
        order_updates: true

server:
  port: 8080
  tomcat:
    threads:
      max: 200
      min-spare: 10
```

## 8.3 缓存策略

**为什么这么设计/选型**：
多层级缓存设计，减少数据库压力，提高响应速度。

**缓存分层**：

| 缓存层级 | 缓存对象 | TTL | 失效策略 |
|---------|---------|-----|---------|
| Redis L1 | 用户会话 | 2小时 | 主动过期 |
| Redis L2 | 查询结果（完整参数） | 10分钟 | 主动过期 |
| Redis L3 | 热点疾病列表 | 1小时 | 主动过期 |
| PostgreSQL | 靶点组合矩阵（预计算） | 24小时 | 定时刷新 |

**缓存键设计**：
```
L1: session:{token}
L2: query:target-combo:{hash(params)}
L3: hot-query:{disease_combo_hash}
```

**缓存击穿防护**：
- 使用Redis SETNX分布式锁，防止缓存失效时并发查询数据库
- 永久缓存基础数据（疾病列表、靶点列表）

**缓存伪代码**：
```java
@Service
@RequiredArgsConstructor
public class CacheService {
    
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;
    
    public <T> T getWithCache(String key, Supplier<T> fetchFunc, long ttlSeconds) {
        // 1. 尝试从缓存获取
        String cached = redisTemplate.opsForValue().get(key);
        if (cached != null) {
            try {
                return objectMapper.readValue(cached, new TypeReference<T>() {});
            } catch (Exception e) {
                log.error("Cache deserialize error", e);
            }
        }
        
        // 2. 分布式锁防止缓存击穿
        String lockKey = "lock:" + key;
        boolean locked = redisTemplate.opsForValue().setIfAbsent(lockKey, "1", 10, TimeUnit.SECONDS);
        
        if (locked) {
            try {
                // 3. 执行查询
                T data = fetchFunc.get();
                String json = objectMapper.writeValueAsString(data);
                redisTemplate.opsForValue().set(key, json, ttlSeconds, TimeUnit.SECONDS);
                return data;
            } catch (Exception e) {
                throw new RuntimeException("Fetch data error", e);
            } finally {
                redisTemplate.delete(lockKey);
            }
        }
        
        // 4. 等待其他线程获取数据
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return getWithCache(key, fetchFunc, ttlSeconds);
    }
}
```

## 8.4 数据库优化

**为什么这么设计/选型**：
- 复合索引：`(drug_id, indication_top_global_latest_stage)`，覆盖常用查询
- GIN索引：JSONB字段（disease_names、targets、company_names），支持数组查询
- 分页查询：使用Cursor-based分页，避免OFFSET性能问题
- 读写分离：查询走只读副本，写入走主库

**索引优化策略**：
```sql
-- 复合索引
CREATE INDEX idx_ci_drug_stage ON ci_tracking_info(drug_id, indication_top_global_latest_stage);

-- GIN索引（JSONB）
CREATE INDEX idx_ci_disease_names ON ci_tracking_info USING GIN(disease_names);

-- 部分索引
CREATE INDEX idx_ci_active ON ci_tracking_info(nct_id) WHERE nct_id IS NOT NULL;

-- 函数索引
CREATE INDEX idx_ci_stage_score ON ci_tracking_info (
    CASE indication_top_global_latest_stage
        WHEN '临床前' THEN 0.1
        WHEN '申报临床' THEN 0.5
        WHEN 'I期临床' THEN 1.0
        WHEN 'I/II期临床' THEN 1.5
        WHEN 'II期临床' THEN 2.0
        WHEN 'II/III期临床' THEN 2.5
        WHEN 'III期临床' THEN 3.0
        WHEN '申请上市' THEN 3.5
        WHEN '批准上市' THEN 4.0
        ELSE 0
    END
);
```

**查询优化**：
```sql
-- 使用EXPLAIN ANALYZE分析查询计划
EXPLAIN ANALYZE
SELECT * FROM ci_tracking_info
WHERE disease_names @> '["Acne"]'::jsonb
AND indication_top_global_latest_stage IN ('II期临床', 'III期临床')
ORDER BY drug_id;
```

---

# 九、部署架构与环境规划

## 9.1 部署架构图

**为什么这么设计/选型**：
- 采用微服务化部署，便于水平扩展
- 数据层与应用层分离，提高可靠性
- 引入消息队列解耦数据摄入与业务逻辑

**架构图**：

```mermaid
graph TB
    subgraph "客户端层"
        WEB[Web浏览器]
    end
    
    subgraph "接入层"
        NGINX[Nginx反向代理<br/>SSL终端 + 负载均衡]
    end
    
    subgraph "应用服务层"
        API1[FastAPI Worker 1]
        API API2[FastAPI Worker 2]
        API3[FastAPI Worker 3]
    end
    
    subgraph "任务处理层"
        CELERY[Celery Beat]
        WORKER1[Celery Worker 1<br/>数据解析]
        WORKER2[Celery Worker 2<br/>预计算]
    end
    
    subgraph "数据存储层"
        PG_MASTER[(PostgreSQL主库)]
        PG_SLAVE[(PostgreSQL从库)]
        REDIS[(Redis缓存+队列)]
        RBMQ[RabbitMQ]
    end
    
    subgraph "外部依赖"
        OSS[阿里云OSS<br/>医药魔方数据]
    end
    
    WEB --> NGINX
    NGINX --> API1
    NGINX --> API2
    NGINX --> API3
    
    API1 --> PG_SLAVE
    API2 --> PG_SLAVE
    API3 --> PG_SLAVE
    API1 --> REDIS
    API2 --> REDIS
    API3 --> REDIS
    
    CELERY --> RBMQ
    RBMQ --> WORKER1
    RBMQ --> WORKER2
    
    WORKER1 --> PG_MASTER
    WORKER1 --> OSS
    WORKER2 --> REDIS
    
    PG_MASTER -.数据复制.-> PG_SLAVE
```

## 9.2 环境规划

**为什么这么设计/选型**：
多环境隔离，降低发布风险。

| 环境 | 用途 | 配置 | 域名 |
|------|------|------|------|
| 开发环境 | 本地开发调试 | Docker Compose单机部署 | localhost:8000 |
| 测试环境 | 功能测试、集成测试 | 阿里云ECS 2核4G × 2 | test.apex.com |
| 预发布环境 | 性能测试、灰度发布 | 阿里云ECS 4核8G × 2 | staging.apex.com |
| 生产环境 | 正式服务 | 阿里云ECS 8核16G × 3 + RDS PostgreSQL + Redis | api.apex.com |

## 9.3 容器化部署

**为什么这么设计/选型**：
- 使用Docker封装应用，保证环境一致性
- 编排工具：Kubernetes（生产环境）或Docker Compose（开发/测试）
- 镜像仓库：阿里云容器镜像服务（ACR）

**Dockerfile示例**：

```dockerfile
FROM python:3.10-slim

WORKDIR /app

# 安装依赖
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# 复制代码
COPY . .

# 暴露端口
EXPOSE 8000

# 启动命令
CMD ["uvicorn", "main:app", "--host", "0.0.0.0", "--port", "8000"]
```

**docker-compose.yml示例**：

```yaml
version: '3.8'

services:
  api:
    build: .
    ports:
      - "8000:8000"
    environment:
      - DATABASE_URL=postgresql://user:pass@postgres:5432/apex
      - REDIS_URL=redis://redis:6379/0
    depends_on:
      - postgres
      - redis
  
  postgres:
    image: postgres:14
    environment:
      - POSTGRES_DB=apex
      - POSTGRES_USER=user
      - POSTGRES_PASSWORD=pass
    volumes:
      - postgres_data:/var/lib/postgresql/data
  
  redis:
    image: redis:7
    volumes:
      - redis_data:/data
  
  celery:
    build: .
    command: celery -A tasks worker --loglevel=info
    environment:
      - DATABASE_URL=postgresql://user:pass@postgres:5432/apex
      - REDIS_URL=redis://redis:6379/0
      - RABBITMQ_URL=amqp://guest:guest@rabbitmq:5672/
    depends_on:
      - postgres
      - redis
      - rabbitmq

volumes:
  postgres_data:
  redis_data:
```

## 9.4 CI/CD流程

**为什么这么设计/选型**：
- GitLab CI/CD流水线，自动化构建部署
- 流程：代码提交 → 单元测试 → 构建镜像 → 推送ACR → 自动部署到测试环境
- 生产环境需要人工触发或批准

**GitLab CI配置示例**：

```yaml
stages:
  - test
  - build
  - deploy

test:
  stage: test
  script:
    - pip install -r requirements.txt
    - pytest tests/

build:
  stage: build
  only:
    - main
    - develop
  script:
    - docker build -t $IMAGE_NAME:$CI_COMMIT_SHA .
    - docker push $IMAGE_NAME:$CI_COMMIT_SHA

deploy_test:
  stage: deploy
  only:
    - develop
  script:
    - kubectl set image deployment/apex-api api=$IMAGE_NAME:$CI_COMMIT_SHA -n test

deploy_prod:
  stage: deploy
  only:
    - main
  when: manual
  script:
    - kubectl set image deployment/apex-api api=$IMAGE_NAME:$CI_COMMIT_SHA -n prod production
```

---

# 十、风险点与 Mitigation 方案

## 10.1 数据质量风险

**风险描述**：
医药魔方推送的Parquet文件可能存在：
- 字段缺失或格式不一致
- 重复记录（同一drug_id有多条最新记录）
- 阶段映射规则变更

**Mitigation 方案**：
- **数据校验层**：入库前进行字段完整性字段校验，缺失字段填充默认值
- **去重策略**：按`(drug_id, disease_names)`分组，保留`nct_id = highest_trial_id`的记录
- **版本管理**：阶段映射规则配置化，支持动态调整
- **数据质量报告**：每日生成数据入库报告，异常数据告警

**数据校验代码示例**：
```python
def validate_ci_record(record: dict) -> tuple[bool, list[str]]:
    """校验CI记录，返回(是否有效, 错误列表)"""
    errors = []
    
    # 必填字段校验
    required_fields = ['drug_id', 'nct_id', 'drug_name_cn']
    for field in required_fields:
        if field not in record or record[field] is None:
            errors.append(f"Missing required field: {field}")
    
    # 格式校验
    if 'targets' in record and not isinstance(record['targets'], list):
        errors.append("targets should be a list")
    
    return len(errors) == 0, errors
```

## 10.2 性能瓶颈风险

**风险描述**：
- 靶点组合矩阵计算涉及笛卡尔积，数据量大时响应慢
- 并发查询可能导致数据库连接耗尽
- Parquet文件解析占用大量内存

**Mitigation 方案**：
- **预计算策略**：定时任务预计算热点查询（常见疾病组合）的结果，存入Redis
- **查询限流**：使用Redis + 滑动窗口算法，限制单用户QPS ≤ 10
- **分页加载**：前端分批加载矩阵数据，虚拟滚动
- **流式处理**：使用PyArrow流式读取Parquet，避免全量加载到内存

**预计算任务代码示例**：
```python
@celery_app.task
def precalculate_hot_queries():
    """预计算热点查询"""
    hot_disease_combos = [
        ["Acne", "Psoriasis"],
        ["GERD", "Gastritis"],
        ["Alcoholic Liver Disease", "NAFLD"]
    ]
    
    for diseases in hot_disease_combos:
        cache_key = f"hot-query:{hash(str(diseases))}"
        result = calculate_target_combo_matrix(diseases)
        redis.set(cache_key, json.dumps(result), ex=3600)
```

## 10.3 安全风险

**风险描述**：
- 未授权访问医药敏感数据
- SQL注入、XSS攻击
- 数据传输泄露

**Mitigation 方案**：
- **最小权限原则**：数据库用户仅授予SELECT权限
- **输入校验**：使用Pydantic严格校验请求参数
- **WAF防护**：部署Web应用防火墙（阿里云WAF）
- **安全审计**：定期扫描漏洞，启用HTTPS + HSTS

**输入校验示例**：
```python
from pydantic import BaseModel, Field

class TargetComboQueryRequest(BaseModel):
    disease_names: List[str] = Field(default=[], max_items=10)
    phases: List[str] = Field(default=[], max_items=9)
    hide_empty_target: bool = False
    hide_single_target: bool = False
    
    class Config:
        # 额外校验
        @validator('disease_names')
        def validate_diseases(cls, v):
            if any(len(d) > 200 for d in v):
                raise ValueError("Disease name too long")
            return v
```

## 10.4 可用性风险

**风险描述**：
- 数据库宕机导致服务不可用
- OSS访问失败导致数据摄入中断
- Celery任务堆积导致数据处理延迟

**Mitigation 方案**：
- **高可用架构**：PostgreSQL主从复制 + 自动故障转移
- **降级策略**：OSS访问失败时，从缓存返回旧数据 + 告警
- **监控告警**：Prometheus + Grafana监控资源使用，异常时发送钉钉告警
- **任务重试**：Celery任务配置指数退避重试机制

**监控指标**：
- 数据库连接池使用率
- Redis命中率
- API响应时间P95、P99
- Celery任务队列长度

## 10.5 扩展性风险

**风险描述**：
- 数据量增长导致查询性能下降
- 新功能需求增加系统复杂度

**Mitigation 方案**：
- **分库分表**：按`drug_id`哈希分表，水平扩展
- **读写分离**：查询走只读副本，减轻主库压力
- **服务拆分**：预留接口边界，未来可拆分为独立服务（如AI助手服务）
- **版本兼容**：API版本化管理（/api/v1/、/api/v2/），支持多版本共存

---

# 十一、附录

## 11.1 阶段分值映射代码示例

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

def calculate_combo_score(records: list) -> float:
    """计算靶点组合最高分值"""
    if not records:
        return 0.0
    
    scores = [get_phase_score(r.indication_top_global_latest_stage)[0] 
              for r in records if r.indication_top_global_latest_stage]
    return max(scores) if scores else 0.0
```

## 11.2 错误码定义

| 错误码 | 说明 |
|--------|------|
| 4001 | 参数缺失或格式错误 |
| 4011 | 用户名或密码错误 |
| 4012 | Token无效或已过期 |
| 4031 | 账号已被禁用 |
| 4032 | 无权限访问 |
| 4041 | 疾病不存在 |
| 4042 | 预设不存在 |
| 4091 | 预设名称已存在 |
| 5001 | 服务器内部错误 |
| 5002 | 矩阵计算失败 |

## 11.3 参考文档

- 需求文档-v1.0.0 - 飞书云文档.md
- apex_prototype.html（原型设计）
- 医药魔方数据推送规范（由数据提供方维护）

## 11.4 依赖版本清单

### 后端依赖（Maven pom.xml）

```xml
<project xmlns="http://maven.apache.org/POM/4.0.0">
    <modelVersion>4.0.0</modelVersion>
    <groupId>com.apex</groupId>
    <artifactId>target-intelligence-api</artifactId>
    <version>1.0.0</version>
    
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.0</version>
    </parent>
    
    <properties>
        <java.version>17</java.version>
    </properties>
    
    <dependencies>
        <!-- Spring Boot Starter -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-redis</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>
        
        <!-- JWT -->
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-api</artifactId>
            <version>0.12.5</version>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-impl</artifactId>
            <version>0.12.5</version>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-jackson</artifactId>
            <version>0.12.5</version>
        </dependency>
        
        <!-- Database -->
        <dependency>
            <groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId>
            <scope>runtime</scope>
        </dependency>
        
        <!-- Object Mapping -->
        <dependency>
            <groupId>org.mapstruct</groupId>
            <artifactId>mapstruct</artifactId>
            <version>1.5.5.Final</version>
        </dependency>
        <dependency>
            <groupId>org.mapstruct</groupId>
            <artifactId>mapstruct-processor</artifactId>
            <version>1.5.5.Final</version>
            <scope>provided</scope>
        </dependency>
        
        <!-- Apache Commons -->
        <dependency>
            <groupId>org.apache.commons</groupId>
            <artifactId>commons-lang3</artifactId>
        </dependency>
        
        <!-- API Documentation -->
        <dependency>
            <groupId>org.springdoc</groupId>
            <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
            <version>2.3.0</version>
        </dependency>
        
        <!-- Testing -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>
</project>
```

### 前端依赖（package.json）

```json
{
  "name": "apex-target-intelligence-frontend",
  "version": "1.0.0",
  "private": true,
  "dependencies": {
    "react": "^19.0.0",
    "react-dom": "^19.0.0",
    "typescript": "^5.3.3",
    "@types/react": "^19.0.0",
    "@types/react-dom": "^19.0.0",
    "antd": "^5.12.0",
    "@ant-design/icons": "^5.2.6",
    "axios": "^1.6.2",
    "echarts": "^5.4.3",
    "echarts-for-react": "^3.0.2",
    "react-router-dom": "^6.20.1",
    "zustand": "^4.4.7",
    "react-hook-form": "^7.49.2",
    "zod": "^3.22.4",
    "@hookform/resolvers": "^3.3.2",
    "ahooks": "^3.3.8"
  },
  "devDependencies": {
    "vite": "^5.0.8",
    "@vitejs/plugin-react": "^4.2.1",
    "eslint": "^8.55.0",
    "prettier": "^3.1.1",
    "@typescript-eslint/eslint-plugin": "^6.15.0",
    "@typescript-eslint/parser": "^6.15.0"
  }
}
```

---

**文档版本**：v1.0.0
**创建日期**：2026-03-30
**最后更新**：2026-03-30
**编写人**：InfCode
**审核状态**：待审核
