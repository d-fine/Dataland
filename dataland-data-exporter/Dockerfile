ARG DATALAND_DATA_EXPORTER_BASE_VERSION
FROM ghcr.io/d-fine/dataland/dataland_data_exporter_base:${DATALAND_DATA_EXPORTER_BASE_VERSION} AS builder

FROM eclipse-temurin:21.0.7_6-jre-alpine@sha256:8728e354e012e18310faa7f364d00185277dec741f4f6d593af6c61fc0eb15fd
COPY --from=builder /app/dataland-data-exporter/build/libs/dataland-data-exporter-0.0.1-SNAPSHOT.jar /jar/dataland-data-exporter.jar
CMD ["java","-jar","/jar/dataland-data-exporter.jar"]

HEALTHCHECK --interval=30s --timeout=3s CMD kill -0 1
