#!/bin/bash
set -eux
source ./deployment/deployment_utils.sh

target_server_url=$1
location=$2

keycloak_volume_name=dataland_keycloak_data

script_dir="$(dirname "$0")"
echo "Copying the realm jsons to the server $target_server_url."
ssh ubuntu@"$target_server_url" "mkdir -p $location/dataland-keycloak/users"
scp -r "$script_dir"/../dataland-keycloak/realms ubuntu@"$target_server_url":"$location"/dataland-keycloak
scp "$script_dir"/../dataland-keycloak/Dockerfile ubuntu@"$target_server_url":$location/DockerfileKeycloak
scp "$script_dir"/../docker-compose.yml ubuntu@"$target_server_url":$location
scp -r "$script_dir"/../dataland-keycloak/dataland_theme/login/dist ubuntu@"$target_server_url":$location/dataland-keycloak/dataland_theme/login

volume_exists=$(search_volume "$target_server_url" "$location" "$keycloak_volume_name")
if [[ -n $volume_exists ]]; then
  ssh ubuntu@"$target_server_url" "cd $location && sudo docker-compose build keycloak-initializer"
  ssh ubuntu@"$target_server_url" "cd $location && sudo docker-compose run keycloak-initializer export"
  ssh ubuntu@"$target_server_url" "cd $location && sudo docker-compose down --remove-orphans"
fi

delete_docker_volume_if_existent "$target_server_url" "$location" "$keycloak_volume_name"

echo "Start Keycloak in initialization mode and wait for it to load the realm data."
ssh ubuntu@"$target_server_url" "cd $location;
                                 sudo docker-compose pull;
                                 sudo docker-compose build keycloak-initializer;
                                 export KEYCLOAK_FRONTEND_URL=\"$KEYCLOAK_FRONTEND_URL\";
                                 export KEYCLOAK_UPLOADER_VALUE=\"$KEYCLOAK_UPLOADER_VALUE\";
                                 export KEYCLOAK_UPLOADER_SALT=\"$KEYCLOAK_UPLOADER_SALT\";
                                 export KEYCLOAK_READER_VALUE=\"$KEYCLOAK_READER_VALUE\";
                                 export KEYCLOAK_READER_SALT=\"$KEYCLOAK_READER_SALT\";
                                 export KEYCLOAK_ADMIN=\"$KEYCLOAK_ADMIN\";
                                 export KEYCLOAK_ADMIN_PASSWORD=\"$KEYCLOAK_ADMIN_PASSWORD\";
                                 export KEYCLOAK_DB_PASSWORD=\"$KEYCLOAK_DB_PASSWORD\";
                                 export KEYCLOAK_GOOGLE_SECRET=\"$KEYCLOAK_GOOGLE_SECRET\";
                                 export KEYCLOAK_GOOGLE_ID=\"$KEYCLOAK_GOOGLE_ID\";
                                 export KEYCLOAK_LINKEDIN_ID=\"$KEYCLOAK_LINKEDIN_ID\";
                                 export KEYCLOAK_LINKEDIN_SECRET=\"$KEYCLOAK_LINKEDIN_SECRET\";
                                 export KEYCLOAK_DOCKERFILE=DockerfileKeycloak;
                                 sudo -E docker-compose run keycloak-initializer"

echo "Shutting down all running containers."
ssh ubuntu@"$target_server_url" 'sudo docker kill $(sudo docker ps -q); sudo docker system prune --force; sudo docker info'

echo "Successfully initialized new instance of Keycloak."