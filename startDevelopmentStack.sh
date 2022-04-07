#!/bin/bash
docker login ghcr.io -u $DATALAND_SKYMINDERCLIENT_USER -p $DATALAND_SKYMINDERCLIENT_TOKEN
#start containers for skyminder and edc-dummyserver
PROXY_NGINX_CONFIG="/dataland-inbound-proxy/nginxdev.conf" docker-compose --profile development up -d
#start the backend
./gradlew dataland-frontend:generateAPIClientFrontend dataland-backend:bootRun --args='--spring.profiles.active=dev' --no-daemon

