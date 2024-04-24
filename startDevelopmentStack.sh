#!/usr/bin/env bash
# Login to the docker repository
set -euxo pipefail
./verifyEnvironmentVariables.sh

docker login ghcr.io -u $GITHUB_USER -p $GITHUB_TOKEN

# Retrieve the SSL-Certificates for local-dev.dataland.com
mkdir -p ./local/certs
scp ubuntu@letsencrypt.dataland.com:/etc/letsencrypt/live/local-dev.dataland.com/* ./local/certs

rm ./*github_env.log || true
./build-utils/base_rebuild_gradle_dockerfile.sh
set -o allexport
source ./*github_env.log
source ./environments/.env.uncritical
set +o allexport

if [ $# -ge 1 ] && [ "$1" == "parallel" ]; then
  echo "Building docker images in parallel for unrivaled speed! (Does not work in Windows Git Bash)"
  find ./build-utils/ -name "rebuild*.sh" ! -name "*prod*" ! -name "*test*" ! -name "*backend*" -print0 |
    parallel -0 --tmux 'eval "{}" && echo "SUCCESS - execution of {} was successful" || echo "ERROR - could not execute {}"'
else
  echo "Building docker images sequentially!"
  find ./build-utils/ -name "rebuild*.sh" ! -name "*prod*" ! -name "*test*" ! -name "*backend*" -exec \
    bash -c 'eval "$1" && echo "SUCCESS - execution of $1 was successful" || echo "ERROR - could not execute $1"' shell {} \;
fi

set -o allexport
source ./*github_env.log
set +o allexport

# start containers with the stack except frontend and backend
docker compose --profile development down
docker volume rm $(docker volume ls -q | grep _pgadmin_config) || true
docker volume rm $(docker volume ls -q | grep _qa_service_data) || true
docker volume rm $(docker volume ls -q | grep _community_manager_data) || true
docker compose --profile development pull --ignore-pull-failures --include-deps
docker compose --profile development up -d --build

#start the backend
./gradlew dataland-backend:bootRun --args='--spring.profiles.active=development' --no-daemon --stacktrace
