# build stage
ARG DATALAND_GRADLE_BASE_VERSION
FROM ghcr.io/d-fine/dataland/dataland_gradle_base:${DATALAND_GRADLE_BASE_VERSION} as build-stage
WORKDIR /app

COPY ./dataland-backend/ ./dataland-backend
COPY ./dataland-frontend/ ./dataland-frontend/
COPY ./.git/ ./.git

RUN --mount=type=secret,id=DOCKER_SECRET,target=/root/.gradle/gradle.properties,required=true \
     ./gradlew dataland-frontend:generateAPIClientFrontend dataland-frontend:npm_run_build --no-daemon --stacktrace

FROM nginx:1.23.2 as production-stage
COPY --from=build-stage /app/dataland-frontend/dist /usr/share/nginx/html
COPY ./dataland-frontend/default.conf /etc/nginx/conf.d/default.conf
COPY ./dataland-inbound-proxy/config/utils/securityHeaderMap.conf /etc/nginx/utils/securityHeaderMap.conf
COPY ./dataland-inbound-proxy/config/utils/security.conf /etc/nginx/utils/security.conf
CMD ["nginx", "-g", "daemon off;"]

HEALTHCHECK --interval=30s --timeout=3s CMD curl -f http://localhost/health/frontend | grep -q 'UP'
