# Apex 数据同步模块

## 模块概述

数据同步模块（apex-data-sync）负责从阿里云 OSS 下载 Parquet 文件，解析数据并同步到 PostgreSQL 数据库。

## 功能特性

- 从阿里云 OSS 下载 Parquet 文件
- 使用 Apache Parquet 库解析数据
- 批量插入数据到 PostgreSQL
- 使用 XXL-Job 定时执行同步任务
- 支持 Redis 分布式锁防止重复同步
- 自动清理旧数据（保留最新 3 个批次）
- 完整的同步日志记录
- 提供 HTTP API 手动触发同步

## 技术栈

- Spring Boot 3.3.x
- Apache Parquet 1.14.x
- Aliyun OSS SDK 3.17.x
- XXL-Job 2.4.x
- MyBatis-Plus 3.5.x
- PostgreSQL 16.x
- Redis

## 目录结构

```
apex-data-sync/
├── src/main/java/com/harbourbiomed/apex/datasync
│   ├── config/
│   │   ├── OssProperties.java          # OSS 配置
│   │   └── XxlJobConfig.java            # XXL-Job 配置
│   ├── controller/
│   │   └── DataSyncController.java      # 数据同步 API
│   ├── entity/
│   │   ├── CiTrackingInfoEntity.java    # CI 追踪信息实体
│   │   └── DataSyncLog.java             # 同步日志实体
│   ├── job/
│   │   └── DataSyncJob.java             # XXL-Job 定时任务
│   ├── mapper/
│   │   ├── CiTrackingInfoMapper.java   # CI 追踪信息 Mapper
│   │   └── SyncLogMapper.java          # 同步日志 Mapper
│   ├── service/
│   │   ├── DataSyncService.java        # 数据同步服务
│   │   ├── OssService.java             # OSS 服务
│   │   ├── ParquetReaderService.java   # Parquet 读取服务
│   │   └── SyncLogService.java         # 同步日志服务
│   └── DataSyncApplication.java       # 应用启动类
├── src/main/resources/
│   ├── application.yml                  # 应用配置
│   ├── mapper/
│   │   ├── CiTrackingInfoMapper.xml   # CI 追踪信息 SQL 映射
│   │   └── SyncLogMapper.xml          # 同步日志 SQL 映射
│   └── sql/
│       └── data_sync.sql              # 数据库初始化脚本
└── pom.xml
```

## 配置说明

### application.yml

```yaml
# 阿里云 OSS 配置
aliyun:
  oss:
    endpoint: oss-cn-hangzhou.aliyuncs.com
    access-key-id: ${ALIYUN_OSS_ACCESS_KEY_ID}
    access-key-secret: ${ALIYUN_OSS_ACCESS_KEY_SECRET}
    bucket-name: pharmcube2harbour
    ci-tracking-parquet-path: pharmcube2harbour_ci_tracking_info_0.parquet
    local-temp-dir: /tmp/apex-sync

# XXL-Job 配置
xxl:
  job:
    admin:
      addresses: http://localhost:8080/xxl-job-admin
    executor:
      appname: apex-data-sync
      accessToken: ${XXL_JOB_ACCESS_TOKEN}
```

## API 接口

### 1. 手动触发同步

**请求**
```
POST /api/v1/data-sync/trigger
```

**响应**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "batchId": "550e8400-e29b-41d4-a716-446655440000"
  }
}
```

### 2. 获取最新同步状态

**请求**
```
GET /api/v1/data-sync/status
```

**响应**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "module": "ci_tracking",
    "syncBatchId": "550e8400-e29b-41d4-a716-446655440000",
    "startTime": "2024-01-01 10:00:00",
    "endTime": "2024-01-01 10:05:00",
    "status": "success",
    "recordCount": 1000,
    "errorMessage": null,
    "createdAt": "2024-01-01 10:00:00"
  }
}
```

### 3. 查询同步日志列表

**请求**
```
GET /api/v1/data-sync/logs?limit=10
```

**响应**
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 2,
      "module": "ci_tracking",
      "syncBatchId": "550e8400-e29b-41d4-a716-446655440001",
      "startTime": "2024-01-01 11:00:00",
      "endTime": "2024-01-01 11:05:00",
      "status": "success",
      "recordCount": 1050,
      "errorMessage": null,
      "createdAt": "2024-01-01 11:00:00"
    }
  ]
}
```

## XXL-Job 定时任务

### 任务配置

- **任务名称**: ciTrackingInfoSyncJob
- **执行器**: apex-data-sync
- **Cron 表达式**: 根据需求配置，例如：`0 0 2 * * ?`（每天凌晨 2 点执行）

### 任务日志

任务执行日志会记录到 XXL-Job 调度中心，包括：
- 任务开始时间
- 同步批次 ID
- 同步记录数量
- 执行状态
- 错误信息（如果失败）

## 数据同步流程

```
┌─────────────┐
│  XXL-Job   │
│   触发     │
└──────┬──────┘
       │
       ▼
┌─────────────┐
│  获取分布式  │
│     锁      │
└──────┬──────┘
       │
       ▼
┌─────────────┐
│  从 OSS 下载 │
│ Parquet 文件│
└──────┬──────┘
       │
       ▼
┌─────────────┐
│  解析       │
│ Parquet 数据 │
└──────┬──────┘
       │
       ▼
┌─────────────┐
│  批量插入   │
│   数据库    │
└──────┬──────┘
       │
       ▼
┌─────────────┐
│  清理旧数据 │
│（保留3批次） │
└──────┬──────┘
       │
       ▼
┌─────────────┐
│  记录同步   │
│     日志    │
└──────┬──────┘
       │
       ▼
┌─────────────┐
│  释放分布式  │
│     锁      │
└─────────────┘
```

## 数据库表

### data_sync_log（同步日志表）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGSERIAL | 主键 ID |
| module | VARCHAR(50) | 模块名称 |
| sync_batch_id | VARCHAR(50) | 同步批次 ID |
| start_time | TIMESTAMP | 开始时间 |
| end_time | TIMESTAMP | 结束时间 |
| status | VARCHAR(20) | 状态（running | success | failed） |
| record_count | BIGINT | 同步记录数量 |
| error_message | TEXT | 错误信息 |
| created_at | TIMESTAMP | 创建时间 |

### ci_tracking_info（CI 追踪信息表）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGSERIAL | 主键 ID |
| drug_id | VARCHAR(100) | 药物 ID |
| drug_name_en | VARCHAR(500) | 药物英文名称 |
| drug_name_cn | VARCHAR(500) | 药物中文名称 |
| targets | TEXT[] | 靶点数组 |
| targets_raw | TEXT | 靶点原始字符串 |
| disease_id | VARCHAR(100) | 疾病 ID |
| harbour_indication_name | VARCHAR(500) | 适应症名称 |
| ta | VARCHAR(100) | 治疗领域 |
| moa | VARCHAR(500) | 作用机制 |
| originator | VARCHAR(500) | 原研机构 |
| research_institute | VARCHAR(500) | 研究机构 |
| global_highest_phase | VARCHAR(100) | 最高研发阶段 |
| global_highest_phase_score | INTEGER | 最高研发阶段分值 |
| indication_top_global_latest_stage | VARCHAR(100) | 首要适应症最新阶段 |
| indication_top_global_start_date | VARCHAR(100) | 首要适应症开始日期 |
| highest_trial_id | VARCHAR(100) | 最高试验 ID |
| highest_trial_phase | VARCHAR(100) | 最高试验阶段 |
| nct_id | VARCHAR(100) | NCT 试验编号 |
| data_source | VARCHAR(100) | 数据来源 |
| sync_batch_id | VARCHAR(50) | 同步批次 ID |
| synced_at | TIMESTAMP | 同步时间 |
| created_at | TIMESTAMP | 创建时间 |

## 环境变量

```bash
# 阿里云 OSS 访问凭证
export ALIYUN_OSS_ACCESS_KEY_ID=your_access_key_id
export ALIYUN_OSS_ACCESS_KEY_SECRET=your_access_key_secret

# XXL-Job 访问令牌
export XXL_JOB_ACCESS_TOKEN=your_access_token
```

## 启动应用

```bash
cd apex-platform/apex-data-sync
mvn clean package
java -jar target/apex-data-sync-1.0.0-SNAPSHOT.jar
```

## 注意事项

1. **并发控制**: 使用 Redis 分布式锁，同一时间只能执行一次同步任务
2. **批量处理**: 每批插入 1000 条记录，避免内存溢出
3. **数据清理**: 自动清理旧批次数据，只保留最新的 3 个批次
4. **临时文件**: 下载的 Parquet 文件存储在临时目录，同步完成后自动删除
5. **错误处理**: 同步失败时，当前批次数据会回滚，不影响历史数据
6. **日志记录**: 所有关键操作都会记录日志，便于排查问题

## 故障排查

### 同步失败

1. 检查 OSS 配置是否正确
2. 检查数据库连接是否正常
3. 查看应用日志获取详细错误信息
4. 检查 Redis 连接是否正常

### 数据不一致

1. 查看 data_sync_log 表确认同步状态
2. 检查 sync_batch_id 是否正确
3. 验证 Parquet 文件格式是否正确

## 开发者

Harbour BioMed Apex Team
