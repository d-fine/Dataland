#!/bin/bash
# Login to the docker repository
set -euxo pipefail

docker login ghcr.io -u $GITHUB_USER -p $GITHUB_TOKEN

# Retrieve the SSL-Certificates for local-dev.dataland.com
mkdir -p ./local/certs
scp ubuntu@letsencrypt.dataland.com:/etc/letsencrypt/live/local-dev.dataland.com/* ./local/certs

rm ./*github_env.log || true
find ./build-utils/ -name "rebuild*.sh" -exec bash -c 'eval "$1"' shell {} \;

set -o allexport
source ./*github_env.log
set +o allexport

# start containers with the stack except frontend and backend
docker compose --profile development down
docker compose --profile development pull
docker compose --profile development up -d --build

#start the backend
./gradlew dataland-backend:bootRun --args='--spring.profiles.active=development' --no-daemon --stacktrace

