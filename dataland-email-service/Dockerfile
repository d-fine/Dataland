ARG DATALAND_EMAIL_SERVICE_BASE_VERSION
FROM ghcr.io/d-fine/dataland/dataland_email_service_base:${DATALAND_EMAIL_SERVICE_BASE_VERSION} AS builder

FROM eclipse-temurin:21.0.3_9-jre-alpine@sha256:f05c742dd20051b104b939370f7db4d6eb420cc7fd842aeb4e2446837da3bd03
COPY --from=builder /app/dataland-email-service/build/libs/dataland-email-service-0.0.1-SNAPSHOT.jar /jar/dataland-email-service.jar
CMD ["java","-jar","/jar/dataland-email-service.jar"]

HEALTHCHECK --interval=30s --timeout=3s CMD wget -nv -O- -t 1 http://localhost:8080/api/actuator/health | tee /dev/stderr | grep -q '^{"status":"UP"' || false
