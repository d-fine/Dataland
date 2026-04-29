ARG DATALAND_DATA_SOURCING_SERVICE_BASE_VERSION
FROM ghcr.io/d-fine/dataland/dataland_data_sourcing_service_base:${DATALAND_DATA_SOURCING_SERVICE_BASE_VERSION} AS builder

FROM eclipse-temurin:21.0.10_7-jre-alpine@sha256:6ad8ed080d9be96b61438ec3ce99388e294af216ed57356000c06070e85c5d5d
COPY --from=builder /app/dataland-data-sourcing-service/build/libs/dataland-data-sourcing-service-0.0.1-SNAPSHOT.jar /jar/dataland-data-sourcing-service.jar
CMD ["java","-jar","/jar/dataland-data-sourcing-service.jar"]

HEALTHCHECK --interval=30s --timeout=3s CMD wget -nv -O- -t 1 http://localhost:8080/data-sourcing/actuator/health | tee /dev/stderr | grep -q '^{"status":"UP"' || false
