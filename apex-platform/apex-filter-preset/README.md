# 筛选条件预设模块 (apex-filter-preset)

## 模块概述

筛选条件预设模块用于管理用户的查询条件预设，允许用户保存和复用常用的筛选条件，提升使用体验。

## 功能特性

- ✅ 保存/更新筛选条件预设
- ✅ 按模块查询预设列表
- ✅ 删除预设
- ✅ 支持设置默认预设
- ✅ 用户数据隔离
- ✅ 支持 JSONB 条件存储

## 数据库表结构

### filter_preset 表

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | SERIAL | 主键 |
| user_id | INTEGER | 用户ID（外键） |
| name | |VARCHAR(100) | 预预设名称 |
| module | VARCHAR(50) | 模块标识（competition/progress） |
| conditions | JSONB | 筛选条件JSON |
| is_default | BOOLEAN | 是否默认预设 |
| created_at | TIMESTAMP | 创建时间 |
| updated_at | TIMESTAMP | 更新时间 |

## API 接口文档

### 1. 保存筛选条件预设

**接口地址：** `POST /api/v1/filter-presets`

**请求头：**
```
Authorization: Bearer {token}
Content-Type: application/json
```

**请求参数：**
```json
{
  "name": "TNF 靶点筛选",
  "module": "competition",
  "conditions": {
    "diseaseIds": [203, 204],
    "targets": ["TNF"],
    "phases": ["Approved", "Phase III"],
    "sortBy": "scorePhase DESC"
  },
  "isDefault": false
}
```

**响应示例：**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "name": "TNF 靶点筛选",
    "module": "competition",
    "conditions": {
      "diseaseIds": [203, 204],
      "targets": ["TNF"],
      "phases": ["Approved", "Phase III"],
      "sortBy": "scorePhase DESC"
    },
    "isDefault": false,
    "createdAt": "2026-03-30T14:30:00",
    "updatedAt": "2026-03-30T14:30:00"
  }
}
```

### 2. 获取预设列表

**接口地址：** `GET /api/v1/filter-presets?module=competition`

**请求头：**
```
Authorization: Bearer {token}
```

**路径参数：**
- `module`: 模块标识（必需）- competition 或 progress

**响应示例：**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "presets": [
      {
        "id": 1,
        "name": "TNF 靶点筛选",
        "module": "competition",
        "conditions": {
          "diseaseIds": [203, 204],
          "targets": ["TNF"],
          "phases": ["Approved", "Phase III"],
          "sortBy": "scorePhase DESC"
        },
        "isDefault": true,
        "createdAt": "2026-03-30T14:30:00",
        "updatedAt": "2026-03-30T14:30:00"
      }
    ]
  }
}
```

### 3. 删除预设

**接口地址：** `DELETE /api/v1/filter-presets/{id}`

**请求头：**
```
Authorization: Bearer {token}
```

**路径参数：**
- `id`: 预设ID

**响应示例：**
```json
{
  "code": 200,
  "message": "删除成功",
  "data": null
}
```

## 条件字段说明

### Competition 模块条件字段

| 字段名 | 类型 | 说明 |
|--------|------|------|
| diseaseIds | Integer[] | 疾病ID列表 |
| targets | String[] | 靶点列表 |
| phases | String[] | 研发阶段列表 |
| origins | String[] | 起源公司列表 |
| sortBy | String | 排序规则 |
| scorePhaseMin | Decimal | 最低阶段分值 |
| scorePhaseMax | Decimal | 最高阶段分值 |

### Progress 模块条件字段

| 字段名 | 类型 | 说明 |
|--------|------|------|
| diseaseId | Integer | 疾病ID |
| targets | String[] | 靶点列表 |
| phases | String[] | 研发阶段列表 |
| origins | String[] | 起源公司列表 |
| sortBy | String | 排序字段（targetName/drugCount/avgPhaseScore） |
| sortOrder | String | 排序方向（ASC/DESC） |

## 测试数据

可以使用以下 SQL 脚本插入测试数据：

```sql
-- 位于: apex-platform/sql/test/insert-filter-preset-test-data.sql
```

## Knife4j 文档

启动服务后访问：`http://localhost:8085/doc.html`

## 业务规则

1. **预设名称唯一性**：同一用户在同一模块下，预设名称必须唯一
2. **默认预设限制**：每个用户在每个模块下只能有一个默认预设
3. **用户数据隔离**：用户只能访问和操作自己的预设数据
4. **更新规则**：如果预设名称已存在，则更新现有预设；否则创建新预设
5. **排序规则**：预设列表按创建时间倒序返回

## 技术栈

- Spring Boot 3.3.x
- MyBatis-Plus 3.5.x
- PostgreSQL 16.x
- MapStruct 1.5.x
- Fastjson2 2.0.x
- Knife4j 4.5.0

## 项目结构

```
apex-filter-preset/
├── src/main/java/com/harbourbiomed/apex/filterpreset/
│   ├── controller/          # 控制器层
│   │   └── FilterPresetController.java
│   ├── service/            # 服务层
│   │   ├── FilterPresetService.java
│   │   └── impl/
│   │       └── FilterPresetServiceImpl.java
│   ├── mapper/            # 数据访问层
│   │   └── FilterPresetMapper.java
│   ├── entity/            # 实体类
│   │   └── FilterPreset.java
│   ├── vo/                # 视图对象
│   │   └── FilterPresetVO.java
│   ├── dto/               # 数据传输对象
│   │   ├── SavePresetRequest.java
│   │   └── PresetListResponse.java
│   ├── util/              # 工具类
│   │   └── SecurityUtil.java
│   └── FilterPresetApplication.java
├── src/main/resources/
│   └── application.yml
└── pom.xml
```

## 启动方式

```bash
# 进入项目目录
cd apex-platform/apex-filter-preset

# 启动服务
mvn spring-boot:run
```

## 注意事项

1. 确保 PostgreSQL 数据库已启动
2. 确保 filter_preset 表已创建（运行 init-schema.sql）
3. 确保 JWT 配置正确
4. 所有接口都需要认证，请求头必须携带有效的 JWT Token

## 开发者

Harbour BioMed
