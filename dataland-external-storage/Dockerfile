ARG DATALAND_EXTERNAL_STORAGE_BASE_VERSION
FROM ghcr.io/d-fine/dataland/dataland_external_storage_base:${DATALAND_EXTERNAL_STORAGE_BASE_VERSION} AS builder

FROM eclipse-temurin:21.0.6_7-jre-alpine@sha256:4c07db858c3b8bfed4cb9163f4aeedbff9c0c2b8212ec1fa75c13a169dec8dc6
COPY --from=builder /app/dataland-external-storage/build/libs/dataland-external-storage-0.0.1-SNAPSHOT.jar /jar/dataland-external-storage.jar
CMD ["java","-jar","/jar/dataland-external-storage.jar"]

HEALTHCHECK --interval=30s --timeout=3s CMD wget -nv -O- -t 1 http://external-storage:8080/external-storage/actuator/health | tee /dev/stderr | grep -q '^{"status":"UP"' || false
