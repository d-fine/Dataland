#!/bin/bash
set -euxo pipefail

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

timeout 300 bash -c "while ! ssh -o ConnectTimeout=5 ubuntu@$target_server_url exit; do echo '$environment server not yet there - retrying in 5s'; sleep 5; done" || exit

location=/home/ubuntu/dataland
keycloak_backup_dir=/home/ubuntu/keycloak_backup
persistent_keycloak_backup_dir=/home/ubuntu/persistent_keycloak_backup
keycloak_user_dir=$location/dataland-keycloak/users

# shut down currently running dataland application and purge files on server
ssh ubuntu@"$target_server_url" "cd \"$location\" && sudo docker compose down" || true
# make sure no remnants remain when docker-compose file changes
ssh ubuntu@"$target_server_url" "docker kill $(docker ps -q); docker system prune --force; docker info"

echo "Exporting users and shutting down keycloak."
ssh ubuntu@"$target_server_url" "mkdir -p ./dataland/dataland-keycloak"
scp ./deployment/migrate_keycloak_users.sh ubuntu@"$target_server_url":"$location"/dataland-keycloak
ssh ubuntu@"$target_server_url" "chmod +x \"$location/dataland-keycloak/migrate_keycloak_users.sh\""
ssh ubuntu@"$target_server_url" "\"$location/dataland-keycloak/migrate_keycloak_users.sh\" \"$location\" \"$keycloak_user_dir\" \"$keycloak_backup_dir\" \"$persistent_keycloak_backup_dir\""

ssh ubuntu@"$target_server_url" "sudo rm -rf \"$location\""

construction_dir=./dataland
build_directories "$construction_dir"
scp -r "$construction_dir" ubuntu@"$target_server_url":"$location"

ssh ubuntu@"$target_server_url" "mv \"$keycloak_backup_dir\"/*-users-*.json \"$keycloak_user_dir\" || true"

echo "Set up Keycloak from scratch."
ssh ubuntu@"$target_server_url" "export KEYCLOAK_UPLOADER_VALUE=\"$KEYCLOAK_UPLOADER_VALUE\";
                                 export KEYCLOAK_UPLOADER_SALT=\"$KEYCLOAK_UPLOADER_SALT\";
                                 export KEYCLOAK_READER_VALUE=\"$KEYCLOAK_READER_VALUE\";
                                 export KEYCLOAK_READER_SALT=\"$KEYCLOAK_READER_SALT\";
                                 export KEYCLOAK_ADMIN=\"$KEYCLOAK_ADMIN\";
                                 export KEYCLOAK_ADMIN_PASSWORD=\"$KEYCLOAK_ADMIN_PASSWORD\";
                                 export KEYCLOAK_GOOGLE_SECRET=\"$KEYCLOAK_GOOGLE_SECRET\";
                                 export KEYCLOAK_GOOGLE_ID=\"$KEYCLOAK_GOOGLE_ID\";
                                 export KEYCLOAK_LINKEDIN_ID=\"$KEYCLOAK_LINKEDIN_ID\";
                                 export KEYCLOAK_LINKEDIN_SECRET=\"$KEYCLOAK_LINKEDIN_SECRET\";
                                 export KEYCLOAK_MAILJET_API_SECRET=\"$KEYCLOAK_MAILJET_API_SECRET\";
                                 export KEYCLOAK_MAILJET_API_ID=\"$KEYCLOAK_MAILJET_API_ID\";
                                 \"$location\"/dataland-keycloak/initialize_keycloak.sh $location $keycloak_user_dir" || exit 1

echo "Cleaning up exported user files."
ssh ubuntu@"$target_server_url" "cp $keycloak_user_dir/*-users-*.json $persistent_keycloak_backup_dir; rm $keycloak_user_dir/*.json" || true

if [[ $RESET_BACKEND_DATABASE_AND_REPOPULATE == true ]]; then
  echo "Resetting backend database"
  ssh ubuntu@"$target_server_url" "source $location/dataland-keycloak/deployment_utils.sh; delete_docker_volume_if_existent \"backend_data\""
fi

echo "Starting docker compose stack."
ssh ubuntu@"$target_server_url" "cd $location; sudo docker compose pull; sudo docker compose --profile $profile up -d --build"

# Wait for backend to finish boot process
wait_for_health "https://$target_server_url/api/actuator/health/ping" "backend"
