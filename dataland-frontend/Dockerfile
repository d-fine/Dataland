# build stage
FROM ghcr.io/d-fine/dataland/cypressbase:latest as build-stage
WORKDIR /app
COPY . .
RUN ./gradlew dataland-frontend:generateAPIClientFrontend dataland-frontend:npm_run_build --no-daemon
# build stage
#FROM node:lts-alpine as build-stage
#WORKDIR /app
#COPY dataland-frontend/package.json /app/
#RUN npm install
#COPY dataland-frontend /app/
#COPY --from=prepare-stage /app/dataland-frontend/src/clients /app/src/clients
#RUN npm run build
# production stage
FROM nginx:stable-alpine as production-stage
COPY --from=build-stage /app/dataland-frontend/dist /usr/share/nginx/html
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]