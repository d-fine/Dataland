ARG DATALAND_API_KEY_MANAGER_BASE_VERSION
FROM ghcr.io/d-fine/dataland/dataland_api_key_manager_base:${DATALAND_API_KEY_MANAGER_BASE_VERSION} AS builder

FROM eclipse-temurin:21.0.3_9-jre-alpine@sha256:f05c742dd20051b104b939370f7db4d6eb420cc7fd842aeb4e2446837da3bd03
COPY --from=builder /app/dataland-api-key-manager/build/libs/dataland-api-key-manager-0.0.1-SNAPSHOT.jar /jar/dataland-api-key-manager.jar
CMD ["java","-jar","/jar/dataland-api-key-manager.jar"]

HEALTHCHECK --interval=30s --timeout=3s CMD wget -nv -O- -t 1 http://localhost:8080/api-keys/actuator/health | tee /dev/stderr | grep -q '^{"status":"UP"' || false
