FROM eclipse-temurin:17.0.2_8-jre-alpine

COPY gradle /app/gradle/
COPY dataland-e2etests/src /app/dataland-e2etests/src/
COPY dataland-e2etests/build.gradle.kts /app/dataland-e2etests
COPY dataland-backend/src /app/dataland-backend/src/
COPY dataland-backend/build.gradle.kts /app/dataland-backend
COPY gradlew settings.gradle.kts build.gradle.kts /app/
EXPOSE 8080
WORKDIR /app

CMD ./gradlew :dataland-e2etests:test --no-daemon
