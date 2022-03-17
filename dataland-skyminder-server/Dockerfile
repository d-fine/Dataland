FROM ghcr.io/d-fine/dataland/temurinbase:latest

COPY . /app/
EXPOSE 8080
WORKDIR /app

CMD ./gradlew :dataland-skyminder-server:bootRun --no-daemon