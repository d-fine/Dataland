# build stage
ARG DATALAND_GRADLE_BASE_VERSION
FROM dataland_gradle_base:${DATALAND_GRADLE_BASE_VERSION} as build-stage
ARG GITHUB_USER
ARG GITHUB_TOKEN
WORKDIR /app

COPY ./dataland-backend/ /app/dataland-backend
COPY ./dataland-frontend/ /app/dataland-frontend/
COPY ./.git/ /app/.git

RUN GITHUB_USER=${GITHUB_USER} \
    GITHUB_TOKEN=${GITHUB_TOKEN} \
    ./gradlew dataland-frontend:generateAPIClientFrontend dataland-frontend:npm_run_build --no-daemon --stacktrace

FROM nginx:1.23.2 as production-stage
COPY --from=build-stage /app/dataland-frontend/dist /usr/share/nginx/html
COPY ./dataland-frontend/default.conf /etc/nginx/conf.d/default.conf
COPY ./dataland-inbound-proxy/config/utils/securityHeaderMap.conf /etc/nginx/utils/securityHeaderMap.conf
COPY ./dataland-inbound-proxy/config/utils/security.conf /etc/nginx/utils/security.conf
CMD ["nginx", "-g", "daemon off;"]
