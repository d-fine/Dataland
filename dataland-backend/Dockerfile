FROM eclipse-temurin:17.0.2_8-jre-alpine

COPY . /app/
EXPOSE 8080
WORKDIR /app

CMD sh -c "./gradlew bootRun --no-daemon || tail -f /dev/null"
