ARG DATALAND_DOCUMENT_MANAGER_BASE_VERSION
FROM ghcr.io/d-fine/dataland/dataland_document_manager_base:${DATALAND_DOCUMENT_MANAGER_BASE_VERSION} AS builder

FROM eclipse-temurin:21.0.11_10-jre-alpine@sha256:3f08b13888f595cc49edabea7250ba69499ba25602b267da591720769400e08c
RUN apk add --no-cache libreoffice-writer libreoffice-impress
COPY --from=builder /app/dataland-document-manager/build/libs/dataland-document-manager-0.0.1-SNAPSHOT.jar /jar/dataland-document-manager.jar
CMD ["java","-jar","/jar/dataland-document-manager.jar"]

HEALTHCHECK --interval=30s --timeout=3s CMD wget -nv -O- -t 1 http://document-manager:8080/documents/actuator/health | tee /dev/stderr | grep -q '^{"status":"UP"' || false
