ARG DATALAND_API_KEY_MANAGER_BASE_VERSION
FROM ghcr.io/d-fine/dataland/dataland_api_key_manager_base:${DATALAND_API_KEY_MANAGER_BASE_VERSION} AS builder

FROM eclipse-temurin:21.0.4_7-jre-alpine@sha256:848653d62f2fe03f2ef6d0527236fbdedd296ee44bfeb7a9836662dc2f0f58d3
COPY --from=builder /app/dataland-api-key-manager/build/libs/dataland-api-key-manager-0.0.1-SNAPSHOT.jar /jar/dataland-api-key-manager.jar
CMD ["java","-jar","/jar/dataland-api-key-manager.jar"]

HEALTHCHECK --interval=30s --timeout=3s CMD wget -nv -O- -t 1 http://localhost:8080/api-keys/actuator/health | tee /dev/stderr | grep -q '^{"status":"UP"' || false
