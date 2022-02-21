FROM eclipse-temurin:17.0.2_8-jre-alpine

COPY . /app/
EXPOSE 8080
WORKDIR /app

CMD "./testing-shell-scripts/wait_for_backend_and_run_e2etests.sh"
