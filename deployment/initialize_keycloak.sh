#!/bin/bash
set -eux

target_server_url=$1
location=$2

script_dir="$(dirname "$0")"
echo "Copying the realm jsons to the server $target_server_url."
ssh ubuntu@"$target_server_url" "mkdir -p $location/dataland-keycloak"
scp -r "$script_dir"/../dataland-keycloak/realms ubuntu@"$target_server_url":"$location"/dataland-keycloak
scp "$script_dir"/../dataland-keycloak/Dockerfile ubuntu@"$target_server_url":$location/DockerfileKeycloak
scp "$script_dir"/../docker-compose.yml ubuntu@"$target_server_url":$location
scp -r ./dataland-keycloak/dataland_theme ubuntu@$target_server_url:$location/dataland-keycloak

old_volume=$(ssh ubuntu@"$target_server_url" "cd $location && sudo docker volume ls -q | grep keycloak_data") || true
if [[ -n $old_volume ]]; then
  echo "Removing old keycloak volume with name $old_volume."
  ssh ubuntu@"$target_server_url" "cd $location && sudo docker volume rm $old_volume"
fi

echo "Start Keycloak in initialization mode and wait for it to load the realm data."
ssh ubuntu@"$target_server_url" "cd $location; sudo docker-compose pull; sudo docker-compose --profile init up -d --build"
message="Profile prod activated."
container_name=$(ssh ubuntu@"$target_server_url" "cd $location && sudo docker ps --format \"{{.Names}}\" | grep keycloak-initializer")
timeout 300 bash -c "while ! ssh ubuntu@\"$target_server_url\" \"cd $location && sudo docker logs $container_name | grep -q \\\"$message\\\"\";
                     do
                       echo Startup of Keycloak incomplete. Waiting for it to finish.;
                       sleep 5;
                     done"

echo "Shutting down all running containers."
ssh ubuntu@"$target_server_url" 'sudo docker kill $(sudo docker ps -q); sudo docker system prune --force; sudo docker info'

echo "Successfully initialized new instance of Keycloak."