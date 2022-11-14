ARG DATALAND_BACKEND_BASE_VERSION
FROM ghcr.io/d-fine/dataland/dataland_backend_base:${DATALAND_BACKEND_BASE_VERSION} as builder

FROM eclipse-temurin:17.0.4.1_1-jre-alpine
COPY --from=builder /app/dataland-backend/build/libs/dataland-backend*.jar /jar/dataland-backend.jar
CMD ["java","-jar","/jar/dataland-backend.jar"]
