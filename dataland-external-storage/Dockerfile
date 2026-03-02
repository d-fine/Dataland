ARG DATALAND_EXTERNAL_STORAGE_BASE_VERSION
FROM ghcr.io/d-fine/dataland/dataland_external_storage_base:${DATALAND_EXTERNAL_STORAGE_BASE_VERSION} AS builder

FROM eclipse-temurin:25.0.1_8-jre-alpine@sha256:b51543f89580c1ba70e441cfbc0cfc1635c3c16d2e2d77fec9d890342a3a8687
COPY --from=builder /app/dataland-external-storage/build/libs/dataland-external-storage-0.0.1-SNAPSHOT.jar /jar/dataland-external-storage.jar
CMD ["java","-jar","/jar/dataland-external-storage.jar"]

HEALTHCHECK --interval=30s --timeout=3s CMD wget -nv -O- -t 1 http://external-storage:8080/external-storage/actuator/health | tee /dev/stderr | grep -q '^{"status":"UP"' || false
