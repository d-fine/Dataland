#!/usr/bin/env bash
set -euxo pipefail

./verifyEnvironmentVariables.sh
if curl -L https://local-dev.dataland.com/api/actuator/health/ping 2>/dev/null | grep -q UP; then
  echo "ERROR: The backend is currently running. This will prevent the new backend from starting."
  echo "Shut down the running process and restart the script."
  exit 1
fi

# Write files necessary for the EuroDaT-client to work
./dataland-eurodat-client/write_secret_files.sh

echo "Clearing Docker..."
docker compose --profile development --profile developmentContainerFrontend down
docker compose --profile init down
docker compose down --remove-orphans
docker volume prune --force --all

echo "Rebuilding clients..."
./gradlew clean
./gradlew assemble --rerun-tasks

rm ./*github_env.log || true
./build-utils/base_rebuild_gradle_dockerfile.sh
set -o allexport
source ./*github_env.log
source ./environments/.env.uncritical
set +o allexport

./build-utils/rebuild_keycloak_image.sh
set -o allexport
source ./*github_env.log
set +o allexport

echo "Initializing Keycloak..."
docker compose --profile init up --build &

project_name=$(basename "$(pwd)")
lowercase_project_name=$(echo "$project_name" | tr '[:upper:]' '[:lower:]')
keycloak_initializer_container="$lowercase_project_name"-keycloak-initializer-1
while ! docker logs $keycloak_initializer_container 2>/dev/null | grep -q "Added user 'admin' to realm 'master'"; do
 echo "Waiting for Keycloak to finish initializing..."
 sleep 5
done

docker compose --profile init down

echo "Starting development stack..."
./startDevelopmentStack.sh