ARG DATALAND_EMAIL_SERVICE_BASE_VERSION
FROM ghcr.io/d-fine/dataland/dataland_email_service_base:${DATALAND_EMAIL_SERVICE_BASE_VERSION} AS builder

FROM eclipse-temurin:21.0.6_7-jre-alpine@sha256:4c07db858c3b8bfed4cb9163f4aeedbff9c0c2b8212ec1fa75c13a169dec8dc6
COPY --from=builder /app/dataland-email-service/build/libs/dataland-email-service-0.0.1-SNAPSHOT.jar /jar/dataland-email-service.jar
CMD ["java","-jar","/jar/dataland-email-service.jar"]

HEALTHCHECK --interval=30s --timeout=3s CMD wget -nv -O- -t 1 http://localhost:8080/email/actuator/health | tee /dev/stderr | grep -q '^{"status":"UP"' || false
