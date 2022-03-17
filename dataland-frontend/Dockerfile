# build stage
FROM ghcr.io/d-fine/dataland/temurinbase:latest as prepare-stage
WORKDIR /app
COPY . .
RUN ./gradlew dataland-frontend:generateAPIClientFrontend --no-deamon
# build stage
FROM node:lts-alpine as build-stage


WORKDIR /app
COPY dataland-frontend/package*.json /app
RUN npm install
COPY dataland-frontend /app
COPY --from=prepare-stage /app/dataland-frontend/src/clients /app/src
RUN npm run build
# production stage
FROM nginx:stable-alpine as production-stage
COPY --from=build-stage /app/dist /usr/share/nginx/html
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]