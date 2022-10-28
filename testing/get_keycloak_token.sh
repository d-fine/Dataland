#!/bin/bash
# This script retrieves tokens from keycloak. Using the dev stack the keycloak_base_url would be https://local-dev.dataland.com/keycloak
set -euxo pipefail

function getToken() {
  local user=$1
  local password=$2
  echo "Getting token for user $user from keycloak."
  local get_user_token_response
  get_user_token_response=$(curl --location --request POST "${keycloak_openid_token_endpoint}" \
                                 --header 'Content-Type: application/x-www-form-urlencoded' \
                                 --data-urlencode "username=${user}" \
                                 --data-urlencode "password=${password}" \
                                 --data-urlencode 'grant_type=password' \
                                 --data-urlencode "client_id=${client_id}")

  if [[ $get_user_token_response =~ $regex ]]; then
    echo "JWT Token for $user is:"
    echo "${BASH_REMATCH[1]}"
  else
    echo "Unable to extract token. Response was:"
    echo "$get_user_token_response"
  fi
}

reader_password="$1"
writer_password="$2"
keycloak_base_url="$3"

keycloak_openid_token_endpoint="$keycloak_base_url"/realms/datalandsecurity/protocol/openid-connect/token
user_reader="data_reader"
user_writer="data_uploader"
client_id="dataland-public"
regex="access_token\":\"([a-zA-Z0-9._-]+)\""

getToken "$user_reader" "$reader_password"
getToken "$user_writer" "$writer_password"