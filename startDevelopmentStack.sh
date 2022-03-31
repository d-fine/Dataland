#!/bin/bash
#start containers for skyminder and edc-dummyserver
docker-compose --profile development up -d
#start the backend
./gradlew dataland-frontend:generateAPIClientFrontend dataland-backend:bootRun --args='--spring.profiles.active=dev' --no-daemon

