ARG DATALAND_INTERNAL_STORAGE_BASE_VERSION
FROM ghcr.io/d-fine/dataland/dataland_internal_storage_base:${DATALAND_INTERNAL_STORAGE_BASE_VERSION} AS builder

FROM eclipse-temurin:21.0.13_9-jre-alpine@sha256:ac05c3c42b7c2f1a52c9bd52f01041fa1958231b15ebeae7420429cbc5baf474
COPY --from=builder /app/dataland-internal-storage/build/libs/dataland-internal-storage-0.0.1-SNAPSHOT.jar /jar/dataland-internal-storage.jar
CMD ["java","-jar","/jar/dataland-internal-storage.jar"]

HEALTHCHECK --interval=30s --timeout=5s --start-period=180s --start-interval=60s --retries=6 CMD wget -nv -O- -t 1 http://internal-storage:8080/internal-storage/actuator/health | tee /dev/stderr | grep -q '"status":"UP"' || false
