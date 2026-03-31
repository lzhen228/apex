# Apex API 文档

> **版本**: v1.0.0 &nbsp;|&nbsp; **最后更新**: 2026-03-30

---

## 目录

- [API 概览](#api-概览)
- [认证机制](#认证机制)
- [接口列表](#接口列表)
  - [认证接口](#认证接口)
  - [疾病筛选接口](#疾病筛选接口)
  - [靶点组合竞争格局接口](#靶点组合竞争格局接口)
  - [靶点研发进展格局接口](#靶点研发进展格局接口)
  - [筛选条件预设接口](#筛选条件预设接口)
- [响应格式规范](#响应格式规范)
- [错误码说明](#错误码说明)
- [Swagger 文档访问地址](#swagger-文档访问地址)

---

## API 概览

### 基础信息

| 项目 | 说明 |
|------|------|
| Base URL | 开发: `http://localhost:8080/api/v1` |
| | 测试: `https://test-apex.harbourbiomed.com/api/v1` |
| | 生产: `https://ai-harby.harbourbiomed.com/api/v1` |
| 内容类型 | `application/json` |
| 字符编码 | `UTF-8` |
| 认证方式 | JWT Bearer Token |

### 请求示例

```http
GET /api/v1/diseases/tree HTTP/1.1
Host: localhost:8080
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
Content-Type: application/json
```

---

## 认证机制

### JWT 获取和使用

#### 1. 登录获取 Token

**接口**: `POST /api/v1/auth/login`

**请求参数**:

```json
{
  "username": "admin",
  "password": "password123"
}
```

**响应示例**:

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiIsImlhdCI6MTcwOTM2MjM4NCwiZXhwIjoxNzA5Mzc2Nzg0fQ.xxx",
    "refreshToken": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiIsImlhdCI6MTcwOTM2MjM4NCwiZXhwIjoxNzA5OTY3MTg0fQ.yyy",
    "user": {
      "id": 1,
      "username": "admin",
      "name": "管理员",
      "email": "admin@harbourbiomed.com"
    }
  }
}
```

#### 2. 使用 Token 访问 API

在请求头中添加 `Authorization`:

```http
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

#### 3. Token 刷新

**接口**: `POST /api/v1/auth/refresh`

**请求参数**:

```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiIsImlhdCI6MTcwOTM2MjM4NCwiZXhwIjoxNzA5OTY3MTg0fQ.yyy"
}
```

---

## 接口列表

### 认证接口

#### 1. 用户登录

**接口**: `POST /api/v1/auth/login`

**描述**: 用户登录获取访问令牌

**请求参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| username | String | 是 | 用户名 |
| password | String | 是 | 密码 |

**响应示例**:

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
    "user": {
      "id": 1,
      "username": "admin",
      "name": "管理员",
      "email": "admin@harbourbiomed.com"
    }
  }
}
```

#### 2. 刷新令令牌

**接口**: `POST /api/v1/auth/refresh`

**描述**: 使用刷新令牌获取新的访问令牌

**请求参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| refreshToken | String | 是 | 刷新令牌 |

#### 3. 登出

**接口**: `POST /api/v1/auth/logout`

**描述**: 退出登录，使当前令牌失效

**请求头**:

```http
Authorization: Bearer {accessToken}
```

#### 4. 获取当前用户信息

**接口**: `GET /api/v1/auth/me`

**描述**: 获取当前登录用户的详细信息

---

### 疾病筛选接口

#### 1. 获取疾病树形结构

**接口**: `GET /api/v1/diseases/tree`

**描述**: 获取治疗领域和疾病的树形结构，用于界面筛选器

**响应示例**:

```json
{
  "code": 0,
  "message": "success",
  "data": [
    {
      "id": 1,
      "name": "肿瘤",
      "type": "area",
      "children": [
        {
          "id": 101,
          "name": "非小细胞肺癌",
          "type": "disease",
          "parentId": 1
        },
        {
          "id": 102,
          "name": "乳腺癌",
          "type": "disease",
          "parentId": 1
        }
      ]
    },
    {
      "id": 2,
      "name": "免疫",
      "type": "area",
      "children": []
    }
  ]
}
```

#### 2. 获取所有疾病列表

**接口**: `GET /api/v1/diseases/list`

**描述**: 获取所有疾病列表（平铺结构）

**查询参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| page | Integer | 否 | 页码，默认 1 |
| pageSize | Integer | 否 | 每页数量，默认 100 |
| keyword | String | 否 | 搜索关键词 |

---

**接口**: `GET /api/v1/diseases/{id}`

**描述**: 根据疾病 ID 获取疾病详细信息

---

### 靶点组合竞争格局接口

#### 1. 矩阵查询

**接口**: `POST /api/v1/competition/matrix`

**描述**: 根据筛选条件查询靶点×疾病矩阵，返回各格子的最高研发阶段

**请求参数**:

```json
{
  "diseaseIds": [101, 102, 103],
  "phases": ["Approved", "Phase III", "Phase II", "Phase I", "Preclinical"],
  "hideNoComboTargets": true,
  "includePrecandidates": false
}
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| diseaseIds | Integer[] | 是 | 疾病 ID 列表 |
| phases | String[] | 否 | 研发阶段筛选，默认全部 |
| hideNoComboTargets | Boolean | 否 | 是否隐藏无靶点组合的行，默认 true |
| includePrecandidates | Boolean | 否 | 是否包含临床前候选物，默认 false |

**响应示例**:

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "targets": ["TNF", "IL6", "PD1", "CD20"],
    "diseases": [
      {
        "id": 101,
        "name": "非小细胞肺癌"
      },
      {
        "id": 102,
        "name": "乳腺癌"
      }
    ],
    "matrix": [
      {
        "target": "TNF",
        "cells": [
          {
            "diseaseId": 101,
            "maxPhase": "Phase III",
            "drugCount": 5
          },
          {
            "diseaseId": 102,
            "maxPhase": "Phase II",
            "drugCount": 3
          }
        ]
      },
      {
        "target": "IL6",
        "cells": [
          {
            "diseaseId": 101,
            "maxPhase": "Approved",
            "drugCount": 12
          },
          {
            "diseaseId": 102,
            "maxPhase": null,
            "drugCount": 0
          }
        ]
      }
    ],
    "phases": ["Approved", "Phase III", "Phase II", "Phase I", "Preclinical"],
    "phaseColors": {
      "Approved": "#52c41a",
      "Phase III": "#faad14",
      "Phase II": "#fa8c16",
      "Phase I": "#eb2f96",
      "Preclinical": "#d9d9d9"
    },
    "statistics": {
      "totalTargets": 4,
      "totalDiseases": 2,
      "totalDrugs": 20,
      "approvedDrugs": 12,
      "clinicalDrugs": 8
    }
  }
}
```

#### 2. 格子药品详情查询

**接口**: `GET /api/v1/competition/cell-drugs`

**描述**: 查询指定靶点×疾病组合的药品详情

**查询参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| target | String | 是 | 靶点名称 |
| diseaseId | Integer | 是 | 疾病 ID |
| phases | String[] | 否 | 研发阶段筛选 |

**请求示例**:

```http
GET /api/v1/competition/cell-drugs?target=TNF&diseaseId=101&phases=Approved,Phase%20III
```

**响应示例**:

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "target": "TNF",
    "disease": {
      "id": 101,
      "name": "非小细胞肺癌"
    },
    "drugs": [
      {
        "ci_tracking_info_id": "DRUG001",
        "drug_generic_name": "Infliximab",
        "drug_brand_name": "Remicade",
        "drug_synonyms": ["Remicade biosimilar", "Infliximab biosimilar"],
        "target": "TNF",
        "targets_combination": null,
        "indication": "非小细胞肺癌",
        "phases_display": "Approved",
        "phases_score": 50,
        "development_stage": "上市",
        "developer": "Janssen Biotech",
        "origin_country": "United States",
        "mechanism_of_action": "Anti-TNF monoclonal antibody",
        "drug_type": "Biological",
        "administration_route": "Intravenous",
        "approval_year": 1998,
        "latest_update_date": "2026-03-15",
        "source_type": "PharmCube"
      },
      {
        "ci_tracking_info_id": "DRUG002",
        "drug_generic_name": "Adalimumab",
        "drug_brand_name": "Humira",
        "drug_synonyms": ["Humira biosimilar", "Adalimumab biosimilar"],
        "target": "TNF",
        "targets_combination": null,
        "indication": "非小细胞肺癌",
        "phases_display": "Phase III",
        "phases_score": 25,
        "development_stage": "III 期临床",
        "developer": "AbbVie",
        "origin_country": "United States",
        "mechanism_of_action": "Anti-TNF monoclonal antibody",
        "drug_type": "Biological",
        "administration_route": "Subcutaneous",
        "approval_year": null,
        "latest_update_date": "2026-03-20",
        "source_type": "PharmCube"
      }
    ],
    "total": 5,
    "summary": {
      "totalDrugs": 5,
      "approved": 3,
      "phase3": 1,
      "phase2": 1,
      "phase1": 0,
      "preclinical": 0
    }
  }
}
```

#### 3. 导出矩阵数据

**接口**: `POST /api/v1/competition/export`

**描述**: 导出靶点×疾病矩阵数据为 Excel 文件

**请求参数**: 同矩阵查询

**响应**: Excel 文件流（Content-Type: application/vnd.openxmlformats-officedocument.spreadsheetml.sheet）

---

### 靶点研发进展格局接口

#### 1. 获取疾病视图数据

**接口**: `POST /api/v1/progress/disease-view`

**描述**: 查询指定疾病的靶点研发进展管线全景图

**请求参数**:

```json
{
  "diseaseId": 101,
  "includePrecandidates": false
}
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| diseaseId | Integer | 是 | 疾病 ID |
| includePrecandidates | Boolean | 否 | 是否包含临床前候选物，默认 false |

**响应示例**:

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "disease": {
      "id": 101,
      "name": "非小细胞肺癌"
    },
    "pipeline": [
      {
        "target": "EGFR",
        "stages": [
          {
            "stage": "Approved",
            "drugCount": 8,
            "drugs": [
              {
                "ci_tracking_info_id": "DRUG003",
                "drug_generic_name": "Erlotinib",
                "drug_brand_name": "Tarceva",
                "disease": "非小细胞肺癌",
                "phases_display": "Approved",
                "phases_score": 50,
                "developer": "Genentech",
                "latest_update_date": "2026-03-10"
              }
            ]
          },
          {
            "stage": "Phase III",
            "drugCount": 5,
            "drugs": []
          },
          {
            "stage": "Phase II",
            "drugCount": 12,
            "drugs": []
          }
        ]
      },
      {
        "target": "ALK",
        "stages": [
          {
            "stage": "Approved",
            "drugCount": 2,
            "drugs": []
          },
          {
            "stage": "Phase I",
            "drugCount": 4,
            "drugs": []
          }
        ]
      }
    ],
    "stages": ["Approved", "Phase III", "Phase II", "Phase I", "Preclinical"],
    "statistics": {
      "totalTargets": 2,
      "totalDrugs": 31,
      "approvedDrugs": 10,
      "clinicalDrugs": 21
    }
  }
}
```

#### 2. 获取靶点列表

**接口**: `GET /api/v1/progress/targets`

**描述**: 获取指定疾病下的所有靶点列表

**查询参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| diseaseId | Integer | 是 | 疾病 ID |

**响应示例**:

```json
{
  "code": 0,
  "message": "success",
  "data": [
    {
      "target": "EGFR",
      "drugCount": 25
    },
    {
      "target": "ALK",
      "drugCount": 6
    },
    {
      "target": "PD1",
      "drugCount": 18
    }
  ]
}
```

---

### 筛选条件预设接口

#### 1. 保存筛选预设

**接口**: `POST /api/v1/filter-presets`

**描述**: 保存当前筛选条件为预设

**请求参数**:

```json
{
  "name": "肺癌免疫检查点抑制剂分析",
  "type": "competition",
  "filters": {
    "diseaseIds": [101, 102],
    "phases": ["Approved", "Phase III", "Phase II"],
    "hideNoComboTargets": true,
    "includePrecandidates": false
  }
}
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| name | String | 是 | 预设名称 |
| type | String | 是 | 预设类型：`competition` 或 `progress` |
| filters | Object | 是 | 筛选条件 JSON |

**响应示例**:

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": 1,
    "name": "肺癌免疫检查点抑制剂分析",
    "type": "competition",
    "filters": {
      "diseaseIds": [101, 102],
      "phases": ["Approved", "Phase III", "Phase II"],
      "hideNoComboTargets": true,
      "includePrecandidates": false
    },
    "createdAt": "2026-03-30T10:00:00",
    "updatedAt": "2026-03-30T10:00:00"
  }
}
```

#### 2. 获取筛选预设列表

**接口**: `GET /api/v1/filter-presets`

**描述**: 获取当前用户的所有筛选预设

**查询参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| type | String | 否 | 按类型筛选：`competition` 或 `progress` |

**响应示例**:

```json
{
  "code": 0,
  "message": "success",
  "data": [
    {
      "id": 1,
      "name": "肺癌免疫检查点抑制剂分析",
      "type": "competition",
      "createdAt": "2026-03-30T10:00:00"
    },
    {
      "id": 2,
      "name": "乳腺癌靶向治疗",
      "type": "progress",
      "createdAt": "2026-03-29T15:30:00"
    }
  ]
}
```

#### 3. 获取单个筛选预设

**接口**: `GET /api/v1/filter-presets/{id}`

**描述**: 根据 ID 获取筛选预设详情

#### 4. 更新筛选预设

**接口**: `PUT /api/v1/filter-presets/{id}`

**描述**: 更新指定筛选预设

#### 5. 删除筛选预设

**接口**: `DELETE /api/v1/filter-presets/{id}`

**描述**: 删除指定筛选预设

---

## 响应格式规范

### 统一响应结构

所有接口统一返回以下格式：

```json
{
  "code": 0,
  "message": "success",
  "data": {},
  "traceId": "abc123def456",
  "timestamp": 1711785600000
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| code | Integer | 响应码，0 表示成功，非 0 表示失败 |
| message | String | 响应消息 |
| data | Object/Array | 业务数据 |
| traceId | String | 追踪 ID，用于问题排查 |
| timestamp | Long | 时间戳（毫秒） |

### 分页响应格式

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "list": [],
    "total": 100,
    "page": 1,
    "pageSize": 10,
    "totalPages": 10
  }
}
```

---

## 错误码说明

### 错误码列表

| 错误码 | 说明 | 解决方案 |
|--------|------|----------|
| 0 | 成功 | - |
| 1001 | 参数校验失败 | 检查请求参数格式 |
| 1002 | 必填参数缺失 | 补充缺失的参数 |
| 2001 | 用户名或密码错误 | 检查用户名和密码 |
| 2002 | Token 已过期 | 重新登录或刷新 Token |
| 2003 | Token 无效 | 重新登录获取新 Token |
| 2004 | 无权限访问 | 联系管理员分配权限 |
| 3001 | 疾病不存在 | 检查疾病 ID |
| 3002 | 靶点不存在 | 检查靶点名称 |
| 3003 | 数据同步中，请稍后 | 等待同步完成 |
| 4001 | 数据库操作失败 | 联系技术支持 |
| 4002 | 外部服务调用失败 | 检查网络连接 |
| 5000 | 服务器内部错误 | 联系技术支持 |

### 错误响应示例

```json
{
  "code": 2002,
  "message": "Token 已过期，请重新登录",
  "data": null,
  "traceId": "abc123def456",
  "timestamp": 1711785600000
}
```

---

## Swagger 文档访问地址

| 环境 | 地址 |
|------|------|
| 开发环境 | http://localhost:8080/doc.html |
| 测试环境 | https://test-apex.harbourbiomed.com/api/doc.html |
| 生产环境 | https://ai-harby.harbourbiomed.com/api/doc.html |

---

**和铂医药（Harbour BioMed）** © 2026
