ARG DATALAND_BATCH_MANAGER_BASE_VERSION
FROM ghcr.io/d-fine/dataland/dataland_batch_manager_base:${DATALAND_BATCH_MANAGER_BASE_VERSION} AS builder

FROM eclipse-temurin:21.0.5_11-jre-alpine@sha256:decee204b9a1eb333c364ba4d859a6b1380eb13f0980d2acfd65c09fee53a48a
COPY ./dataland-batch-manager/src/main/resources/NorthdataTestData.zip ./NorthdataTestData.zip
COPY --from=builder /app/dataland-batch-manager/build/libs/dataland-batch-manager-0.0.1-SNAPSHOT.jar /jar/dataland-batch-manager.jar
CMD ["java","-jar","/jar/dataland-batch-manager.jar"]

HEALTHCHECK --interval=30s --timeout=3s CMD kill -0 1
