# build stage
FROM ghcr.io/d-fine/dataland/temurinbase:latest as prepare-stage
WORKDIR /app
COPY . .
RUN ./gradlew dataland-frontend:generateAPIClientFrontend
# build stage
FROM node:lts-alpine as build-stage
WORKDIR /app/dataland-frontend
COPY dataland-frontend/package*.json .
RUN ls -hartl
RUN npm install
COPY dataland-frontend dataland-frontend
RUN ls -hartl
COPY --from=prepare-stage /app/dataland-frontend/src/clients ./src
RUN ls -hartl
RUN npm run build
# production stage
FROM nginx:stable-alpine as production-stage
COPY --from=build-stage /app/dist /usr/share/nginx/html
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]