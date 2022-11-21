ARG DATALAND_BACKEND_BASE_VERSION
FROM ghcr.io/d-fine/dataland/dataland_backend_base:${DATALAND_BACKEND_BASE_VERSION} as builder

FROM eclipse-temurin:17.0.5_8-jre-alpine
COPY --from=builder /app/dataland-backend/build/libs/dataland-backend-0.0.1-SNAPSHOT.jar /jar/dataland-backend.jar
CMD ["java","-jar","/jar/dataland-backend.jar"]

# Install JQ (Json parsing) for Health Check
RUN apk add --no-cache jq curl

HEALTHCHECK --interval=30s --timeout=3s CMD curl -f http://localhost:8080/api/actuator/health/ping | jq ".status" | grep -q 'UP'
