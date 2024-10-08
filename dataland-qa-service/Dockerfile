ARG DATALAND_QA_SERVICE_BASE_VERSION
FROM ghcr.io/d-fine/dataland/dataland_qa_service_base:${DATALAND_QA_SERVICE_BASE_VERSION} AS builder

FROM eclipse-temurin:21.0.4_7-jre-alpine@sha256:8cc1202a100e72f6e91bf05ab274b373a5def789ab6d9e3e293a61236662ac27
COPY --from=builder /app/dataland-qa-service/build/libs/dataland-qa-service-0.0.1-SNAPSHOT.jar /jar/dataland-qa-service.jar
CMD ["java","-jar","/jar/dataland-qa-service.jar"]

HEALTHCHECK --interval=30s --timeout=3s CMD wget -nv -O- -t 1 http://localhost:8080/qa/actuator/health | tee /dev/stderr | grep -q '^{"status":"UP"' || false
