ARG DATALAND_USER_SERVICE_BASE_VERSION
FROM ghcr.io/d-fine/dataland/dataland_user_service_base:${DATALAND_USER_SERVICE_BASE_VERSION} AS builder

FROM eclipse-temurin:21.0.11_10-jre-alpine@sha256:3f08b13888f595cc49edabea7250ba69499ba25602b267da591720769400e08c
COPY --from=builder /app/dataland-user-service/build/libs/dataland-user-service-0.0.1-SNAPSHOT.jar /jar/dataland-user-service.jar
CMD ["java","-jar","/jar/dataland-user-service.jar"]

HEALTHCHECK --interval=30s --timeout=3s CMD wget -nv -O- -t 1 http://localhost:8080/users/actuator/health | tee /dev/stderr | grep -q '^{"status":"UP"' || false
