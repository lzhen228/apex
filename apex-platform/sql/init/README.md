# Apex 平台数据库初始化脚本

## 概述

本目录包含 Apex 平台的 PostgreSQL 数据库初始化脚本，用于创建数据库表结构、索引和初始数据。

## 文件说明

| 文件名 | 说明 | 依赖 | 执行顺序 |
|--------|------|------|----------|
| `01-init-schema.sql` | 创建所有表、索引和约束 | 无 | 1 |
| `02-insert-initial-data.sql` | 插入初始数据（研发阶段分值映射等） | 01-init-schema.sql | 2 |

## 数据库要求

- **数据库类型**: PostgreSQL
- **最低版本**: 16.x
- **推荐版本**: 16.x 或更高
- **数据库名称**: `apex`

## 初始化步骤

### 方式一：使用 psql 命令行工具

1. **创建数据库**

```bash
# 连接到 PostgreSQL 服务器
psql -U postgres

# 在 psql 中执行
CREATE DATABASE apex;
\q
```

2. **执行初始化脚本**

```bash
# 执行 schema 初始化
psql -U postgres -d apex -f 01-init-schema.sql

# 执行初始数据插入
psql -U postgres -d apex -f 02-insert-initial-data.sql
```

### 方式二：在 Docker Compose 环境中使用

在 `docker-compose.yml` 中配置：

```yaml
services:
  postgres:
    image: postgres:16-alpine
    container_name: apex-postgres
    ports:
      - "5432:5432"
    environment:
      POSTGRES_DB: apex
      POSTGRES_USER: apex
      POSTGRES_PASSWORD: your_password_here
    volumes:
      - pgdata:/var/lib/postgresql/data
      - ./sql/init:/docker-entrypoint-initdb.d  # 自动执行初始化脚本
```

启动容器时，PostgreSQL 会自动执行 `/docker-entrypoint-initdb.d` 目录下的所有 `.sql` 文件（按字母顺序）。

### 方式三：在应用启动时通过 Flyway/Liquibase 迁移

推荐使用 Flyway 进行数据库版本管理：

1. 将 `01-init-schema.sql` 重命名为 `V1__init_schema.sql`
2. 将 `02-insert-initial-data.sql` 重命名为 `V2__insert_initial_data.sql`
3. 配置 Flyway 连接信息，应用启动时自动执行迁移

## 数据库对象说明

### 核心表

| 表名 | 说明 | 记录数估算 |
|------|------|-----------|
| `phase_score_mapping` | 研发阶段分值映射表（9个阶段） | 9 |
| `therapeutic_area` | 治疗领域维度表 | ~10-50 |
| `disease` | 疾病维度表 | ~100-500 |
| `sys_user` | 系统用户表 | ~10-50 |
| `ci_tracking_latest` | 竞争情报主表（查询宽表） | ~100,000+ |
| `filter_preset` | 筛选条件预设表 | ~100-500 |
| `drug_pipeline_info` | 药品管线信息表（预留） | 0（初始为空） |

### 重要索引

| 索引名 | 类型 | 用途 |
|--------|------|------|
| `idx_ci_tracking_targets` | GIN | 加速靶点数组查询（`targets @> '{TNF}'`） |
| `idx_ci_tracking_matrix` | B-tree | 加速矩阵查询（按治疗领域、疾病、阶段分值排序） |
| `idx_ci_tracking_disease_target` | B-tree | 支持按疾病和靶点组合过滤 |

### 研发阶段分值映射

| 阶段名称 | 分值 | 排序 | 颜色编码 |
|----------|------|------|----------|
| Approved | 4.0 | 1 | #10B981（绿色） |
| BLA | 3.5 | 2 | #06B6D4（青色） |
| Phase III | 3.0 | 3 | #3B82F6（蓝色） |
| Phase II/III | 2.5 | 4 | #6366F1（靛蓝色） |
| Phase II | 2.0 | 5 | #8B5CF6（紫色） |
| Phase I/II | 1.5 | 6 | #A855F7（紫粉色） |
| Phase I | 1.0 | 7 | #D946EF（粉红色） |
| IND | 0.5 | 8 | #F59E0B（橙色） |
| PreClinical | 0.1 | 9 | #6B7280（灰色） |

## 默认管理员账号

初始化后会创建一个默认管理员账号：

- **用户名**: `admin`
- **密码**: 需要在应用启动后通过管理接口设置（初始密码哈希为占位符）
- **角色**: `admin`
- **状态**: `active` (1)

## 注意事项

### 1. 字符编码

所有 SQL 文件使用 UTF-8 编码，确保中文字符正确显示。

### 2. 数据同步

- `therapeutic_area` 和 `disease` 表的示例数据仅用于演示
- 实际生产环境应通过 ETL 从医药魔方数据源同步
- `ci_tracking_latest` 表的数据完全依赖 ETL 同步

### 3. 数组类型使用

`ci_tracking_latest` 表的 `targets` 字段使用 PostgreSQL 的 `TEXT[]` 数组类型，支持：

```sql
-- 插入数据
INSERT INTO ci_tracking_latest (targets) VALUES ('{TNF,IL-17A,IL-23}');

-- 查询包含特定靶点的记录
SELECT * FROM ci_tracking_latest WHERE targets @> '{TNF}';

-- 展开数组查询
SELECT unnest(targets) AS target FROM ci_tracking_latest;
```

### 4. 影子表策略（用于全量同步）

为避免全量同步期间影响在线查询，建议使用影子表策略：

1. 创建 `ci_tracking_latest_shadow` 表（结构与主表相同）
2. 写入新数据到 shadow 表
3. 校验数据完整性
4. 在事务中原子切换：
   ```sql
   BEGIN;
   DROP TABLE ci_tracking_latest;
   ALTER TABLE ci_tracking_latest_shadow RENAME TO ci_tracking_latest;
   COMMIT;
   ```

## 验证初始化结果

执行以下 SQL 验证数据库初始化是否成功：

```sql
-- 检查表是否存在
SELECT table_name 
FROM information_schema.tables 
WHERE table_schema = 'public' 
ORDER BY table_name;

-- 检查研发阶段分值映射数据
SELECT * FROM phase_score_mapping ORDER BY sort_order;

-- 检查默认管理员
SELECT id, username, display_name, role, status 
FROM sys_user 
WHERE username = 'admin';

-- 检查索引
SELECT indexname, tablename 
FROM pg_indexes 
WHERE schemaname = 'public' 
ORDER BY tablename, indexname;
```

## 故障排查

### 问题 1: 编码错误

**症状**: 中文字符显示为乱码

**解决方案**:
```sql
-- 检查数据库编码
SELECT encoding, datcollate, datctype 
FROM pg_database 
WHERE datname = 'apex';

-- 应该看到 encoding = 6 (UTF8)
```

### 问题 2: 权限错误

**症状**: Permission denied for table...

**解决方案**:
```sql
-- 授予应用用户所有权限
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO apex;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO apex;
```

### 问题 3: 数组操作符不识别

**症状**: operator does not exist: text[] @> text[]

**解决方案**:
确保启用了 `intarray` 或相关扩展（虽然 `@>` 操作符是原生支持的，但某些场景可能需要）。

## 版本历史

| 版本 | 日期 | 说明 |
|------|------|------|
| v1.0.0 | 2026-03-30 | 初始版本，创建所有核心表和初始数据 |

## 相关文档

- [技术规范文档](../../TECH_SPEC.md) - 完整的技术规范和数据模型设计
- [需求文档](../../需求文档-v1.0.0.md) - 业务需求和功能说明

## 支持

如遇到问题，请联系 Apex 平台技术团队或查看项目文档。
