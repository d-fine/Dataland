#!/bin/bash

export keycloak_openid_token_endpoint="http://localhost:8080/realms/datalandsecurity/protocol/openid-connect/token"
export user_name="some_user"
export user_password="test"
export client_id="public"


echo "Getting token from keycloak."
curl --location --request POST "${keycloak_openid_token_endpoint}" --header 'Content-Type: application/x-www-form-urlencoded' --data-urlencode "username=${user_name}" --data-urlencode "password=${user_password}" --data-urlencode 'grant_type=password' --data-urlencode "client_id=${client_id}"

#regex="\"([a-f0-9\-]+:[a-f0-9\-]+)\""
#if [[ $response =~ $regex ]]; then
 # dataId=${BASH_REMATCH[1]}
#else
 # echo "Unable to extract data ID from response: $response"
  #exit 1
#fi
#echo "Received response from post request with data ID: $dataId"

