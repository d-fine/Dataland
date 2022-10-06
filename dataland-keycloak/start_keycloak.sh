#!/bin/bash
set -eu

mode=$1
dataland_realm_folder=/keycloak_realms/datalandsecurity
script_dir="$(dirname "$0")"
cd $script_dir

if [[ "$mode" == initialize ]]; then
  mkdir -p $dataland_realm_folder
  echo "Initializing new keycloak realms" >> $dataland_realm_folder/inner.log
  cp /keycloak_users/datalandsecurity-users-0.json $dataland_realm_folder || echo "No importable users exist"
  cp /keycloak_realms/datalandsecurity-realm.json $dataland_realm_folder
  env > $dataland_realm_folder/environment.log
  for variable in $(env | grep KEYCLOAK_ | cut -d'=' -f1); do
    echo "Replacing variable $variable" >> $dataland_realm_folder/inner.log
    sed s%\$\{"$variable"\}%"${!variable}"%g -i $dataland_realm_folder/datalandsecurity-realm.json >> $dataland_realm_folder/inner.log 2>>$dataland_realm_folder/inner.log
  done
  ./kc.sh import --file /keycloak_realms/master-realm.json >> $dataland_realm_folder/inner.log
  ./kc.sh import --dir $dataland_realm_folder >> $dataland_realm_folder/inner.log
  ./kc.sh start
elif [[ "$mode" == export ]]; then
  echo "Exporting users"
  ./kc.sh export --dir /keycloak_users --users same_file --realm datalandsecurity
else
  echo "Starting keycloak using: $*"
  ./kc.sh "$@"
fi
