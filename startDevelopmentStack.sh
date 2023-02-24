#!/bin/bash
# Login to the docker repository
#set -euxo pipefail
./verifyEnvironmentVariables.sh

docker login ghcr.io -u $GITHUB_USER -p $GITHUB_TOKEN

# Retrieve the SSL-Certificates for local-dev.dataland.com
mkdir -p ./local/certs
scp ubuntu@letsencrypt.dataland.com:/etc/letsencrypt/live/local-dev.dataland.com/* ./local/certs

rm ./*github_env.log || true
./build-utils/base_rebuild_gradle_dockerfile.sh
set -o allexport
source ./*github_env.log
set +o allexport

find ./build-utils/ -name "rebuild*.sh" ! -name "*prod*" ! -name "*test*" ! -name "*backend*" -exec bash -c 'eval "$1" && echo "SUCCESS - execution of $1 was successful" || echo "ERROR - could not execute $1"' shell {} \;

set -o allexport
source ./*github_env.log
set +o allexport

# start containers with the stack except frontend and backend
docker compose --profile development down
docker volume rm $(docker volume ls -q | grep _pgadmin_config) || true
docker compose --profile development pull --ignore-pull-failures --include-deps
docker compose --profile development up -d --build

#start the backend
./gradlew dataland-backend:bootRun --args='--spring.profiles.active=development' --no-daemon --stacktrace

sleep 10000