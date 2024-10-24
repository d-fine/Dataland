ARG DATALAND_BATCH_MANAGER_BASE_VERSION
FROM ghcr.io/d-fine/dataland/dataland_batch_manager_base:${DATALAND_BATCH_MANAGER_BASE_VERSION} AS builder

FROM eclipse-temurin:21.0.5_11-jre-alpine@sha256:03c1fb6fff9b28aa3be69f59df0a274c2c3984189726645cb383217229b25082
COPY ./dataland-batch-manager/src/main/resources/NorthdataTestData.zip ./NorthdataTestData.zip
COPY --from=builder /app/dataland-batch-manager/build/libs/dataland-batch-manager-0.0.1-SNAPSHOT.jar /jar/dataland-batch-manager.jar
CMD ["java","-jar","/jar/dataland-batch-manager.jar"]

HEALTHCHECK --interval=30s --timeout=3s CMD kill -0 1
