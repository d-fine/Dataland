ARG DATALAND_COMMUNITY_MANAGER_BASE_VERSION
FROM ghcr.io/d-fine/dataland/dataland_community_manager_base:${DATALAND_COMMUNITY_MANAGER_BASE_VERSION} AS builder

FROM eclipse-temurin:21.0.9_10-jre-alpine@sha256:08eecc477dbe3f2e33daac27f36e41daf7f4ec51d2f3396006e54fa41832c74c
COPY --from=builder /app/dataland-community-manager/build/libs/dataland-community-manager-0.0.1-SNAPSHOT.jar /jar/dataland-community-manager.jar
CMD ["java","-jar","/jar/dataland-community-manager.jar"]

HEALTHCHECK --interval=30s --timeout=3s CMD wget -nv -O- -t 1 http://localhost:8080/community/actuator/health | tee /dev/stderr | grep -q '^{"status":"UP"' || false
