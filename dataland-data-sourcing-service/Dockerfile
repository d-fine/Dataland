ARG DATALAND_API_KEY_MANAGER_BASE_VERSION
FROM ghcr.io/d-fine/dataland/dataland_data_sourcing_service_base:${DATALAND_API_KEY_MANAGER_BASE_VERSION} AS builder

FROM eclipse-temurin:21.0.8_9-jre-alpine@sha256:4ca7eff3ab0ef9b41f5fefa35efaeda9ed8d26e161e1192473b24b3a6c348aef
COPY --from=builder /app/dataland-api-key-manager/build/libs/dataland-api-key-manager-0.0.1-SNAPSHOT.jar /jar/dataland-api-key-manager.jar
CMD ["java","-jar","/jar/dataland-api-key-manager.jar"]

HEALTHCHECK --interval=30s --timeout=3s CMD wget -nv -O- -t 1 http://localhost:8080/api-keys/actuator/health | tee /dev/stderr | grep -q '^{"status":"UP"' || false
