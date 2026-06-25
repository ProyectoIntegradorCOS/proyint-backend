FROM docker.io/library/maven:3.9-eclipse-temurin-21 AS build

WORKDIR /workspace

# Cache dependencies
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

# Copy sources and build
COPY src ./src
COPY src/main/resources ./src/main/resources
RUN mvn -B -q clean package -DskipTests

# ---------- Runtime Stage ----------
FROM docker.io/library/eclipse-temurin:21-jre-alpine

ENV JAVA_OPTS="-XX:MaxRAMPercentage=50.0 -XX:+UseContainerSupport -XX:+ExitOnOutOfMemoryError"

RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

WORKDIR /app

COPY --from=build /workspace/target/*.jar /app/app.jar

EXPOSE 5511
ENV SPRING_PROFILES_ACTIVE=default

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
