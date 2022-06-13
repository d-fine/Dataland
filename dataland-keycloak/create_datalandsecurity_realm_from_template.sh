#!/bin/bash

if [[ $KEYCLOAK_FRONTEND_URL == "" ]]; then
  echo "Error: Env KEYCLOAK_FRONTEND_URL is missing! The built realm would be broken."
  echo "Exit script."
  exit 1
fi

# Since the keycloak realm file already contains bash-like variable entries, all variables to be substituted have to be
# listed explicitly below.
envsubst '${KEYCLOAK_FRONTEND_URL}' < ./datalandsecurity-realm-template.json > ./realms/datalandsecurity-realm.json
