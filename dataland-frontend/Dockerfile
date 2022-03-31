# build stage
ARG DATALAND_EDC_USER
ARG DATALAND_EDC_TOKEN
FROM ghcr.io/d-fine/dataland/cypressbase:latest as build-stage
WORKDIR /app
COPY . .
RUN DATALAND_EDC_USER=${DATALAND_EDC_USER} \
    DATALAND_EDC_TOKEN=${DATALAND_EDC_TOKEN} \
    ./gradlew dataland-frontend:generateAPIClientFrontend dataland-frontend:npm_run_build --no-daemon
FROM nginx:stable-alpine as production-stage
COPY --from=build-stage /app/dataland-frontend/dist /usr/share/nginx/html
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]