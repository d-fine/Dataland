FROM ubuntu:latest

RUN apt-get update && apt-get install -y \
openjdk-17-jdk \
&& rm -rf /var/lib/apt/lists/*

COPY gradle /app/gradle/
COPY dataland-backend/src /app/dataland-backend/src/
COPY dataland-backend/build.gradle.kts /app/dataland-backend
COPY gradlew settings.gradle.kts /app/
EXPOSE 8080
WORKDIR /app

CMD ./gradlew bootRun
