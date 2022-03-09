FROM eclipse-temurin:17.0.2_8-jdk-focal

COPY . /app/
EXPOSE 8080
WORKDIR /app

CMD ./gradlew :dataland-skyminder-server:bootRun --no-daemon