#!/bin/bash
set -ux

source ./deployment/deployment_utils.sh

environment=$1
source ./deployment/deployment_utils.sh

if [[ $IN_MEMORY == true ]]; then
  profile=productionInMemory
else
  profile=production
  echo "Checking if EuroDaT is available before deploying to target server."
  if ! curl -f -X 'GET' "${TRUSTEE_BASE_URL}/${TRUSTEE_ENVIRONMENT_NAME}/api/ids/description" -H 'accept: application/json' >/dev/null 2>&1; then
    echo "EuroDaT is not available."
    exit 1
  fi
  echo "EuroDat is available."
fi

echo "Starting $environment server"
curl "$TARGETSERVER_STARTUP_URL" > /dev/null
echo "Setting $environment server as deployment target"
target_server_url="$TARGETSERVER_URL"

setup_ssh

timeout 300 bash -c "while ! ssh -o ConnectTimeout=3 ubuntu@$target_server_url exit; do echo '$environment server not yet there - retrying in 1s'; sleep 1; done" || exit

location=/home/ubuntu/dataland
# shut down currently running dataland application and purge files on server
ssh ubuntu@"$target_server_url" "cd $location && sudo docker-compose down"
# make sure no remnants remain when docker-compose file changes
ssh ubuntu@"$target_server_url" 'sudo docker kill $(sudo docker ps -q); sudo docker system prune --force; sudo docker info'
ssh ubuntu@"$target_server_url" "sudo rm -rf $location; mkdir -p $location/jar; mkdir -p $location/dataland-keycloak/dataland_theme/login"

envsubst < environments/.env.template > .env

scp ./.env ubuntu@"$target_server_url":$location
scp -r ./dataland-frontend/dist ./docker-compose.yml ./dataland-inbound-proxy/ ./dataland-inbound-admin-proxy/ ./dataland-frontend/default.conf ubuntu@$target_server_url:$location
scp -r ./dataland-keycloak/dataland_theme/login/dist ubuntu@$target_server_url:$location/dataland-keycloak/dataland_theme/login
scp -r ./dataland-pgadmin ubuntu@$target_server_url:$location
scp ./dataland-keycloak/start_keycloak.sh ubuntu@"$target_server_url":$location/dataland-keycloak/start_keycloak.sh
scp ./dataland-frontend/Dockerfile ubuntu@"$target_server_url":$location/DockerfileFrontend
scp ./dataland-backend/Dockerfile ubuntu@"$target_server_url":$location/DockerfileBackend
scp ./dataland-keycloak/Dockerfile ubuntu@"$target_server_url":$location/DockerfileKeycloak
scp ./dataland-backend/build/libs/dataland-backend*.jar ubuntu@"$target_server_url":$location/jar/dataland-backend.jar

if [[ $INITIALIZE_KEYCLOAK == true ]]; then
  echo "Deployment configuration requires Keycloak to be set up from scratch."
  "$(dirname "$0")"/initialize_keycloak.sh "$target_server_url" "$location" || exit 1
fi

if [[ $RESET_BACKEND_DATABASE_AND_REPOPULATE == true ]]; then
  echo "Resetting backend database"
  delete_docker_volume_if_existent "$target_server_url" "$location" "backend_data"
fi

echo "Starting docker compose stack."
ssh ubuntu@"$target_server_url" "cd $location; sudo docker-compose pull; sudo docker-compose --profile $profile up -d --build"

# Wait for backend to finish boot process
wait_for_health "https://$target_server_url/api/actuator/health/ping" "backend"