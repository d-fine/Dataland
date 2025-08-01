# build stage
ARG DATALAND_GRADLE_BASE_VERSION
FROM ghcr.io/d-fine/dataland/dataland_gradle_base:${DATALAND_GRADLE_BASE_VERSION} as build-stage
WORKDIR /app

COPY ./dataland-backend/backendOpenApi.json ./dataland-backend/backendOpenApi.json
COPY ./dataland-api-key-manager/apiKeyManagerOpenApi.json ./dataland-api-key-manager/apiKeyManagerOpenApi.json
COPY ./dataland-document-manager/documentManagerOpenApi.json ./dataland-document-manager/documentManagerOpenApi.json
COPY ./dataland-qa-service/qaServiceOpenApi.json ./dataland-qa-service/qaServiceOpenApi.json
COPY ./dataland-community-manager/communityManagerOpenApi.json ./dataland-community-manager/communityManagerOpenApi.json
COPY ./dataland-email-service/emailServiceOpenApi.json ./dataland-email-service/emailServiceOpenApi.json
COPY ./dataland-user-service/userServiceOpenApi.json ./dataland-user-service/userServiceOpenApi.json
COPY ./dataland-frontend/ ./dataland-frontend/
COPY ./.git/ ./.git

ARG DOCUMENT_UPLOAD_MAX_FILE_SIZE_IN_MEGABYTES
ENV DOCUMENT_UPLOAD_MAX_FILE_SIZE_IN_MEGABYTES=${DOCUMENT_UPLOAD_MAX_FILE_SIZE_IN_MEGABYTES}
ARG MAX_NUMBER_OF_DAYS_SELECTABLE_FOR_API_KEY_VALIDITY
ENV MAX_NUMBER_OF_DAYS_SELECTABLE_FOR_API_KEY_VALIDITY=${MAX_NUMBER_OF_DAYS_SELECTABLE_FOR_API_KEY_VALIDITY}
ARG DATA_REQUEST_UPLOAD_MAX_FILE_SIZE_IN_MEGABYTES
ENV DATA_REQUEST_UPLOAD_MAX_FILE_SIZE_IN_MEGABYTES=${DATA_REQUEST_UPLOAD_MAX_FILE_SIZE_IN_MEGABYTES}
ARG MAX_NUMBER_OF_DATA_REQUESTS_PER_DAY_FOR_ROLE_USER
ENV MAX_NUMBER_OF_DATA_REQUESTS_PER_DAY_FOR_ROLE_USER=${MAX_NUMBER_OF_DATA_REQUESTS_PER_DAY_FOR_ROLE_USER}
RUN ./gradlew dataland-frontend:generateClients dataland-frontend:npm_run_build --no-daemon --stacktrace

FROM nginx:1.29.0@sha256:84ec966e61a8c7846f509da7eb081c55c1d56817448728924a87ab32f12a72fb as production-stage
COPY --from=build-stage /app/dataland-frontend/dist /usr/share/nginx/html
COPY ./dataland-frontend/default.conf /etc/nginx/conf.d/default.conf
COPY ./dataland-inbound-proxy/config/utils/securityHeaderMap.conf /etc/nginx/utils/securityHeaderMap.conf
COPY ./dataland-inbound-proxy/config/utils/security.conf /etc/nginx/utils/security.conf

CMD ["nginx", "-g", "daemon off;"]

HEALTHCHECK --interval=30s --timeout=3s CMD curl -f http://localhost/health/frontend | tee /dev/stderr | grep -q 'UP' || false
