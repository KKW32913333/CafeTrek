# ---- build stage ----
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /build
COPY pom.xml .
# Cache dependency downloads separately from source changes.
RUN mvn -q -B dependency:go-offline || true
COPY src ./src
RUN mvn -q -B clean package -DskipTests

# ---- run stage ----
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=build /build/target/*.jar app.jar

# Render injects $PORT at runtime (see application.yml: server.port: ${PORT:10000}).
EXPOSE 10000
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
