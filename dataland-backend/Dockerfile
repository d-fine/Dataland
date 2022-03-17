FROM ghcr.io/d-fine/dataland/temurinbase:latest
COPY . /app/
# COPY <path to .gradle on host system, which is $HOME/.gradle> /root/
EXPOSE 8080
WORKDIR /app

CMD ./gradlew :dataland-backend:bootRun --no-daemon