ARG DATALAND_DATA_EXPORTER_BASE_VERSION
FROM ghcr.io/d-fine/dataland/dataland_data_exporter_base:${DATALAND_DATA_EXPORTER_BASE_VERSION} AS builder

FROM eclipse-temurin:21.0.5_11-jre-alpine@sha256:decee204b9a1eb333c364ba4d859a6b1380eb13f0980d2acfd65c09fee53a48a
COPY --from=builder /app/dataland-data-exporter/build/libs/dataland-data-exporter-0.0.1-SNAPSHOT.jar /jar/dataland-data-exporter.jar
CMD ["java","-jar","/jar/dataland-data-exporter.jar"]

HEALTHCHECK --interval=30s --timeout=3s CMD kill -0 1
