FROM eclipse-temurin:17.0.2_8-jdk-alpine
COPY . /app/
EXPOSE 8080
WORKDIR /app

CMD ./gradlew :dataland-backend:bootRun --no-daemon