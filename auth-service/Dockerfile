FROM eclipse-temurin:17-jdk-jammy AS build
WORKDIR /app

COPY gradlew .
COPY gradle gradle
COPY settings.gradle .
COPY build.gradle .
COPY common/build.gradle common/build.gradle
COPY auth-service/build.gradle auth-service/build.gradle
COPY finance-service/build.gradle finance-service/build.gradle
COPY report-service/build.gradle report-service/build.gradle

RUN ./gradlew :auth-service:dependencies --no-daemon -q || true

COPY common/src common/src
COPY auth-service/src auth-service/src

RUN ./gradlew :auth-service:bootJar -x test --no-daemon

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=build /app/auth-service/build/libs/*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
