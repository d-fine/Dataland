ARG DATALAND_SPECIFICATION_SERVICE_BASE_VERSION
FROM ghcr.io/d-fine/dataland/dataland_specification_service_base:${DATALAND_SPECIFICATION_SERVICE_BASE_VERSION} AS builder

FROM eclipse-temurin:21.0.8_9-jre-alpine@sha256:990397e0495ac088ab6ee3d949a2e97b715a134d8b96c561c5d130b3786a489d
COPY --from=builder /app/dataland-specification-service/build/libs/dataland-specification-service-0.0.1-SNAPSHOT.jar /jar/dataland-specification-service.jar
COPY ./dataland-specification-service/src/main/resources/specifications /specifications
CMD ["java", \
     "-Xms128m", \
     "-Xmx384m", \
     "-XX:+UseContainerSupport", \
     "-XX:MaxRAMPercentage=75.0", \
     "-XX:+TieredCompilation", \
     "-jar", "/jar/dataland-specification-service.jar"]

HEALTHCHECK --interval=30s --timeout=3s CMD wget -nv -O- -t 1 http://localhost:8489/specifications/actuator/health | tee /dev/stderr | grep -q '^{"status":"UP"' || false
