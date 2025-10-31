ARG DATALAND_INTERNAL_STORAGE_BASE_VERSION
FROM ghcr.io/d-fine/dataland/dataland_internal_storage_base:${DATALAND_INTERNAL_STORAGE_BASE_VERSION} AS builder

FROM eclipse-temurin:21.0.8_9-jre-alpine@sha256:990397e0495ac088ab6ee3d949a2e97b715a134d8b96c561c5d130b3786a489d
COPY --from=builder /app/dataland-internal-storage/build/libs/dataland-internal-storage-0.0.1-SNAPSHOT.jar /jar/dataland-internal-storage.jar
CMD ["java","-jar","/jar/dataland-internal-storage.jar"]

HEALTHCHECK --interval=30s --timeout=3s CMD wget -nv -O- -t 1 http://internal-storage:8080/internal-storage/actuator/health | tee /dev/stderr | grep -q '^{"status":"UP"' || false
