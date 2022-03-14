FROM eclipse-temurin:17.0.2_8-jdk-focal
#FROM ghrc.io/d-fine/dataland/backendbase:latest

WORKDIR /app

CMD ["./gradlew","ktlintcheck","detekt",":dataland-backend:test"]
