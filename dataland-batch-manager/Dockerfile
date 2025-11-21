ARG DATALAND_BATCH_MANAGER_BASE_VERSION
FROM ghcr.io/d-fine/dataland/dataland_batch_manager_base:${DATALAND_BATCH_MANAGER_BASE_VERSION} AS builder

FROM eclipse-temurin:21.0.8_9-jre-alpine@sha256:990397e0495ac088ab6ee3d949a2e97b715a134d8b96c561c5d130b3786a489d
COPY ./dataland-batch-manager/src/main/resources/NorthdataTestData.zip ./NorthdataTestData.zip
COPY --from=builder /app/dataland-batch-manager/build/libs/dataland-batch-manager-0.0.1-SNAPSHOT.jar /jar/dataland-batch-manager.jar
CMD ["java", \
     "-Xms128m", \
     "-Xmx384m", \
     "-XX:+UseContainerSupport", \
     "-XX:MaxRAMPercentage=75.0", \
     "-XX:+TieredCompilation", \
     "-jar", "/jar/dataland-batch-manager.jar"]

HEALTHCHECK --interval=30s --timeout=3s CMD kill -0 1
