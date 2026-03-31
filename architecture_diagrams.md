# Mermaid 架构图

## 1. 模块依赖图

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

## 2. 数据流图

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
    PRECALC_TRIGGER --> HOT_QUERY_CACHE
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

## 3. 关键时序图 - 靶点组合竞争格局查询

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
    API-->>User: 显示悬浮卡片<br/>{drug_name, company_names, nct_id, phase_date}
```

## 4. 关键时序图 - 数据摄入与处理

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
        
        Reader->>Validator: 校验字段完整性<br/>检查必填字段
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

## 使用说明

1. 复制任一图表的 Mermark 代码块
2. 打开 [Mermaid Live Editor](https://mermaid.live/)
3. 将代码粘贴到编辑器中
4. 即可查看渲染后的架构图
