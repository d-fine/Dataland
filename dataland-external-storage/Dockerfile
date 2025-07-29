ARG DATALAND_EXTERNAL_STORAGE_BASE_VERSION
FROM ghcr.io/d-fine/dataland/dataland_external_storage_base:${DATALAND_EXTERNAL_STORAGE_BASE_VERSION} AS builder

FROM eclipse-temurin:21.0.7_6-jre-alpine@sha256:7b5c88eb4182a92aab3a4b10550061a6e18639bf176e7ebca21f866b19f853c1
COPY --from=builder /app/dataland-external-storage/build/libs/dataland-external-storage-0.0.1-SNAPSHOT.jar /jar/dataland-external-storage.jar
CMD ["java","-jar","/jar/dataland-external-storage.jar"]

HEALTHCHECK --interval=30s --timeout=3s CMD wget -nv -O- -t 1 http://external-storage:8080/external-storage/actuator/health | tee /dev/stderr | grep -q '^{"status":"UP"' || false
