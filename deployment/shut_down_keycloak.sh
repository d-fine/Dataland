#!/bin/bash
set -eux
source "$(dirname "$0")"/deployment_utils.sh

location=$1

keycloak_volume_name=dataland_keycloak_data

cd "$location"

volume=$(search_volume "$keycloak_volume_name")
if [[ -n $volume ]]; then
  sudo docker-compose run keycloak-initializer export
  sudo docker-compose down --remove-orphans
  delete_docker_volume $volume
fi