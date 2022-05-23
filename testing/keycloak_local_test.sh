#!/bin/bash
# This test does two things in sequence:
# First it gets a jwt token from the keycloak service, validates it and saves it.
# Then it uses this token to make a get-all-companies request against the backend. Since this endpoint needs
# an authorization, it only responds (with an empty list: []) if authorization was successful.


export keycloak_openid_token_endpoint="http://localhost:8080/realms/datalandsecurity/protocol/openid-connect/token"
export backend_url="http://localhost:8081/api"
export user_name="some_user"
export user_password="test"
export admin_name="admin_user"
export admin_password="test"
export client_id="public"


echo "Getting token for user with role USER from keycloak."
response=$(curl --location --request POST "${keycloak_openid_token_endpoint}" --header 'Content-Type: application/x-www-form-urlencoded' --data-urlencode "username=${user_name}" --data-urlencode "password=${user_password}" --data-urlencode 'grant_type=password' --data-urlencode "client_id=${client_id}")
echo "Start extracting jwt token from response."
regex="access_token\":\"([a-zA-Z0-9._-]+)\""
if [[ $response =~ $regex ]]; then
  jwt_token_user=${BASH_REMATCH[1]}
  echo "Extracting successful. JWT Token for user is ${jwt_token_user}."
else
  echo "Extracting failed, jwt token could not be found."
  exit 1
fi

echo "Trying to get all companies with user jwt token."
getallcompanies_response=$(curl --location --request GET "${backend_url}/companies" --header "Authorization: Bearer ${jwt_token_user}")
echo "Start matching getallcompanies_response with regex of expected value."
regex="\[\]"
if [[ $getallcompanies_response =~ $regex ]]; then
  echo "Matching successful, all companies list could be retrieved."
else
  echo "Matching failed."
  exit 1
fi

echo "Getting token for admin user with role ADMIN from keycloak."
response=$(curl --location --request POST "${keycloak_openid_token_endpoint}" --header 'Content-Type: application/x-www-form-urlencoded' --data-urlencode "username=${admin_name}" --data-urlencode "password=${admin_password}" --data-urlencode 'grant_type=password' --data-urlencode "client_id=${client_id}")
echo "Start extracting jwt token from response."
regex="access_token\":\"([a-zA-Z0-9._-]+)\""
if [[ $response =~ $regex ]]; then
  jwt_token_admin=${BASH_REMATCH[1]}
  echo "Extracting successful. JWT Token for admin user is ${jwt_token_admin}."
else
  echo "Matching failed, jwt token could not be found."
  exit 1
fi

echo "Trying to post a company with admin jwt token."
postcompany_response=$(curl -X 'POST'   "${backend_url}/companies"   -H 'accept: application/json'   -H 'Content-Type: application/json'   -d '{
"companyName": "TestCompanyA",
"headquarters": "string",
"sector": "string",
"marketCap": 0,
"reportingDateOfMarketCap": "2022-05-23",
"indices": [
  "Cdax"
],
"identifiers": [
  {
    "identifierType": "Lei",
    "identifierValue": "string"
  }
]
}' --header "Authorization: bearer ${jwt_token_admin}"
)
echo "Start matching postcompany_response with regex of expected value."
regex="\"companyId\":\"([a-f0-9\-]+)\""
if [[ $postcompany_response =~ $regex ]]; then
  companyId=${BASH_REMATCH[1]}
  echo "Matching successful, company could be posted. Company Id is ${companyId}."
else
  echo "Matching failed."
  exit 1
fi

