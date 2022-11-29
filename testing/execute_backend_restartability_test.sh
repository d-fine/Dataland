#!/bin/bash
set -euxo pipefail
source "$(dirname "$0")/authorisation_tools.sh"
api_key=$(getApiKeyWithUsernamePassword data_reader "$KEYCLOAK_READER_PASSWORD" "https://local-dev.dataland.com")
CYPRESS_TEST_GROUP=101 ./testing/execute_e2e_tests.sh
curl -X 'GET' \
  'https://local-dev.dataland.com/api/companies' \
  -H 'accept: application/json' \
  -H "dataland-api-key: $api_key"
docker compose --project-name dala-e2e-test --profile testing down
CYPRESS_TEST_GROUP=102 CYPRESS_SINGLE_POPULATE="true" CYPRESS_AWAIT_PREPOPULATION_RETRIES=3 ./testing/execute_e2e_tests.sh
curl -X 'GET' \
  'https://local-dev.dataland.com/api/companies' \
  -H 'accept: application/json' \
  -H "dataland-api-key: $api_key"
