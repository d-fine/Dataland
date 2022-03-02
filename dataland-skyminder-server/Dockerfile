FROM eclipse-temurin:17.0.14.1_1-jdk-focal

COPY . /app/
EXPOSE 8080
WORKDIR /app

CMD ./gradlew :dataland-skyminder-server:bootRun --no-daemon