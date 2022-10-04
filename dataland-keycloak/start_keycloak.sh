#!/bin/bash
set -eu

mode=$1

script_dir="$(dirname "$0")"
cd $script_dir

if [[ "$mode" == initialize ]]; then
  echo "Initializing new keycloak realms"
  mkdir -p /opt/keycloak/data/import
  cp /keycloak_realms/* /opt/keycloak/data/import
  cp /keycloak_users/* /opt/keycloak/data/import || echo "No importable users exist"
  ./kc.sh import --file /opt/keycloak/data/import/master-realm.json
  ./kc.sh start --import-realm
elif [[ "$mode" == export ]]; then
  echo "Exporting users"
  rm /keycloak_users/datalandsecurity-realm.json
  ./kc.sh export --dir /keycloak_users --users same_file --realm datalandsecurity
  rm /keycloak_users/datalandsecurity-realm.json
else
  cp -r /keycloak_realms/ /opt/keycloak/data/import/
  echo "Starting keycloak using: $@"
  ./kc.sh "$@"
fi
