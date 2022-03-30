# build stage
ARG DATALAND_SKYMINDER_USER
ARG DATALAND_SKYMINDER_TOKEN
ARG DATALAND_EDC_USER
ARG DATALAND_EDC_TOKEN
FROM ghcr.io/d-fine/dataland/temurinbase:latest as prepare-stage
WORKDIR /app
COPY . .
RUN DATALAND_SKYMINDERCLIENT_USER=${DATALAND_SKYMINDERCLIENT_USER} \
    DATALAND_SKYMINDERCLIENT_TOKEN=${DATALAND_SKYMINDERCLIENT_TOKEN} \
    DATALAND_EDC_USER=${DATALAND_EDC_USER} \
    DATALAND_EDC_TOKEN=${DATALAND_EDC_TOKEN} \
    ./gradlew dataland-frontend:generateAPIClientFrontend --no-daemon
# build stage
FROM node:lts-alpine as build-stage
WORKDIR /app
COPY dataland-frontend/package.json /app/
RUN npm install
COPY dataland-frontend /app/
COPY --from=prepare-stage /app/dataland-frontend/src/clients /app/src/clients
RUN npm run build
# production stage
FROM nginx:stable-alpine as production-stage
COPY --from=build-stage /app/dist /usr/share/nginx/html
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]