ARG DATALAND_QA_SERVICE_BASE_VERSION
FROM ghcr.io/d-fine/dataland/dataland_qa_service_base:${DATALAND_QA_SERVICE_BASE_VERSION} AS builder

FROM eclipse-temurin:21.0.8_9-jre-alpine@sha256:4ca7eff3ab0ef9b41f5fefa35efaeda9ed8d26e161e1192473b24b3a6c348aef
COPY --from=builder /app/dataland-qa-service/build/libs/dataland-qa-service-0.0.1-SNAPSHOT.jar /jar/dataland-qa-service.jar
CMD ["java","-jar","/jar/dataland-qa-service.jar"]

HEALTHCHECK --interval=30s --timeout=3s CMD wget -nv -O- -t 1 http://localhost:8080/qa/actuator/health | tee /dev/stderr | grep -q '^{"status":"UP"' || false
