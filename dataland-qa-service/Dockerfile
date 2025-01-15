ARG DATALAND_QA_SERVICE_BASE_VERSION
FROM ghcr.io/d-fine/dataland/dataland_qa_service_base:${DATALAND_QA_SERVICE_BASE_VERSION} AS builder

FROM eclipse-temurin:21.0.5_11-jre-alpine@sha256:4300bfe1e11f3dfc3e3512f39939f9093cf18d0e581d1ab1ccd0512f32fe33f0
COPY --from=builder /app/dataland-qa-service/build/libs/dataland-qa-service-0.0.1-SNAPSHOT.jar /jar/dataland-qa-service.jar
CMD ["java","-jar","/jar/dataland-qa-service.jar"]

HEALTHCHECK --interval=30s --timeout=3s CMD wget -nv -O- -t 1 http://localhost:8080/qa/actuator/health | tee /dev/stderr | grep -q '^{"status":"UP"' || false
