# agentool后端生产环境Dockerfile
FROM eclipse-temurin:17-jdk-jammy AS builder

# 设置工作目录
WORKDIR /app

# 复制Maven配置文件
COPY agentool-backend/pom.xml .
COPY agentool-backend/mvnw .
COPY agentool-backend/.mvn .mvn

# 修复mvnw执行权限（Windows兼容）
RUN chmod +x ./mvnw

# 下载依赖（利用Docker缓存）
RUN ./mvnw dependency:go-offline -B

# 复制源代码
COPY agentool-backend/src src

# 构建应用
RUN ./mvnw clean package -DskipTests -B

# 运行时镜像
FROM eclipse-temurin:17-jre-jammy

# 安装必要的系统工具
RUN apt-get update && \
    apt-get install -y --no-install-recommends curl && \
    rm -rf /var/lib/apt/lists/*

# 创建应用用户
RUN groupadd -g 1000 agentool && \
    useradd -u 1000 -g agentool -m -s /bin/sh agentool

# 设置工作目录
WORKDIR /app

# 从构建阶段复制JAR文件
COPY --from=builder /app/target/*.jar app.jar

# 创建日志和存储目录
RUN mkdir -p logs storage && \
    chown -R agentool:agentool /app

# 切换到非root用户
USER agentool

# 健康检查
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8088/api/health || exit 1

# 暴露端口
EXPOSE 8088

# JVM优化参数
ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC -XX:MaxGCPauseMillis=200"

# 启动命令
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
