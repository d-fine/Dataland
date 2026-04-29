ARG DATALAND_DATA_EXPORTER_BASE_VERSION
FROM ghcr.io/d-fine/dataland/dataland_data_exporter_base:${DATALAND_DATA_EXPORTER_BASE_VERSION} AS builder

FROM eclipse-temurin:21.0.10_7-jre-alpine@sha256:6ad8ed080d9be96b61438ec3ce99388e294af216ed57356000c06070e85c5d5d
COPY --from=builder /app/dataland-data-exporter/build/libs/dataland-data-exporter-0.0.1-SNAPSHOT.jar /jar/dataland-data-exporter.jar
CMD ["java","-jar","/jar/dataland-data-exporter.jar"]

HEALTHCHECK --interval=30s --timeout=3s CMD kill -0 1
