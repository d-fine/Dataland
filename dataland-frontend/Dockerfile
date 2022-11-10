# build stage
FROM eclipse-temurin:17.0.4.1_1-jdk-jammy as build-stage
ARG GITHUB_USER
ARG GITHUB_TOKEN
WORKDIR /app
COPY . .
RUN GITHUB_USER=${GITHUB_USER} \
    GITHUB_TOKEN=${GITHUB_TOKEN} \
    ./gradlew dataland-frontend:generateAPIClientFrontend dataland-frontend:npm_run_build --no-daemon --stacktrace
FROM nginx:1.23.1 as production-stage
COPY --from=build-stage /app/dataland-frontend/dist /usr/share/nginx/html
COPY ./dataland-frontend/default.conf /etc/nginx/conf.d/default.conf
COPY ./dataland-inbound-proxy/config/utils/securityHeaderMap.conf /etc/nginx/utils/securityHeaderMap.conf
COPY ./dataland-inbound-proxy/config/utils/security.conf /etc/nginx/utils/security.conf
CMD ["nginx", "-g", "daemon off;"]
