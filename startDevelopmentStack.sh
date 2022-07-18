#!/bin/bash
# Login to the docker repository
docker login ghcr.io -u $DATALAND_SKYMINDERCLIENT_USER -p $DATALAND_SKYMINDERCLIENT_TOKEN

# Retrieve the SSL-Certificates for dataland-local.duckdns.org
mkdir -p ./local/certs
scp ubuntu@dataland-letsencrypt.duckdns.org:/etc/letsencrypt/live/dataland-local.duckdns.org/* ./local/certs

#start containers for skyminder and edc-dummyserver
docker-compose --profile development down
docker-compose --profile development pull
docker-compose --profile development up -d --build

#start the backend
./gradlew dataland-frontend:generateAPIClientFrontend --no-daemon --stacktrace
./gradlew dataland-backend:bootRun --args='--spring.profiles.active=development' --no-daemon --stacktrace

