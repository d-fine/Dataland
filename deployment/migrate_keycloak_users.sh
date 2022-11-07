#!/bin/bash
set -euxo pipefail
source "$(dirname "$0")"/deployment_utils.sh

location=$1
keycloak_user_dir=$2
keycloak_backup_dir=$3
persistent_keycloak_backup_dir=$4

keycloak_volume_name=dataland_keycloak_data

cd "$location"

persistent_backup="$persistent_keycloak_backup_dir"/"$(date '+%Y%m%d_%H%M')"
if ls "$keycloak_user_dir"/*-users-*.json &>/dev/null; then
  echo "Found users from previous export. Moving to backup location: $persistent_backup"
  mkdir -p "$persistent_backup"
  cp "$keycloak_user_dir"/*-users-*.json "$persistent_backup"
fi

mkdir -p "$keycloak_backup_dir"
volume=$(search_volume "$keycloak_volume_name")
if [[ -n $volume ]]; then
  echo "Found existing Keycloak volume. Exporting Users."
  sudo docker compose run keycloak-initializer export
  sudo docker compose down --remove-orphans
  delete_docker_volume "$volume"
  cp "$keycloak_user_dir"/*-users-*.json "$keycloak_backup_dir"
elif ls "$persistent_backup"/*-users-*.json &>/dev/null; then
  echo "No Keycloak volume found. Loading uses from previous backup."
  cp "$persistent_backup"/*-users-*.json "$keycloak_backup_dir"
else
  echo "No Keycloak volume or current user backup found. Resulting Keycloak instance will have no users."
fi