ARG DATALAND_ACCOUNTING_SERVICE_BASE_VERSION
FROM ghcr.io/d-fine/dataland/dataland_accounting_service_base:${DATALAND_ACCOUNTING_SERVICE_BASE_VERSION} AS builder

FROM eclipse-temurin:21.0.9_10-jre-alpine@sha256:08eecc477dbe3f2e33daac27f36e41daf7f4ec51d2f3396006e54fa41832c74c
COPY --from=builder /app/dataland-accounting-service/build/libs/dataland-accounting-service-0.0.1-SNAPSHOT.jar /jar/dataland-accounting-service.jar
CMD ["java","-jar","/jar/dataland-accounting-service.jar"]

HEALTHCHECK --interval=30s --timeout=3s CMD wget -nv -O- -t 1 http://localhost:8080/accounting/actuator/health | tee /dev/stderr | grep -q '^{"status":"UP"' || false
