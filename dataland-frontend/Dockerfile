# build stage
ARG DATALAND_GRADLE_BASE_VERSION
FROM ghcr.io/d-fine/dataland/dataland_gradle_base:${DATALAND_GRADLE_BASE_VERSION} as build-stage
WORKDIR /app

COPY ./dataland-backend/backendOpenApi.json ./dataland-backend/backendOpenApi.json
COPY ./dataland-api-key-manager/apiKeyManagerOpenApi.json ./dataland-api-key-manager/apiKeyManagerOpenApi.json
COPY ./dataland-frontend/ ./dataland-frontend/
COPY ./.git/ ./.git

RUN ./gradlew dataland-frontend:generateClients dataland-frontend:npm_run_build --no-daemon --stacktrace

FROM nginx:1.23.2 as production-stage
COPY --from=build-stage /app/dataland-frontend/dist /usr/share/nginx/html
COPY ./dataland-frontend/default.conf /etc/nginx/conf.d/default.conf
COPY ./dataland-inbound-proxy/config/utils/securityHeaderMap.conf /etc/nginx/utils/securityHeaderMap.conf
COPY ./dataland-inbound-proxy/config/utils/security.conf /etc/nginx/utils/security.conf
COPY ./dataland-frontend/src/assets/excel-files /etc/nginx/utils/excel-files

CMD ["nginx", "-g", "daemon off;"]

HEALTHCHECK --interval=30s --timeout=3s CMD curl -f http://localhost/health/frontend | tee /dev/stderr | grep -q 'UP' || false
