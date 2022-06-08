#!/bin/bash
# This test can/should be executed locally after starting the backend.
# It generates different jwt tokens and tests if it can reach secured endpoints by using them (or by not using them
# in case of teaser data).


export keycloak_openid_token_endpoint="http://keycloak:8080/keycloak/realms/datalandsecurity/protocol/openid-connect/token"
export backend_url="http://backend:8080/api"

export user_name="some_user"
export user_password="test"

export admin_name="admin_user"
export admin_password="test"

export client_id="dataland-public"

export test_company_name=$1
export test_company_data_marker=1234565432101

export teaser_company_name="Balnuweit, Günther and Benninger"
export teaser_company_data_marker=98765456789101


echo "=>Check if test company name was passed as argument for this test script."
if [[ ${test_company_name} == "" ]]; then
  echo "Error: No name was passed for test company."
  exit 1
fi

echo "=>Getting token for user with role USER from keycloak."
get_user_token_response=$(curl --location --request POST "${keycloak_openid_token_endpoint}" --header 'Content-Type: application/x-www-form-urlencoded' --data-urlencode "username=${user_name}" --data-urlencode "password=${user_password}" --data-urlencode 'grant_type=password' --data-urlencode "client_id=${client_id}")
echo "=>Start extracting jwt token from get_user_token_response."
regex="access_token\":\"([a-zA-Z0-9._-]+)\""
if [[ $get_user_token_response =~ $regex ]]; then
  jwt_token_user=${BASH_REMATCH[1]}
  echo "=>Extracting successful. JWT Token for user is ${jwt_token_user}."
else
  echo "Extracting failed, jwt token could not be found."
  exit 1
fi
echo "=>Trying to get all companies with user jwt token."
getallcompanies_response=$(curl --location --request GET "${backend_url}/companies" --header "Authorization: Bearer ${jwt_token_user}")
echo "=>Start matching getallcompanies_response with regex of expected value."
regex="\[\]"
if [[ $getallcompanies_response =~ $regex ]]; then
  echo "=>Matching successful, all companies list could be retrieved."
else
  echo "Matching failed."
  exit 1
fi



echo "=>Getting token for admin user with role ADMIN from keycloak."
get_admin_token_response=$(curl --location --request POST "${keycloak_openid_token_endpoint}" --header 'Content-Type: application/x-www-form-urlencoded' --data-urlencode "username=${admin_name}" --data-urlencode "password=${admin_password}" --data-urlencode 'grant_type=password' --data-urlencode "client_id=${client_id}")
echo "=>Start extracting jwt token from get_admin_token_response."
regex="access_token\":\"([a-zA-Z0-9._-]+)\""
if [[ $get_admin_token_response =~ $regex ]]; then
  jwt_token_admin=${BASH_REMATCH[1]}
  echo "=>Extracting successful. JWT Token for admin user is ${jwt_token_admin}."
else
  echo "Matching failed, jwt token could not be found."
  exit 1
fi
echo "=>Trying to post test company with admin jwt token."
post_testcompany_response=$(curl -X 'POST'   "${backend_url}/companies"   -H 'accept: application/json'   -H 'Content-Type: application/json'   -d "{
\"companyName\": \"${test_company_name}\",
\"headquarters\": \"string\",
\"sector\": \"string\",
\"marketCap\": 0,
\"reportingDateOfMarketCap\": \"2022-05-23\",
\"indices\": [
  \"Cdax\"
],
\"identifiers\": [
  {
    \"identifierType\": \"Lei\",
    \"identifierValue\": \"string\"
  }
]
}" --header "Authorization: bearer ${jwt_token_admin}"
)
echo "=>Start matching post_testcompany_response with regex of expected value."
regex="\"companyId\":\"([a-f0-9\-]+)\""
if [[ $post_testcompany_response =~ $regex ]]; then
  test_companyId=${BASH_REMATCH[1]}
  echo "=>Matching successful, test company could be posted. Company Id is ${test_companyId}."
else
  echo "Matching failed."
  exit 1
fi
echo "=>Trying to get the test company with user jwt token."
get_testcompany_response=$(curl --location --request GET "${backend_url}/companies/${test_companyId}" --header "Authorization: Bearer ${jwt_token_user}")
echo "=>Start matching get_testcompany_response with regex of expected value."
regex="${test_company_name}"
if [[ $get_testcompany_response =~ $regex ]]; then
  echo "=>Matching successful, test company ${test_company_name} could be retrieved."
else
  echo "Matching failed."
  exit 1
fi




echo "=>Trying to post teaser company with admin jwt token."
post_teasercompany_response=$(curl -X 'POST'   "${backend_url}/companies"   -H 'accept: application/json'   -H 'Content-Type: application/json'   -d "{
\"companyName\": \"${teaser_company_name}\",
\"headquarters\": \"string\",
\"sector\": \"string\",
\"marketCap\": 0,
\"reportingDateOfMarketCap\": \"2022-05-23\",
\"indices\": [
  \"Cdax\"
],
\"identifiers\": [
  {
    \"identifierType\": \"Lei\",
    \"identifierValue\": \"string\"
  }
]
}" --header "Authorization: bearer ${jwt_token_admin}"
)
echo "=>Start matching post_teasercompany_response with regex of expected value."
regex="\"companyId\":\"([a-f0-9\-]+)\""
if [[ $post_teasercompany_response =~ $regex ]]; then
  teaser_companyId=${BASH_REMATCH[1]}
  echo "=>Matching successful, teaser company could be posted. Teaser Company Id is ${teaser_companyId}."
else
  echo "Matching failed."
  exit 1
fi
echo "=>Trying to get the teaser company without jwt token."
get_teasercompany_response=$(curl --location --request GET "${backend_url}/companies/${teaser_companyId}")
echo "=>Start matching get_teasercompany_response with regex of expected value."
regex="${teaser_company_name}"
if [[ $get_teasercompany_response =~ $regex ]]; then
  echo "=>Matching successful, teaser company ${teaser_company_name} could be retrieved."
else
  echo "Matching failed."
  exit 1
fi



echo "=>Post one EU taxonomy data set for test company ${test_company_name} and one data set for teaser company ${teaser_company_name} by using admin jwt token."
postdata_response_test_company=$(curl -X 'POST' "${backend_url}/data/eutaxonomies" -H 'accept: application/json' -H 'Content-Type: application/json' -d "{
  \"companyId\": \"${test_companyId}\",
  \"data\": {
    \"Capex\": {
      \"totalAmount\": ${test_company_data_marker},
      \"alignedPercentage\": 0,
      \"eligiblePercentage\": 0
    },
    \"Opex\": {
      \"totalAmount\": 0,
      \"alignedPercentage\": 0,
      \"eligiblePercentage\": 0
    },
    \"Revenue\": {
      \"totalAmount\": 0,
      \"alignedPercentage\": 0,
      \"eligiblePercentage\": 0
    },
    \"Reporting Obligation\": \"Yes\",
    \"Attestation\": \"None\"
  }
}" --header "Authorization: bearer ${jwt_token_admin}"
)
echo "=>Start matching postdata_response_test_company with regex of expected value."
regex="\"dataId\":\"([a-f0-9\-]+:[a-f0-9\-]+)\""
if [[ $postdata_response_test_company =~ $regex ]]; then
  test_dataId=${BASH_REMATCH[1]}
  echo "=>Matching successful, data set for test company could be posted. Data Id is ${test_dataId}."
else
  echo "Matching failed."
  exit 1
fi
postdata_response_teaser_company=$(curl -X 'POST' "${backend_url}/data/eutaxonomies" -H 'accept: application/json' -H 'Content-Type: application/json' -d "{
  \"companyId\": \"${teaser_companyId}\",
  \"data\": {
    \"Capex\": {
      \"totalAmount\": ${teaser_company_data_marker},
      \"alignedPercentage\": 0,
      \"eligiblePercentage\": 0
    },
    \"Opex\": {
      \"totalAmount\": 0,
      \"alignedPercentage\": 0,
      \"eligiblePercentage\": 0
    },
    \"Revenue\": {
      \"totalAmount\": 0,
      \"alignedPercentage\": 0,
      \"eligiblePercentage\": 0
    },
    \"Reporting Obligation\": \"Yes\",
    \"Attestation\": \"None\"
  }
}" --header "Authorization: bearer ${jwt_token_admin}"
)
echo "=>Start matching postdata_response_teaser_company with regex of expected value."
regex="\"dataId\":\"([a-f0-9\-]+:[a-f0-9\-]+)\""
if [[ $postdata_response_teaser_company =~ $regex ]]; then
  teaser_dataId=${BASH_REMATCH[1]}
  echo "=>Matching successful, data set for teaser company could be posted. Data Id is ${teaser_dataId}."
else
  echo "Matching failed."
  exit 1
fi
echo "=>Try to access data set of test company with user jwt token."
getdataset_test_company_response=$(curl --location --request GET "${backend_url}/data/eutaxonomies/${test_dataId}" --header "Authorization: Bearer ${jwt_token_user}")
echo "=>Start matching getdataset_test_company_response with regex of expected value."
regex="${test_company_data_marker}"
if [[ $getdataset_test_company_response =~ $regex ]]; then
  echo "=>Matching successful, the data marker of the test company data set ${test_company_data_marker} could be retrieved."
else
  echo "Matching failed."
  exit 1
fi
echo "=>Try to access data set of teaser company without jwt token."
getdataset_teaser_company_response=$(curl --location --request GET "${backend_url}/data/eutaxonomies/${teaser_dataId}")
echo "=>Start matching getdataset_teaser_company_response with regex of expected value."
echo $getdataset_teaser_company_response
regex="${teaser_company_data_marker}"
if [[ $getdataset_teaser_company_response =~ $regex ]]; then
  echo "=>Matching successful, the data marker of the teaser company data set ${teaser_company_data_marker} could be retrieved."
else
  echo "Matching failed."
  exit 1
fi



echo "

=>Show all-companies-list after tests by using user jwt token:"
curl --location --request GET "${backend_url}/companies" --header "Authorization: Bearer ${jwt_token_user}"
