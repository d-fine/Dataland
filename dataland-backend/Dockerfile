ARG DATALAND_BACKEND_BASE_VERSION
FROM ghcr.io/d-fine/dataland/dataland_backend_base:${DATALAND_BACKEND_BASE_VERSION} as builder

FROM eclipse-temurin:17.0.6_10-jre-alpine
COPY --from=builder /app/dataland-backend/build/libs/dataland-backend-0.0.1-SNAPSHOT.jar /jar/dataland-backend.jar
CMD ["java","-jar","/jar/dataland-backend.jar"]

HEALTHCHECK --interval=30s --timeout=3s CMD wget -nv -O- -t 1 http://localhost:8080/api/actuator/health | tee /dev/stderr | grep -q '^{"status":"UP"' || false
