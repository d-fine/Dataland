ARG DATALAND_COMMUNITY_MANAGER_BASE_VERSION
FROM ghcr.io/d-fine/dataland/dataland_community_manager_base:${DATALAND_COMMUNITY_MANAGER_BASE_VERSION} AS builder

FROM eclipse-temurin:21.0.8_9-jre-alpine@sha256:990397e0495ac088ab6ee3d949a2e97b715a134d8b96c561c5d130b3786a489d
COPY --from=builder /app/dataland-community-manager/build/libs/dataland-community-manager-0.0.1-SNAPSHOT.jar /jar/dataland-community-manager.jar
CMD ["java", \
     "-Xms256m", \
     "-Xmx512m", \
     "-XX:+UseContainerSupport", \
     "-XX:MaxRAMPercentage=75.0", \
     "-XX:+TieredCompilation", \
     "-jar", "/jar/dataland-community-manager.jar"]

HEALTHCHECK --interval=30s --timeout=3s CMD wget -nv -O- -t 1 http://localhost:8080/community/actuator/health | tee /dev/stderr | grep -q '^{"status":"UP"' || false
