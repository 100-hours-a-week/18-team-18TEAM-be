FROM docker.io/library/eclipse-temurin:21-jdk-jammy AS builder

WORKDIR /workspace

# 1) Gradle wrapper + build scripts first for dependency-layer caching.
COPY gradlew gradlew
COPY gradle gradle
COPY settings.gradle build.gradle gradle.properties ./

RUN chmod +x gradlew
RUN ./gradlew --no-daemon dependencies

# 2) Source is copied after deps so code-only changes reuse cached dependency layer.
COPY src src

RUN ./gradlew --no-daemon clean bootJar -x test \
    && cp build/libs/*.jar app.jar


FROM docker.io/library/eclipse-temurin:21-jre-jammy AS runtime

WORKDIR /app

RUN groupadd --system spring && useradd --system --gid spring spring

COPY --from=builder /workspace/app.jar /app/app.jar

ENV TZ=Asia/Seoul
ENV SERVER_PORT=8080
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+ExitOnOutOfMemoryError"

EXPOSE 8080

USER spring:spring

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar --server.port=${SERVER_PORT} --server.address=0.0.0.0"]
