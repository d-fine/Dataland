#!/bin/bash
set -eux

target_server_url=$1
location=$2
keycloak_user_dir=$3

script_dir="$(dirname "$0")"
echo "Copying the realm jsons to the server $target_server_url."
scp -r "$script_dir"/../dataland-keycloak/realms ubuntu@"$target_server_url":"$location"/dataland-keycloak
scp "$script_dir"/../dataland-keycloak/Dockerfile ubuntu@"$target_server_url":"$location"/DockerfileKeycloak
scp "$script_dir"/../docker-compose.yml ubuntu@"$target_server_url":"$location"
scp -r "$script_dir"/../dataland-keycloak/dataland_theme/login/dist ubuntu@"$target_server_url":"$location"/dataland-keycloak/dataland_theme/login

scp "$script_dir"/initialize_keycloak_server.sh ubuntu@"$target_server_url":"$location"/dataland-keycloak
scp .deployment/deployment_utils.sh ubuntu@"$target_server_url":"$location"/dataland-keycloak
ssh ubuntu@"$target_server_url" "export KEYCLOAK_FRONTEND_URL=\"$KEYCLOAK_FRONTEND_URL\";
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
                                 \"$location\"/dataland-keycloak/initialize_keycloak_server.sh $location $keycloak_user_dir"