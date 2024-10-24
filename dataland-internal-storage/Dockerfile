ARG DATALAND_INTERNAL_STORAGE_BASE_VERSION
FROM ghcr.io/d-fine/dataland/dataland_internal_storage_base:${DATALAND_INTERNAL_STORAGE_BASE_VERSION} AS builder

FROM eclipse-temurin:21.0.5_11-jre-alpine@sha256:03c1fb6fff9b28aa3be69f59df0a274c2c3984189726645cb383217229b25082
COPY --from=builder /app/dataland-internal-storage/build/libs/dataland-internal-storage-0.0.1-SNAPSHOT.jar /jar/dataland-internal-storage.jar
CMD ["java","-jar","/jar/dataland-internal-storage.jar"]

HEALTHCHECK --interval=30s --timeout=3s CMD wget -nv -O- -t 1 http://internal-storage:8080/internal-storage/actuator/health | tee /dev/stderr | grep -q '^{"status":"UP"' || false
