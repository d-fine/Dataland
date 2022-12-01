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
container_name=$(sudo docker ps --format "{{.Names}}" | grep keycloak-initializer)
timeout 300 bash -c "while ! docker logs $container_name 2>/dev/null | grep -q \"$message\";
                     do
                       echo Startup of Keycloak incomplete. Waiting for it to finish.;
                       sleep 5;
                     done"

if ls "$keycloak_user_dir"/*-users-*.json &>/dev/null; then
  echo "Testing if the number of current users matches the number of exported users"
  current_users=$(sudo docker exec $container_name /opt/keycloak/bin/kcadm.sh get users -r datalandsecurity --server http://localhost:8080/keycloak --realm master --user $KEYCLOAK_ADMIN --password $KEYCLOAK_ADMIN_PASSWORD | grep -c '\"username\" :')
  all_users=$(sudo docker exec "$container_name" bash -c 'grep -l username /keycloak_users/datalandsecurity-users-*.json | wc -l')
  test_users=$(sudo docker exec "$container_name" bash -c 'grep -E -l \"test_user.*@dataland.com\" /keycloak_users/datalandsecurity-users-*.json | wc -l')
  expected_users=$((all_users-test_users))
  if [[ ! $expected_users -eq $current_users ]]; then
    echo "Found $current_users but $expected_users were expected."
    exit 1
  fi
  echo "Number of imported users match the exported users."
fi

echo "Shutting down all running containers."
sudo docker kill $(docker ps -q); docker system prune --force; docker info

echo "Successfully initialized new instance of Keycloak."