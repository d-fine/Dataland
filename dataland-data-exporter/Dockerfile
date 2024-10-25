ARG DATALAND_DATA_EXPORTER_BASE_VERSION
FROM ghcr.io/d-fine/dataland/dataland_data_exporter_base:${DATALAND_DATA_EXPORTER_BASE_VERSION} AS builder

FROM eclipse-temurin:21.0.4_7-jre-alpine@sha256:848653d62f2fe03f2ef6d0527236fbdedd296ee44bfeb7a9836662dc2f0f58d3
COPY --from=builder /app/dataland-data-exporter/build/libs/dataland-data-exporter-0.0.1-SNAPSHOT.jar /jar/dataland-data-exporter.jar
CMD ["java","-jar","/jar/dataland-data-exporter.jar"]

HEALTHCHECK --interval=30s --timeout=3s CMD kill -0 1
