ARG DATALAND_USER_SERVICE_BASE_VERSION
FROM ghcr.io/d-fine/dataland/dataland_user_service_base:${DATALAND_USER_SERVICE_BASE_VERSION} AS builder

FROM eclipse-temurin:21.0.6_7-jre-alpine@sha256:4e9ab608d97796571b1d5bbcd1c9f430a89a5f03fe5aa6c093888ceb6756c502
COPY --from=builder /app/dataland-user-service/build/libs/dataland-user-service-0.0.1-SNAPSHOT.jar /jar/dataland-user-service.jar
CMD ["java","-jar","/jar/dataland-user-service.jar"]

HEALTHCHECK --interval=30s --timeout=3s CMD wget -nv -O- -t 1 http://localhost:8080/users/actuator/health | tee /dev/stderr | grep -q '^{"status":"UP"' || false
