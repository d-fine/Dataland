#!/bin/bash

set -euox pipefail

if curl -L https://local-dev.dataland.com/api/actuator/health/ping 2>/dev/null | grep -q UP; then
  echo "ERROR: The backend is currently running. This will prevent the new backend from starting."
  echo "Shut down the running process and restart the script."
  exit 1
fi

echo "Clearing Docker..."
docker compose down --remove-orphans
docker volume prune --force
docker image prune -a -f

echo "Clearing frontend clients..."
./gradlew clean
./gradlew assemble

rm ./*github_env.log || true
./build-utils/base_rebuild_gradle_dockerfile.sh
set -o allexport
source ./*github_env.log
set +o allexport

find ./build-utils/ -name "rebuild*.sh" ! -name "*prod*" ! -name "*test*" -exec bash -c 'eval "$1" && echo "SUCCESS - execution of $1 was successful" || echo "ERROR - could not execute $1"' shell {} \;

set -o allexport
source ./*github_env.log
set +o allexport

echo "Initializing Keycloak..."
docker compose --profile init up --build &

keycloak_initializer_container=dataland-keycloak-initializer-1
while ! docker logs $keycloak_initializer_container 2>/dev/null | grep -q "Added user 'admin' to realm 'master'"; do
 echo "Waiting for Keycloak to finish initializing..."
 sleep 5
done

docker compose --profile init down

echo "Starting development stack..."
./startDevelopmentStack.sh