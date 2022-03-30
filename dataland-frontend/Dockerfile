# build stage
FROM ghcr.io/d-fine/dataland/cypressbase:latest as build-stage
WORKDIR /app
COPY . .
RUN ./gradlew dataland-frontend:generateAPIClientFrontend dataland-frontend:npm_run_build --no-daemon
FROM nginx:stable-alpine as production-stage
COPY --from=build-stage /app/dist /usr/share/nginx/html
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]