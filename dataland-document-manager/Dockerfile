ARG DATALAND_DOCUMENT_MANAGER_BASE_VERSION
FROM ghcr.io/d-fine/dataland/dataland_document_manager_base:${DATALAND_DOCUMENT_MANAGER_BASE_VERSION} AS builder

FROM eclipse-temurin:21.0.5_11-jre-alpine@sha256:decee204b9a1eb333c364ba4d859a6b1380eb13f0980d2acfd65c09fee53a48a
RUN apk add --no-cache libreoffice-writer libreoffice-impress
COPY --from=builder /app/dataland-document-manager/build/libs/dataland-document-manager-0.0.1-SNAPSHOT.jar /jar/dataland-document-manager.jar
CMD ["java","-jar","/jar/dataland-document-manager.jar"]

HEALTHCHECK --interval=30s --timeout=3s CMD wget -nv -O- -t 1 http://document-manager:8080/documents/actuator/health | tee /dev/stderr | grep -q '^{"status":"UP"' || false
