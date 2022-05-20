#!/bin/bash
# This test does two things in sequence:
# First it gets a jwt token from the keycloak service, validates it and saves it.
# Then it uses this token to make a get-all-companies request against the backend. Since this endpoint needs
# an authorization, it only responds (with an empty list: []) if authorization was successful.


export keycloak_openid_token_endpoint="http://localhost:8080/realms/datalandsecurity/protocol/openid-connect/token"
export backend_url="http://localhost:8081/api"
export user_name="some_user"
export user_password="test"
export client_id="public"


echo "Getting token from keycloak."
response=$(curl --location --request POST "${keycloak_openid_token_endpoint}" --header 'Content-Type: application/x-www-form-urlencoded' --data-urlencode "username=${user_name}" --data-urlencode "password=${user_password}" --data-urlencode 'grant_type=password' --data-urlencode "client_id=${client_id}")
echo "Start matching"
regex="access_token\":\"([a-zA-Z0-9._-]+)\""
if [[ $response =~ $regex ]]; then
  jwt_token=${BASH_REMATCH[1]}
  echo "Matching successful"
else
  echo "Matching failed, jwt token could not be found."
  exit 1
fi

echo "Trying to get all companies with jwt token."
getallcompanies_response=$(curl --location --request GET "${backend_url}/companies" --header "Authorization: Bearer ${jwt_token}")
echo "Start matching getallcompanies_response with regex of expected value."
regex="\[\]"
if [[ $getallcompanies_response =~ $regex ]]; then
  echo "Matching successful, all companies list could be retrieved."
else
  echo "Matching failed."
  exit 1
fi


