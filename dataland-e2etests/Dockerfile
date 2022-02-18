FROM eclipse-temurin:17.0.2_8-jre-alpine

COPY gradle /app/gradle/
COPY dataland-e2etests/src /app/dataland-e2etests/src/
COPY dataland-e2etests/build.gradle.kts /app/dataland-e2etests
COPY dataland-backend/src /app/dataland-backend/src/
COPY dataland-backend/build.gradle.kts /app/dataland-backend
COPY gradlew settings.gradle.kts build.gradle.kts /app/
COPY testing-shell-scripts/wait_for_backend.sh /app/wait_for_backend.sh
RUN apk update && apk add bash && apk add curl
EXPOSE 8080
WORKDIR /app

CMD [ "/bin/sh", "./wait_for_backend.sh", "start"]

