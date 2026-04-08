# Apex 平台后端 Dockerfile
# 使用多阶段构建优化镜像大小

# ============================================
# 阶段 1：Maven 构建阶段
# ============================================
FROM maven:3.9-eclipse-temurin-21-alpine AS builder

WORKDIR /app

# 先复制所有 pom.xml，利用 Docker 层缓存加速依赖下载
COPY pom.xml .
COPY apex-common/pom.xml apex-common/pom.xml
COPY apex-auth/pom.xml apex-auth/pom.xml
COPY apex-competition/pom.xml apex-competition/pom.xml
COPY apex-progress/pom.xml apex-progress/pom.xml
COPY apex-filter-preset/pom.xml apex-filter-preset/pom.xml
COPY apex-data-sync/pom.xml apex-data-sync/pom.xml
COPY apex-bootstrap/pom.xml apex-bootstrap/pom.xml

RUN mvn dependency:go-offline -B -q

# 复制源码并构建
COPY apex-common/src apex-common/src
COPY apex-auth/src apex-auth/src
COPY apex-competition/src apex-competition/src
COPY apex-progress/src apex-progress/src
COPY apex-filter-preset/src apex-filter-preset/src
COPY apex-data-sync/src apex-data-sync/src
COPY apex-bootstrap/src apex-bootstrap/src

RUN mvn clean package -DskipTests -B -q

# ============================================
# 阶段 2：JRE 运行阶段
# DuckDB JNI 依赖 glibc / libstdc++，使用 Debian 系运行时避免 Alpine musl 兼容问题
# ============================================
FROM eclipse-temurin:21-jre

WORKDIR /app

RUN apt-get update \
    && apt-get install -y --no-install-recommends tzdata wget ca-certificates \
    && ln -snf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime \
    && echo "Asia/Shanghai" > /etc/timezone \
    && rm -rf /var/lib/apt/lists/*

COPY --from=builder /app/apex-bootstrap/target/apex-bootstrap-1.0.0-SNAPSHOT.jar app.jar

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=10s --start-period=90s --retries=5 \
    CMD wget --quiet --tries=1 --spider http://localhost:8080/api/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
