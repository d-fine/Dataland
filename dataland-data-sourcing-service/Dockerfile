ARG DATALAND_DATA_SOURCING_SERVICE_BASE_VERSION
FROM ghcr.io/d-fine/dataland/dataland_data_sourcing_service_base:${DATALAND_DATA_SOURCING_SERVICE_BASE_VERSION} AS builder

FROM eclipse-temurin:25.0.1_8-jre-alpine@sha256:b51543f89580c1ba70e441cfbc0cfc1635c3c16d2e2d77fec9d890342a3a8687
COPY --from=builder /app/dataland-data-sourcing-service/build/libs/dataland-data-sourcing-service-0.0.1-SNAPSHOT.jar /jar/dataland-data-sourcing-service.jar
CMD ["java","-jar","/jar/dataland-data-sourcing-service.jar"]

HEALTHCHECK --interval=30s --timeout=3s CMD wget -nv -O- -t 1 http://localhost:8080/data-sourcing/actuator/health | tee /dev/stderr | grep -q '^{"status":"UP"' || false
