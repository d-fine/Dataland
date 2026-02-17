ARG DATALAND_BATCH_MANAGER_BASE_VERSION
FROM ghcr.io/d-fine/dataland/dataland_batch_manager_base:${DATALAND_BATCH_MANAGER_BASE_VERSION} AS builder

FROM eclipse-temurin:21.0.9_10-jre-alpine@sha256:08eecc477dbe3f2e33daac27f36e41daf7f4ec51d2f3396006e54fa41832c74c
COPY ./dataland-batch-manager/src/main/resources/NorthdataTestData.zip ./NorthdataTestData.zip
COPY --from=builder /app/dataland-batch-manager/build/libs/dataland-batch-manager-0.0.1-SNAPSHOT.jar /jar/dataland-batch-manager.jar
CMD ["java","-jar","/jar/dataland-batch-manager.jar"]

HEALTHCHECK --interval=30s --timeout=3s CMD kill -0 1
