#!/bin/bash
set -eu

mode=$1
datalandrealm_folder=/keycloak_realms/datalandsecurity
script_dir="$(dirname "$0")"
cd $script_dir

if [[ "$mode" == initialize ]]; then
  echo "Initializing new keycloak realms"
  mkdir -p $datalandrealm_folder
  cp /keycloak_users/datalandsecurity-users-0.json $datalandrealm_folder || echo "No importable users exist"
  cp /keycloak_realms/datalandsecurity-realm.json $datalandrealm_folder
  for variable in $(env | grep KEYCLOAK_ | cut -d'=' -f1); do
    sed s/\$\{"$variable"\}/"${!variable}"/g -i $datalandrealm_folder/datalandsecurity-realm.json
  done
  ./kc.sh import --file /keycloak_realms/master-realm.json
  ./kc.sh import --dir $datalandrealm_folder
  ./kc.sh start
elif [[ "$mode" == export ]]; then
  echo "Exporting users"
  ./kc.sh export --dir /keycloak_users --users same_file --realm datalandsecurity
else
  echo "Starting keycloak using: $*"
  ./kc.sh "$@"
fi
