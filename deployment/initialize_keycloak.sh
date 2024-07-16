#!/usr/bin/env bash
set -eux
source "$(dirname "$0")"/deployment_utils.sh

function write_log_and_exit () {
  docker logs "$keycloak_initializer_container_name"
  exit 1
}

location=$1
keycloak_user_dir=$2

cd "$location"

echo "Start Keycloak in initialization mode and wait for it to load the realm data."
sudo docker compose pull;
sudo -E docker compose --profile init up -d --build

message="Profile prod activated."
keycloak_initializer_container_name=$(sudo docker ps --format "{{.Names}}" | grep keycloak-initializer)
keycloak_database_container_name=$(sudo docker ps --format "{{.Names}}" | grep keycloak-db)
timeout 300 bash -c "while ! docker logs $keycloak_initializer_container_name 2>/dev/null | grep -q \"$message\";
                     do
                       echo Startup of Keycloak incomplete. Waiting for it to finish.;
                       sleep 5;
                     done" || write_log_and_exit

docker logs "$keycloak_initializer_container_name"

if ls "$keycloak_user_dir"/*-users-*.json &>/dev/null; then
  echo "Testing if the number of current users matches the number of exported users"
  exported_users=$(sudo docker exec "$keycloak_initializer_container_name" bash -c 'grep -l username /keycloak_users/datalandsecurity-users-*.json | wc -l')
  exported_expected_technical_users=$(sudo docker exec --env USER_PATTERN='"username" : "(data_(reader|uploader|reviewer|premium_user|admin)|service-account-dataland-batch-manager|service-account-dataland-backend|service-account-dataland-community-manager|service-account-dataland-email-service|service-account-dataland-document-manager|service-account-dataland-qa-service|service-account-dataland-automated-qa)"' "$keycloak_initializer_container_name" bash -c 'grep -E -l "$USER_PATTERN" /keycloak_users/datalandsecurity-users-*.json | wc -l')
  exported_test_users=$(sudo docker exec "$keycloak_initializer_container_name" bash -c 'grep -E -l \"test_user.*@example.com\" /keycloak_users/datalandsecurity-users-*.json | wc -l')
  exported_actual_users=$((exported_users-exported_test_users-exported_expected_technical_users))

  imported_users=$(sudo docker exec $keycloak_database_container_name psql -U keycloak -d keycloak -t -c "select count(*) from user_entity where realm_id = 'datalandsecurity'")
  imported_expected_technical_users=$(sudo docker exec $keycloak_database_container_name psql -U keycloak -d keycloak -t -c "select count(*) from user_entity where realm_id = 'datalandsecurity' and username in ('data_reader','data_uploader','data_reviewer','data_premium_user','data_admin','service-account-dataland-batch-manager','service-account-dataland-community-manager','service-account-dataland-email-service','service-account-dataland-document-manager', 'service-account-dataland-qa-service','service-account-dataland-automated-qa','service-account-dataland-backend')")
  imported_actual_users=$((imported_users-imported_expected_technical_users))

  echo "The new instance contains a total of $imported_users users with $imported_expected_technical_users technical users (Actual users: $imported_actual_users)"
  echo "The old instance contained a total of $exported_users users with $exported_expected_technical_users technical users and $exported_test_users test users (Actual users: $exported_actual_users)"
  if [[ ! $exported_actual_users -eq imported_actual_users ]]; then
    echo "Found $imported_actual_users but $exported_actual_users were expected."
    exit 1
  fi
  echo "Number of imported users match the exported users."
fi

echo "Shutting down all running containers."
sudo docker kill $(docker ps -q); docker system prune --force; docker info

echo "Successfully initialized new instance of Keycloak."