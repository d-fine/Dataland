#!/bin/bash
# Login to the docker repository
set -euxo pipefail

docker login ghcr.io -u $GITHUB_USER -p $GITHUB_TOKEN

# Retrieve the SSL-Certificates for dataland-local.duckdns.org
mkdir -p ./local/certs
scp ubuntu@dataland-letsencrypt.duckdns.org:/etc/letsencrypt/live/dataland-local.duckdns.org/* ./local/certs

./build-utils/rebuild_dataland_images.sh

set -o allexport
source ./github_env.log
set +o allexport

# start containers with the stack except frontend and backend
docker compose --profile development down
docker compose --profile development pull
docker compose --profile development up -d --build

#start the backend
./gradlew dataland-backend:bootRun --args='--spring.profiles.active=development' --no-daemon --stacktrace

