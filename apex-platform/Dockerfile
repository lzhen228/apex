# Apex 平台后端 Dockerfile
# 基础镜像：Eclipse Temurin JDK 21 Alpine

# ============================================
# 阶段 1：Maven 构建阶段
# ============================================
FROM maven:3.9-eclipse-temurin-21-alpine AS builder

# 设置工作目录
WORKDIR /build

# 设置时区
RUN apk add --no-cache tzdata && \
    cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && \
    echo "Asia/Shanghai" > /etc/timezone && \
    apk del tzdata

# 复制 Maven 配置文件（pom.xml 和 settings.xml）
COPY pom.xml .
COPY mvnw* .
COPY .mvn .mvn

# 下载依赖（利用 Docker 缓存层）
RUN mvn dependency:go-offline -B || echo "Dependencies downloaded"

# 复制源码
COPY src ./src

# 构建 Spring Boot 应用
RUN mvn clean package -DskipTests -B && \
    mv target/*.jar app.jar && \
    chmod +x app.jar

# ============================================
# 阶段 2：运行镜像阶段
# ============================================
FROM eclipse-temurin:21-jre-alpine

# 设置时区
RUN apk add --no-cache tzdata && \
    cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && \
    echo "Asia/Shanghai" > /etc/timezone && \
    apk del tzdata

# 创建非 root 用户
RUN addgroup -S spring && adduser -S spring -G spring

# 设置工作目录
WORKDIR /app

# 从构建阶段复制 JAR 包
COPY --from=builder /build/app.jar app.jar

# 创建日志目录
RUN mkdir -p /app/logs && \
    chown -R spring:spring /app

# 切换到非 root 用户
USER spring:spring

# 暴露端口
EXPOSE 8080
EXPOSE 8081

# 健康检查
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD wget --quiet --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# JVM 优化参数
ENV JAVA_OPTS="-XX:+UseContainerSupport \
               -XX:MaxRAMPercentage=75.0 \
               -XX:+UseG1GC \
               -XX:MaxGCPauseMillis=200 \
               -XX:+HeapDumpOnOutOfMemoryError \
               -XX:HeapDumpPath=/app/logs/heapdump.hprof \
               -Djava.security.egd=file:/dev/./urandom \
               -Dfile.encoding=UTF-8 \
               -Duser.timezone=Asia/Shanghai"

# 启动应用
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
