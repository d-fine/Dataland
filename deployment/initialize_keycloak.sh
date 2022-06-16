#!/bin/bash
set -eux

target_server_url=$1
location=$2

script_dir="$(dirname "$0")"
echo "Copying the realm jsons to the server $target_server_url."
ssh ubuntu@"$target_server_url" "mkdir -p $location/realms"
scp -r "$script_dir"/../dataland-keycloak/realms ubuntu@"$target_server_url":"$location"/realms
scp "$script_dir"/../dataland-keycloak/Dockerfile ubuntu@"$target_server_url":$location/DockerfileKeycloak
scp "$script_dir"/../docker-compose.yml ubuntu@"$target_server_url":$location

old_volume=$(ssh ubuntu@"$target_server_url" "cd $location && sudo docker volume ls -q | grep keycloak_data") || true
if [[ -n $old_volume ]]; then
  echo "Removing old keycloak volume with name $old_volume."
  ssh ubuntu@"$target_server_url" "cd $location && sudo docker volume rm $old_volume"
fi

echo "Start Keycloak in initialization mode and wait for it to load the realm data."
ssh ubuntu@"$target_server_url" "cd $location; sudo docker-compose pull;
                                 export KEYCLOAK_FRONTEND_URL=$KEYCLOAK_FRONTEND_URL;
                                 export KEYCLOAK_ADMIN=$KEYCLOAK_ADMIN;
                                 export KEYCLOAK_ADMIN_PASSWORD=$KEYCLOAK_ADMIN_PASSWORD;
                                 sudo -E docker-compose --profile init up -d --build"
message="Profile prod activated."
timeout 300 bash -c "while ! docker logs dataland_keycloak_1 | grep -q \"$message\"; do echo Startup of Keycloak incomplete. Waiting for it to finish.; sleep 1; done" || exit 1

echo "Shutting down all running containers."
ssh ubuntu@"$target_server_url" 'sudo docker kill $(sudo docker ps -q); sudo docker system prune --force; sudo docker info'

echo "Successfully initialized new instance of Keycloak."