ARG DATALAND_API_KEY_MANAGER_BASE_VERSION
FROM ghcr.io/d-fine/dataland/dataland_api_key_manager_base:${DATALAND_API_KEY_MANAGER_BASE_VERSION} AS builder

FROM eclipse-temurin:21.0.9_10-jre-alpine@sha256:08eecc477dbe3f2e33daac27f36e41daf7f4ec51d2f3396006e54fa41832c74c
COPY --from=builder /app/dataland-api-key-manager/build/libs/dataland-api-key-manager-0.0.1-SNAPSHOT.jar /jar/dataland-api-key-manager.jar
CMD ["java","-jar","/jar/dataland-api-key-manager.jar"]

HEALTHCHECK --interval=30s --timeout=3s CMD wget -nv -O- -t 1 http://localhost:8080/api-keys/actuator/health | tee /dev/stderr | grep -q '^{"status":"UP"' || false
