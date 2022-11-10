#!/bin/bash
# Login to the docker repository
set -euxo pipefail

docker login ghcr.io -u $GITHUB_USER -p $GITHUB_TOKEN

# Retrieve the SSL-Certificates for local-dev.dataland.com
mkdir -p ./local/certs
scp ubuntu@letsencrypt.dataland.com:/etc/letsencrypt/live/local-dev.dataland.com/* ./local/certs


./gradlew dataland-keycloak:dataland_theme:login:buildTheme --no-daemon --stacktrace
./gradlew dataland-frontend:generateAPIClientFrontend --no-daemon --stacktrace

# start containers with the stack except frontend and backend
docker compose --profile development down
docker volume rm dataland_pgadmin_config || true
docker compose --profile development pull
docker compose --profile development up -d --build

#start the backend
./gradlew dataland-backend:bootRun --args='--spring.profiles.active=development' --no-daemon --stacktrace

