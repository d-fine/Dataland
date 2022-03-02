FROM eclipse-temurin:17.0.14.1_1-jdk-focal
RUN apk add --update nodejs npm

COPY . /app/
EXPOSE 8080
WORKDIR /app

CMD "./testing-shell-scripts/wait_for_backend_and_run_e2etests.sh"
