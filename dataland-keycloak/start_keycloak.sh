#!/bin/bash
set -eu

mode=$1
script_dir="$(dirname "$0")"
cd $script_dir

if [[ "$mode" == initialize ]]; then
  echo "Initializing new keycloak realms"
  mkdir -p /opt/keycloak/data/import
  mkdir -p /keycloak_users/test
  cp /keycloak_realms/* /opt/keycloak/data/import
  cp /keycloak_users/datalandsecurity-users-0.json /opt/keycloak/data/import || echo "No importable users exist"
  cp /keycloak_users/datalandsecurity-users-0.json /keycloak_users/test
  cp /keycloak_realms/datalandsecurity-realm.json /keycloak_users/test
  for variable in $(env | grep KEYCLOAK_ | cut -d'=' -f1); do
    sed s/\$\{"$variable"\}/"${!variable}"/g -i /keycloak_users/test/datalandsecurity-realm.json
  done
  ./kc.sh import --file /opt/keycloak/data/import/master-realm.json
  ./kc.sh import --dir /keycloak_users/test
  ./kc.sh start --import-realm
 elif [[ "$mode" == export ]]; then
  echo "Exporting users"
  ./kc.sh export --dir /keycloak_users --users same_file --realm datalandsecurity
  #rm /keycloak_users/datalandsecurity-realm.json
else
  cp /keycloak_realms/* /opt/keycloak/data/import || true
  echo Starting keycloak using: "$@"
  ./kc.sh "$@"
fi
