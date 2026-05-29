# Dockerfile

# ===== Stage 1: Build =====
# Dùng multi-stage build — image cuối cùng không chứa Maven, nhỏ hơn nhiều
FROM maven:3.9-eclipse-temurin-21 AS builder

WORKDIR /app

# Copy pom.xml trước — tận dụng Docker layer cache
# Nếu code thay đổi nhưng pom.xml không đổi → không download lại dependency
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source và build — bỏ qua test (test đã chạy trước khi deploy)
COPY src ./src
RUN mvn clean package -DskipTests

# ===== Stage 2: Run =====
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Chỉ copy file jar — không copy source, không copy Maven
COPY --from=builder /app/target/*.jar app.jar

# Giới hạn RAM — tránh JVM chiếm hết memory
ENV JAVA_OPTS="-Xms128m -Xmx512m"

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]