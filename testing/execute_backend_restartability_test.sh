#!/bin/sh
set -ex
CYPRESS_TEST_GROUP=101 ./testing/execute_e2e_tests.sh

# The backend is down now, but the proxy is still up. Test if the proxy returns errors in the expected format
if ! curl -L "https://dataland-local.duckdns.org/api/test" 2>/dev/null | grep -q 'proxy-error-502'; then
  echo "Proxy does not seem to respond with the desired error format"
  exit 1
fi

docker compose --project-name dala-e2e-test --profile testing down
CYPRESS_TEST_GROUP=102 CYPRESS_SINGLE_POPULATE="true" CYPRESS_AWAIT_PREPOPULATION_RETRIES=3 ./testing/execute_e2e_tests.sh
