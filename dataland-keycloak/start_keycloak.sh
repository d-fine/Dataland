#!/bin/bash
set -eu

mode=$1
dataland_realm_folder=/opt/keycloak/datalandsecurity
script_dir="$(dirname "$0")"
cd $script_dir

if [[ "$mode" == initialize ]]; then
  echo "Initializing new keycloak realms"
  mkdir -p $dataland_realm_folder
  cp /keycloak_users/datalandsecurity-users-*.json $dataland_realm_folder || echo "No importable users exist"
  rm $(grep -E -l '"username" : "data_(reader|uploader|admin)"' "$dataland_realm_folder"/datalandsecurity-users-*.json) || echo "No technical users to be cleaned up"
  rm $(grep -E -l '"username" : "test_user.*@dataland.com"' "$dataland_realm_folder"/datalandsecurity-users-*.json) || echo "No test users to be cleaned up"
  cp /keycloak_realms/datalandsecurity-realm.json $dataland_realm_folder
  for variable in $(env | grep KEYCLOAK_ | cut -d'=' -f1); do
    # If one of the env variables containing "KEYCLOAK_" contains a % character the below sed statement will fail
    # and the script will terminate because % is the sed delimiter
    sed s%\$\{"$variable"\}%"${!variable}"%g -i $dataland_realm_folder/datalandsecurity-realm.json
  done
  ./kc.sh import --file /keycloak_realms/master-realm.json
  ./kc.sh import --dir $dataland_realm_folder
  ./kc.sh start
elif [[ "$mode" == export ]]; then
  echo "Exporting users"
  ./kc.sh export --dir /keycloak_users --users different_files --users-per-file 1 --realm datalandsecurity
else
  echo "Starting keycloak using: $*"
  ./kc.sh "$@"
fi
