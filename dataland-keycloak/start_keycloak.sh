#!/bin/bash
set -eu

script_dir="$(dirname "$0")"
cd $script_dir

if [[ "$1" == initialize ]]; then
  echo "Initializing new keycloak realms"
 ./kc.sh import --file /opt/keycloak/data/import/master-realm.json
 ./kc.sh start --import-realm
else
  echo "Starting keycloak using: $@"
  ./kc.sh "$@"
fi