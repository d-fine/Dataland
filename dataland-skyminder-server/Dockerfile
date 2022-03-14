FROM eclipse-temurin:17.0.2_8-jdk-focal

COPY . /app/
# COPY <path to .gradle on host system, which is $HOME/.gradle> /root/
EXPOSE 8080
WORKDIR /app

CMD ./gradlew :dataland-skyminder-server:bootRun