FROM eclipse-temurin:21-jdk-alpine AS build

WORKDIR /workspace

COPY gradlew build.gradle.kts settings.gradle.kts ./
COPY gradle gradle

RUN chmod +x gradlew && ./gradlew dependencies --no-daemon

COPY src src

RUN ./gradlew bootJar --no-daemon

FROM eclipse-temurin:21-jre-alpine

RUN addgroup -S spring && adduser -S spring -G spring

USER spring:spring
WORKDIR /app

COPY --from=build /workspace/build/libs/*.jar app.jar

EXPOSE 10000

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
