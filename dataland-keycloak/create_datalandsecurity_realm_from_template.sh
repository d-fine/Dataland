#!/bin/bash

if [[ $KEYCLOAK_FRONTEND_URL == "" ]]; then
  echo "Error: Env KEYCLOAK_FRONTEND_URL is missing! The built realm would be broken."
  echo "Exit script."
  exit 1
fi

envsubst < ./datalandsecurity-realm-template.json > ./realms/datalandsecurity-realm.json
