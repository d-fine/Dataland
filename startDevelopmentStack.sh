#!/bin/bash
# Login to the docker repository
docker login ghcr.io -u $GITHUB_USER -p $GITHUB_TOKEN

# Retrieve the SSL-Certificates for dataland-local.duckdns.org
mkdir -p ./local/certs
scp ubuntu@dataland-letsencrypt.duckdns.org:/etc/letsencrypt/live/dataland-local.duckdns.org/* ./local/certs


./gradlew dataland-keycloak:dataland_theme:login:buildTheme --no-daemon --stacktrace
./gradlew dataland-frontend:generateAPIClientFrontend --no-daemon --stacktrace

#start containers with the stack except frontend and backend
docker-compose --profile development down
docker-compose --profile development pull
docker-compose --profile development up -d --build

#start the backend
./gradlew dataland-backend:bootRun --args='--spring.profiles.active=development' --no-daemon --stacktrace

