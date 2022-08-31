#!/bin/bash
set -eu

arguments="$1"

script_dir="$(dirname "$0")"
cd $script_dir

if [[ "$arguments" == initialize ]]; then
  echo "Initializing new keycloak realms"
 ./kc.sh import --file /opt/keycloak/data/import/master-realm.json
 ./kc.sh start --import-realm
else
  echo "Starting keycloak using: $arguments"
  ./kc.sh "$arguments"
fi