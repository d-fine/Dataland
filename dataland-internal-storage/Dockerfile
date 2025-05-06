ARG DATALAND_INTERNAL_STORAGE_BASE_VERSION
FROM ghcr.io/d-fine/dataland/dataland_internal_storage_base:${DATALAND_INTERNAL_STORAGE_BASE_VERSION} AS builder

FROM eclipse-temurin:21.0.7_6-jre-alpine@sha256:8728e354e012e18310faa7f364d00185277dec741f4f6d593af6c61fc0eb15fd
COPY --from=builder /app/dataland-internal-storage/build/libs/dataland-internal-storage-0.0.1-SNAPSHOT.jar /jar/dataland-internal-storage.jar
CMD ["java","-jar","/jar/dataland-internal-storage.jar"]

HEALTHCHECK --interval=30s --timeout=3s CMD wget -nv -O- -t 1 http://internal-storage:8080/internal-storage/actuator/health | tee /dev/stderr | grep -q '^{"status":"UP"' || false
