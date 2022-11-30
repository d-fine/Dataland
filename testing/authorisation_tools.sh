#!/bin/bash
# This script retrieves tokens from keycloak. Using the dev stack the keycloak_base_url would be https://local-dev.dataland.com/keycloak
set -euxo pipefail

function getJwt() {
  local user=$1
  local password=$2
  local base_url=$3
  local host=$4
  local keycloak_openid_token_endpoint
  keycloak_openid_token_endpoint="$base_url"/keycloak/realms/datalandsecurity/protocol/openid-connect/token
  local client_id="dataland-public"
  local get_user_token_response
  get_user_token_response=$(curl --request POST "${keycloak_openid_token_endpoint}" \
                                 --header 'Content-Type: application/x-www-form-urlencoded' \
                                 --data-urlencode "username=${user}" \
                                 --data-urlencode "password=${password}" \
                                 --data-urlencode 'grant_type=password' \
                                 --data-urlencode "client_id=${client_id}" \
                                 --header "Host: $host" \
                                 --insecure)

  local token_regex="access_token\":\"([a-zA-Z0-9._-]+)\""
  if [[ $get_user_token_response =~ $token_regex ]]; then
    echo "${BASH_REMATCH[1]}"
    return 0
  else
    echo "Unable to extract token. Response was:"
    echo "$get_user_token_response"
    return 1
  fi
}

getApiKeyWithToken() {
  local token=$1
  local base_url=$2
  local host=$3
  local get_api_key_base_url="$base_url"/api-keys/generateApiKey
  local get_api_key_response
  get_api_key_response=$(curl --location "${get_api_key_base_url}?daysValid=1" \
                                   --header "accept: application/json" \
                                   --header "authorization: Bearer $token" \
                                   --header "Host: $host" \
                                   --insecure)

  local base64characters="[A-Za-z0-9+/]"
  local token_regex="\"apiKey\": *\"($base64characters+_$base64characters+_[0-9]+)\","
  if [[ $get_api_key_response =~ $token_regex ]]; then
    echo "${BASH_REMATCH[1]}"
    return 0
  else
    echo "Unable to extract token. Response was:"
    echo "$get_api_key_response"
    return 1
  fi
}

getApiKeyWithUsernamePassword() {
    local user=$1
    local password=$2
    local base_url=$3
    local host=$4
    local jwt
    jwt=$(getJwt "$user" "$password" "$base_url" "$host")
    if [[ "$jwt" =~ "^Unable to extract token" ]]; then
      echo $jwt
      return 1
    fi
    getApiKeyWithToken "$jwt" "$base_url" "$host"
}
