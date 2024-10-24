ARG DATALAND_API_KEY_MANAGER_BASE_VERSION
FROM ghcr.io/d-fine/dataland/dataland_api_key_manager_base:${DATALAND_API_KEY_MANAGER_BASE_VERSION} AS builder

FROM eclipse-temurin:21.0.5_11-jre-alpine@sha256:03c1fb6fff9b28aa3be69f59df0a274c2c3984189726645cb383217229b25082
COPY --from=builder /app/dataland-api-key-manager/build/libs/dataland-api-key-manager-0.0.1-SNAPSHOT.jar /jar/dataland-api-key-manager.jar
CMD ["java","-jar","/jar/dataland-api-key-manager.jar"]

HEALTHCHECK --interval=30s --timeout=3s CMD wget -nv -O- -t 1 http://localhost:8080/api-keys/actuator/health | tee /dev/stderr | grep -q '^{"status":"UP"' || false
