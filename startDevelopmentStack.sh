#!/bin/bash
docker login ghcr.io -u $DATALAND_SKYMINDERCLIENT_USER -p $DATALAND_SKYMINDERCLIENT_TOKEN
#start containers for skyminder and edc-dummyserver
export NGINX_CONFIG_FILE="./dataland-inbound-proxy/nginx-dev.conf"
docker-compose --profile development down
docker-compose --profile development pull
docker-compose --profile development up -d --build
#start the backend
./gradlew dataland-frontend:generateAPIClientFrontend --no-daemon --stacktrace
./gradlew dataland-backend:bootRun --args='--spring.profiles.active=development' --no-daemon --stacktrace

