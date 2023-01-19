#!/bin/bash
set -eux
source "$(dirname "$0")"/deployment_utils.sh

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
                     done"

docker logs $keycloak_initializer_container_name

if ls "$keycloak_user_dir"/*-users-*.json &>/dev/null; then
  echo "Testing if the number of current users matches the number of exported users"
  current_users=$(sudo docker exec $keycloak_database_container_name psql -U keycloak -d keycloak -t -c "select count(*) from user_entity where realm_id = 'datalandsecurity'")
  current_technical_users=$(sudo docker exec $keycloak_database_container_name psql -U keycloak -d keycloak -t -c "select count(*) from user_entity where realm_id = 'datalandsecurity' and username in ('data_reader','data_uploader','data_admin')")
  all_users=$(sudo docker exec "$keycloak_initializer_container_name" bash -c 'grep -l username /keycloak_users/datalandsecurity-users-*.json | wc -l')
  technical_users=$(sudo docker exec --env USER_PATTERN='"username" : "data_(reader|uploader|admin)"' "$keycloak_initializer_container_name" bash -c 'grep -E -l "$USER_PATTERN" /keycloak_users/datalandsecurity-users-*.json | wc -l')
  test_users=$(sudo docker exec "$keycloak_initializer_container_name" bash -c 'grep -E -l \"test_user.*@dataland.com\" /keycloak_users/datalandsecurity-users-*.json | wc -l')
  actual_users=$((current_users-current_technical_users))
  expected_users=$((all_users-test_users-technical_users))
  echo "The new instance contains a total of $current_users users with $current_technical_users technical users (Actual users: $actual_users)"
  echo "The old instance contained a total of $all_users users with $technical_users technical users and $test_users test users (Actual users: $expected_users)"
  if [[ ! $expected_users -eq actual_users ]]; then
    echo "Found $actual_users but $expected_users were expected."
    exit 1
  fi
  echo "Number of imported users match the exported users."
fi

echo "Shutting down all running containers."
sudo docker kill $(docker ps -q); docker system prune --force; docker info

echo "Successfully initialized new instance of Keycloak."