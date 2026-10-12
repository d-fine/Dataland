ARG DATALAND_DOCUMENT_MANAGER_BASE_VERSION
FROM ghcr.io/d-fine/dataland/dataland_document_manager_base:${DATALAND_DOCUMENT_MANAGER_BASE_VERSION} AS builder

FROM eclipse-temurin:21.0.12_8-jre-alpine@sha256:1a29e1fe337eb28b5bec30f0ee8ed29f0ff80ab6f75dcf9313efe82911065a52
RUN apk add --no-cache libreoffice-writer libreoffice-impress
COPY --from=builder /app/dataland-document-manager/build/libs/dataland-document-manager-0.0.1-SNAPSHOT.jar /jar/dataland-document-manager.jar
CMD ["java","-jar","/jar/dataland-document-manager.jar"]

HEALTHCHECK --interval=30s --timeout=5s --start-period=180s --start-interval=60s --retries=6 CMD wget -nv -O- -t 1 http://document-manager:8080/documents/actuator/health | tee /dev/stderr | grep -q '^{"status":"UP"' || false
