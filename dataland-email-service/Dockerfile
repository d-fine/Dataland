ARG DATALAND_EMAIL_SERVICE_BASE_VERSION
FROM ghcr.io/d-fine/dataland/dataland_email_service_base:${DATALAND_EMAIL_SERVICE_BASE_VERSION} AS builder

FROM eclipse-temurin:21.0.12_8-jre-alpine@sha256:1a29e1fe337eb28b5bec30f0ee8ed29f0ff80ab6f75dcf9313efe82911065a52
COPY --from=builder /app/dataland-email-service/build/libs/dataland-email-service-0.0.1-SNAPSHOT.jar /jar/dataland-email-service.jar
CMD ["java","-jar","/jar/dataland-email-service.jar"]

HEALTHCHECK --interval=30s --timeout=5s --start-period=180s --start-interval=60s --retries=6 CMD wget -nv -O- -t 1 http://localhost:8080/email/actuator/health | tee /dev/stderr | grep -q '^{"status":"UP"' || false
