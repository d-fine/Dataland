ARG DATALAND_BATCH_MANAGER_BASE_VERSION
FROM ghcr.io/d-fine/dataland/dataland_batch_manager_base:${DATALAND_BATCH_MANAGER_BASE_VERSION} AS builder

FROM eclipse-temurin:21.0.5_11-jre-alpine@sha256:4300bfe1e11f3dfc3e3512f39939f9093cf18d0e581d1ab1ccd0512f32fe33f0
COPY ./dataland-batch-manager/src/main/resources/NorthdataTestData.zip ./NorthdataTestData.zip
COPY --from=builder /app/dataland-batch-manager/build/libs/dataland-batch-manager-0.0.1-SNAPSHOT.jar /jar/dataland-batch-manager.jar
CMD ["java","-jar","/jar/dataland-batch-manager.jar"]

HEALTHCHECK --interval=30s --timeout=3s CMD kill -0 1
