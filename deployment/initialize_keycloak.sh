#!/bin/bash
set -eux
source ./deployment/deployment_utils.sh

target_server_url=$1
location=$2

keycloak_volume_name=dataland_keycloak_data

script_dir="$(dirname "$0")"
echo "Copying the realm jsons to the server $target_server_url."
ssh ubuntu@"$target_server_url" "mkdir -p $location/dataland-keycloak"
scp -r "$script_dir"/../dataland-keycloak/realms ubuntu@"$target_server_url":"$location"/dataland-keycloak
scp "$script_dir"/../dataland-keycloak/Dockerfile ubuntu@"$target_server_url":$location/DockerfileKeycloak
scp "$script_dir"/../docker-compose.yml ubuntu@"$target_server_url":$location
scp -r "$script_dir"/../dataland-keycloak/dataland_theme/login/dist ubuntu@"$target_server_url":$location/dataland-keycloak/dataland_theme/login

ssh ubuntu@"$target_server_url" "cd $location && sudo docker-compose build keycloak-initializer" || exit 1
ssh ubuntu@"$target_server_url" "cd $location && sudo docker-compose run keycloak-initializer export" || exit 1

delete_docker_volume_if_existent "$target_server_url" "$location" "$keycloak_volume_name"

echo "Start Keycloak in initialization mode and wait for it to load the realm data."
ssh ubuntu@"$target_server_url" "cd $location; sudo docker-compose pull;
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
                                 sudo -E docker-compose --profile init up -d --build"
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