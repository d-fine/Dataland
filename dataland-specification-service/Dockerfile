ARG DATALAND_SPECIFICATION_SERVICE_BASE_VERSION
FROM ghcr.io/d-fine/dataland/dataland_specification_service_base:${DATALAND_SPECIFICATION_SERVICE_BASE_VERSION} AS builder

FROM eclipse-temurin:21.0.6_7-jre-alpine@sha256:4c07db858c3b8bfed4cb9163f4aeedbff9c0c2b8212ec1fa75c13a169dec8dc6
COPY --from=builder /app/dataland-specification-service/build/libs/dataland-specification-service-0.0.1-SNAPSHOT.jar /jar/dataland-specification-service.jar
COPY ./dataland-specification-service/src/main/resources/specifications /specifications
CMD ["java","-jar","/jar/dataland-specification-service.jar"]

HEALTHCHECK --interval=30s --timeout=3s CMD wget -nv -O- -t 1 http://localhost:8489/specifications/actuator/health | tee /dev/stderr | grep -q '^{"status":"UP"' || false
