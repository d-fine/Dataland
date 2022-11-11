FROM ghcr.io/d-fine/dataland/temurinbase:latest as builder
COPY . /app/
WORKDIR /app
RUN ./gradlew dataland-backend:bootJar --no-daemon --stacktrace

FROM eclipse-temurin:17.0.5_8-jre-alpine
COPY --from=builder /app/dataland-backend/build/libs/dataland-backend*.jar /jar/dataland-backend.jar
CMD ["java","-jar","/jar/dataland-backend.jar"]
