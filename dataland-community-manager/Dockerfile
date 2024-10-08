ARG DATALAND_COMMUNITY_MANAGER_BASE_VERSION
FROM ghcr.io/d-fine/dataland/dataland_community_manager_base:${DATALAND_COMMUNITY_MANAGER_BASE_VERSION} AS builder

FROM eclipse-temurin:21.0.4_7-jre-alpine@sha256:8cc1202a100e72f6e91bf05ab274b373a5def789ab6d9e3e293a61236662ac27
COPY --from=builder /app/dataland-community-manager/build/libs/dataland-community-manager-0.0.1-SNAPSHOT.jar /jar/dataland-community-manager.jar
CMD ["java","-jar","/jar/dataland-community-manager.jar"]

HEALTHCHECK --interval=30s --timeout=3s CMD wget -nv -O- -t 1 http://localhost:8080/community/actuator/health | tee /dev/stderr | grep -q '^{"status":"UP"' || false
