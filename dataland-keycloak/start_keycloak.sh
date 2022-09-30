#!/bin/bash
set -eu

script_dir="$(dirname "$0")"
cd $script_dir

if [[ "$1" == initialize ]]; then
  echo "Initializing new keycloak realms"
  cp -r /keycloak_users/ /opt/keycloak/data/import/
 ./kc.sh import --file /opt/keycloak/data/import/master-realm.json
 ./kc.sh start --import-realm
elif [[ "$1" == export ]]; then
  echo "Exporting users"
  ./kc.sh export --dir /keycloak_users --users same_file --realm datalandsecurity || exit 1
  rm /keycloak_users/datalandsecurity-realm.json
else
  echo "Starting keycloak using: $@"
  ./kc.sh "$@"
fi
