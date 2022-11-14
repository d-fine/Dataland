FROM ghcr.io/d-fine/dataland/temurinbase:latest as builder
ARG GITHUB_USER
ARG GITHUB_TOKEN

COPY ./gradle/ /app/gradle/
COPY ./build.gradle.kts ./gradle.properties ./settings.gradle.kts ./gradlew /app/
COPY ./dataland-backend/ /app/dataland-backend/
COPY ./.git/ /app/.git
WORKDIR /app
RUN GITHUB_USER=$GITHUB_USER GITHUB_TOKEN=$GITHUB_TOKEN ./gradlew dataland-backend:bootJar --no-daemon --stacktrace

FROM eclipse-temurin:17.0.4.1_1-jre-alpine
COPY --from=builder /app/dataland-backend/build/libs/dataland-backend*.jar /jar/dataland-backend.jar
CMD ["java","-jar","/jar/dataland-backend.jar"]
