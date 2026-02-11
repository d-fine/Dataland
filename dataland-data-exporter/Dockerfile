ARG DATALAND_DATA_EXPORTER_BASE_VERSION
FROM ghcr.io/d-fine/dataland/dataland_data_exporter_base:${DATALAND_DATA_EXPORTER_BASE_VERSION} AS builder

FROM eclipse-temurin:21.0.9_10-jre-alpine@sha256:08eecc477dbe3f2e33daac27f36e41daf7f4ec51d2f3396006e54fa41832c74c
COPY --from=builder /app/dataland-data-exporter/build/libs/dataland-data-exporter-0.0.1-SNAPSHOT.jar /jar/dataland-data-exporter.jar
CMD ["java","-jar","/jar/dataland-data-exporter.jar"]

HEALTHCHECK --interval=30s --timeout=3s CMD kill -0 1
