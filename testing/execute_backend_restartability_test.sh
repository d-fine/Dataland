#!/bin/bash
set -euxo pipefail
source "$(dirname "$0")/authorisation_tools.sh"
CYPRESS_TEST_GROUP=101 ./testing/execute_e2e_tests.sh
#api_key=$(getApiKeyWithUsernamePassword data_reader "$KEYCLOAK_READER_PASSWORD" "https://localhost" "local-dev.dataland.com")
#if [[ "$api_key" =~ "^Unable to extract token" ]]; then
#  echo $api_key
#  return 1
#fi
#curl -X 'GET' \
#  'https://localhost/api/companies' \
#  -H 'accept: application/json' \
#  -H "dataland-api-key: $api_key" \
#  -H "Host: local-dev.dataland.com" \
#  --insecure
#docker compose --project-name dala-e2e-test --profile testing down
#CYPRESS_TEST_GROUP=102 CYPRESS_SINGLE_POPULATE="true" CYPRESS_AWAIT_PREPOPULATION_RETRIES=3 ./testing/execute_e2e_tests.sh
#curl -X 'GET' \
#  'https://localhost/api/companies' \
#  -H 'accept: application/json' \
#  -H "dataland-api-key: $api_key" \
#  -H "Host: local-dev.dataland.com" \
#  --insecure
