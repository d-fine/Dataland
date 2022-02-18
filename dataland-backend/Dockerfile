FROM eclipse-temurin:17.0.2_8-jre-alpine

COPY gradle /app/gradle/
COPY dataland-backend/src /app/dataland-backend/src/
COPY dataland-backend/build.gradle.kts /app/dataland-backend
COPY gradlew settings.gradle.kts build.gradle.kts /app/
EXPOSE 8080
WORKDIR /app

CMD sh -c "./gradlew bootRun || tail -f /dev/null"
