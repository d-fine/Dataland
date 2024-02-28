ARG DATALAND_EMAIL_SERVICE_BASE_VERSION
FROM ghcr.io/d-fine/dataland/dataland_email_service_base:${DATALAND_EMAIL_SERVICE_BASE_VERSION} as builder

FROM eclipse-temurin:17.0.9_9-jre-alpine
COPY --from=builder /app/dataland-email-service/build/libs/dataland-email-service-0.0.1-SNAPSHOT.jar /jar/dataland-email-service.jar
CMD ["java","-jar","/jar/dataland-email-service.jar"]

HEALTHCHECK --interval=30s --timeout=3s CMD exit 0

# toDo HEALTHCHECK überprüfen, ob hier weiterhin ein OpenApi Endpunkt verwedent werden soll oder nicht (application.properties gegenchecken)
