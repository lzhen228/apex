# Apex Platform - 后端服务

> **版本**: v1.0.0 &nbsp;|&nbsp; **最后更新**: 2026-03-30

---

## 目录

- [模块简介](#模块简介)
- [技术栈](#技术栈)
- [项目结构](#项目结构)
- [快速开始](#快速开始)
- [配置说明](#配置说明)
- [开发指南](#开发指南)
- [测试](#测试)
- [打包部署](#打包部署)

---

## 模块简介

**Apex Platform** 是 Apex 早期靶点情报分析智能体的后端服务，基于 Spring Boot 3.x 构建，提供靶点竞争格局分析、研发进展查询、数据同步等核心功能。

### 核心特性

- **微模块化架构**：按业务域拆分，模块间解耦
- **高性能查询**：物化视图 + Redis 二级缓存
- **定时数据同步**：XXL-Job 调度，每日自动更新
- **安全认证**：JWT 无状态认证 + RBAC 权限控制
- **完整文档**：Knife4j (Swagger) 自动生成 API 文档

---

## 技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| Java | 21 (LTS) | 虚拟线程支持 |
| Spring Boot | 3.3.x | 应用框架 |
| Spring Security | 6.x | 安全框架 |
| MyBatis-Plus | 3.5.x | ORM 框架 |
| Apache Parquet | 1.14.x | Parquet 文件解析 |
| Aliyun OSS SDK | 3.17.x | 对象存储 |
| XXL-Job | 2.4.x | 任务调度 |
| MapStruct | 1.5.x | 对象映射 |
| Knife4j | 4.x | API 文档 |
| HikariCP | 5.x | 数据库连接池 |
| Lombok | 1.18.x | 代码简化 |

---

## 项目结构

```
apex-platform/
├── pom.xml                                    # 父 POM
├── apex-platform/                             # 主启动模块
│   ├── src/main/java/com/harbourbiomed/apex/
│   │   ├── ApexApplication.java              # 启动类
│   │   └── config/                           # 配置类
│   └── pom.xml
├── apex-platform-auth/                        # 认证模块
│   ├── src/main/java/com/harbourbiomed/apex/auth/
│   │   ├── controller/
│   │   │   └── AuthController.java           # 认证接口
│   │   ├── service/
│   │   │   ├── AuthService.java              # 认证服务
│   │   │   └── JwtService.java               # JWT 服务
│   │   ├── security/
│   │   │   ├── JwtAuthenticationFilter.java  # JWT 过滤器
│   │   │   └── SecurityConfig.java           # 安全配置
│   │   └── model/
│   │       ├── dto/
│   │       └── entity/
│   └── pom.xml
├── apex-platform-common/                       # 通用基础模块
│   ├── src/main/java/com/harbourbiomed/apex/common/
│   │   ├── response/
│   │   │   ├── R.java                        # 统一响应
│   │   │   └── ResultCode.java               # 响应码枚举
│   │   ├── exception/
│   │   │   ├── BusinessException.java        # 业务异常
│   │   │   └── GlobalExceptionHandler.java   # 全局异常处理
│   │   ├── utils/
│   │   │   ├── DateUtils.java
│   │   │   ├── EncryptionUtils.java
│   │   │   └── FileUtils.java
│   │   └── aspect/
│   │       └── LogAspect.java                # 操作日志切面
│   └── pom.xml
├── apex-platform-data-sync/                    # 数据同步模块
│   ├── src/main/java/com/harbourbiomed/apex/datasync/
│   │   ├── job/
│   │   │   └── DataSyncJob.java              # 同步任务
│   │   ├── service/
│   │   │   ├── OssService.java               # OSS 服务
│   │   │   ├── ParquetService.java           # Parquet 解析
│   │   │   └── DataSyncService.java          # 数据同步服务
│   │   └── mapper/
│   └── pom.xml
├── apex-platform-competition/                  # 竞争格局模块
│   ├── src/main/java/com/harbourbiomed/apex/competition/
│   │   ├── controller/
│   │   │   ├── CompetitionController.java   # 竞争格局接口
│   │   │   └── DiseaseController.java         # 疾病接口
│   │   ├── service/
│   │   │   ├── MatrixQueryService.java       # 矩阵查询
│   │   │   └── CompetitionService.java       # 竞争格局服务
│   │   └── mapper/
│   │       ├── CiTrackingMapper.java
│   │       ├── DiseaseMapper.java
│   │       └── TherapeuticAreaMapper.java
│   └── pom.xml
├── apex-platform-progress/                     # 研发进展模块
│   ├── src/main/java/com/harbourbiomed/apex/progress/
│   │   ├── controller/
│   │   │   └── ProgressController.java       # 研发进展接口
│   │   ├── service/
│   │   │   ├── DiseaseViewService.java       # 疾病视图
│   │   │   └── ProgressService.java         # 研发进展服务
│   │   └── mapper/
│   └── pom.xml
└── apex-platform-filter-preset/                # 筛选预设模块
    ├── src/main/java/com/harbourbiomed/apex/filterpreset/
    │   ├── controller/
    │   │   └── FilterPresetController.java   # 筛选预设接口
    │   ├── service/
    │   │   └── FilterPresetService.java      # 筛选预设服务
    │   └── mapper/
    │       └── FilterPresetMapper.java
    └── pom.xml
```

---

## 快速开始

### 环境要求

- JDK 21+
- Maven 3.8+
- PostgreSQL 16.x
- Redis 7.x

### 本地运行

```bash
# 1. 克隆项目
cd apex-platform

# 2. 编译项目
./mvnw clean install -DskipTests

# 3. 配置数据库连接
# 编辑 src/main/resources/application-dev.yml

# 4. 启动应用
cd apex-platform
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# 或直接运行打包后的 JAR
java -jar target/apex-platform-1.0.0.jar --spring.profiles.active=dev
```

### 访问 Swagger 文档

启动成功后，访问 http://localhost:8080/doc.html

---

## 配置说明

### application.yml 主配置

```yaml
spring:
  application:
    name: apex-platform
  profiles:
    active: ${SPRING_PROFILES_ACTIVE:dev}
  
server:
  port: 8080
  servlet:
    context-path: /
```

### 数据库配置

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/apex
    username: apex_user
    password: ${DB_PASSWORD}
    driver-class-name: org.postgresql.Driver
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000

mybatis-plus:
  mapper-locations: classpath*:mapper/**/*Mapper.xml
  type-aliases-package: com.harbourbiomed.apex.*.entity
  configuration:
    map-underscore-to-camel-case: true
    cache-enabled: true
    log-impl: org.apache.ibatis.logging.slf4j.Slf4jImpl
```

### Redis 配置

```yaml
spring:
  data:
    redis:
      host: ${REDIS_HOST:localhost}
      port: ${REDIS_PORT:6379}
      password: ${REDIS_PASSWORD:}
      database: 0
      lettuce:
        pool:
          max-active: 20
          max-idle: 10
          min-idle: 5
          max-wait: 1000ms
      timeout: 5000ms
```

### OSS 配置

```yaml
aliyun:
  oss:
    endpoint: ${OSS_ENDPOINT}
    access-key-id: ${OSS_ACCESS_KEY_ID}
    access-key-secret: ${OSS_ACCESS_KEY_SECRET}
    bucket-name: ${OSS_BUCKET_NAME}
    # 数据文件路径
    tracking-info-path: pharmcube2harbour_ci_tracking_info_0.parquet
    drug-pipeline-path: pharmcube2harbour_drug_pipeline_info_0.parquet
```

### JWT 配置

```yaml
jwt:
  secret: ${JWT_SECRET:your-secret-key-here-change-in-production}
  access-token-expiration: 7200000  # 2小时（毫秒）
  refresh-token-expiration: 604800000  # 7天（毫秒）
  issuer: apex-platform
```

### XXL-Job 配置

```yaml
xxl:
  job:
    admin:
      addresses: http://localhost:8080/xxl-job-admin
    executor:
      appname: apex-platform
      port: 9999
      logpath: ./logs/xxl-job
      logretentiondays: 30
```

---

## 开发指南

### 代码规范

#### 包命名规范

```
com.harbourbiomed.apex
├── {module}                    # 功能模块
│   ├── controller              # 控制器层
│   ├── service                 # 服务层
│   │   └── impl                # 服务实现
│   ├── mapper                  # 持久层
│   ├── model                   # 数据模型
│   │   ├── entity              # 实体类
│   │   ├── dto                 # 数据传输对象
│   │   └── vo                  # 视图对象
│   └── config                  # 配置类
```

#### 类命名规范

| 类型 | 命名规范 | 示例 |
|------|----------|------|
| 实体类 | `{Name}Entity` 或 `{Name}` | `CiTracking`, `Disease` |
| DTO | `{Name}DTO` 或 `{Name}Request` | `MatrixQueryRequest` |
| VO | `{Name}VO` 或 `{Name}Response` | `MatrixQueryResponse` |
| Service | `{Name}Service` | `MatrixQueryService` |
| ServiceImpl | `{Name}ServiceImpl` | `MatrixQueryServiceImpl` |
| Controller | `{Name}Controller` | `CompetitionController` |
| Mapper | `{Name}Mapper` | `CiTrackingMapper` |

### API 规范

#### RESTful 接口设计

```java
@RestController
@RequestMapping("/api/v1/competition")
@Tag(name = "靶点组合竞争格局", description = "提供靶点组合竞争格局查询接口")
public class CompetitionController {
    
    @PostMapping("/matrix")
    @Operation(summary = "矩阵查询", description = "根据筛选条件查询靶点×疾病矩阵")
    public R<MatrixQueryResponse> queryMatrix(
        @Valid @RequestBody MatrixQueryRequest request
    ) {
        return R.ok(competitionService.queryMatrix(request));
    }
    
    @GetMapping("/cell-drugs")
    @Operation(summary = "格子药品详情", description = "查询指定靶点×疾病组合的药品详情")
    public R<CellDrugsResponse> getCellDrugs(
        @RequestParam String target,
        @RequestParam Integer diseaseId,
        @RequestParam List<String> phases
    ) {
        return R.ok(competitionService.getCellDrugs(target, diseaseId, phases));
    }
}
```

#### 统一响应格式

所有接口统一返回 `R<T>` 包装类型：

```java
@Data
public class R<T> {
    private Integer code;      // 0=成功，非0=失败
    private String message;     // 提示信息
    private T data;            // 业务数据
    private String traceId;    // 追踪ID
    private Long timestamp;    // 时间戳
    
    public static <T> R<T> ok(T data) {
        R<T> r = new R<>();
        r.setCode(0);
        r.setMessage("success");
        r.setData(data);
        r.setTraceId(MDC.get("traceId"));
        r.setTimestamp(System.currentTimeMillis());
        return r;
    }
    
    public static <T> R<T> fail(ResultCode code) {
        R<T> r = new R<>();
        r.setCode(code.getCode());
        r.setMessage(code.getMessage());
        r.setTraceId(MDC.get("traceId"));
        r.setTimestamp(System.currentTimeMillis());
        return r;
    }
}
```

### 数据库迁移

使用 Flyway 进行数据库版本管理：

```bash
# 创建新的迁移脚本
# 文件命名: V{version}__{description}.sql
# 例如: V1.0.1__add_index_on_targets.sql

# 迁移脚本位置
sql/migrations/
```

---

## 测试

```bash
# 运行所有测试
./mvnw test

# 运行指定模块测试
./mvnw -pl apex-platform-competition test

# 运行指定测试类
./mvnw test -Dtest=MatrixQueryServiceTest

# 生成测试覆盖率报告
./mvnw jacoco:report
```

---

## 打包部署

### 本地打包

```bash
# 清理并打包（跳过测试）
./mvnw clean package -DskipTests

# 生成的 JAR 文件
# apex-platform/target/apex-platform-1.0.0.jar
```

### Docker 镜像构建

```bash
# 构建镜像
docker build -t harbourbiomed/apex-platform:1.0.0 .

# 推送到镜像仓库
docker push harbourbiomed/apex-platform:1.0.0
```

### 运行参数示例

```bash
java -jar apex-platform-1.0.0.jar \
  --spring.profiles.active=prod \
  --DB_URL=jdbc:postgresql://db:5432/apex \
  --DB_PASSWORD=*** \
  --REDIS_HOST=redis \
  --REDIS_PASSWORD=*** \
  --OSS_ENDPOINT=*** \
  --OSS_ACCESS_KEY_ID=*** \
  --OSS_ACCESS_KEY_SECRET=*** \
  --OSS_BUCKET_NAME=*** \
  --JWT_SECRET=***
```

---

**和铂医药（Harbour BioMed）** © 2026
