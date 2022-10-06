#!/bin/bash
set -eu

mode=$1
dataland_realm_folder=/keycloak_realms/datalandsecurity
script_dir="$(dirname "$0")"
cd $script_dir

if [[ "$mode" == initialize ]]; then
  echo "Initializing new keycloak realms"
  mkdir -p $dataland_realm_folder
  cp /keycloak_users/datalandsecurity-users-0.json $dataland_realm_folder || echo "No importable users exist"
  cp /keycloak_realms/datalandsecurity-realm.json $dataland_realm_folder
  for variable in $(env | grep KEYCLOAK_ | cut -d'=' -f1); do
    echo "Replacing variable $variable"
    sed s/\$\{"$variable"\}/"${!variable}"/g -i $dataland_realm_folder/datalandsecurity-realm.json
  done
  ./kc.sh import --file /keycloak_realms/master-realm.json
  ./kc.sh import --dir $dataland_realm_folder
  ./kc.sh start
elif [[ "$mode" == export ]]; then
  echo "Exporting users"
  ./kc.sh export --dir /keycloak_users --users same_file --realm datalandsecurity
else
  echo "Starting keycloak using: $*"
  ./kc.sh "$@"
fi
