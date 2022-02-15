FROM ubuntu:latest

RUN apt-get update && apt-get install -y \
openjdk-17-jdk \
&& rm -rf /var/lib/apt/lists/*

COPY gradle /app/gradle/
COPY dataland-e2etests/src /app/dataland-e2etests/src/
COPY dataland-e2etests/build.gradle.kts /app/dataland-e2etests
COPY gradlew settings.gradle.kts /app/
EXPOSE 8080
WORKDIR /app

CMD tail -f /dev/null
