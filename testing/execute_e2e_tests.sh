#!/bin/bash
set -euxo pipefail
source "$(dirname "$0")"/../deployment/docker_utils.sh

#Start E2E Test and wait for E2E Test completion
docker compose --project-name dala-e2e-test --profile testing up -d || exit
timeout 2400 sh -c "docker logs dala-e2e-test-e2etests-1 --follow"

# Check and validate that all docker containers are indeed healthy
health_check_results=$(get_services_that_are_not_healthy_but_should_be_in_compose_profile testing)
if [ -z "$health_check_results" ]; then
  echo "All relevant containers are healthy!"
else
  echo "Some containers are NOT HEALTHY"
  echo "$health_check_results"
  exit 1
fi

mkdir -p ./cypress/${CYPRESS_TEST_GROUP}
mkdir -p ./reports/${CYPRESS_TEST_GROUP}
docker cp dala-e2e-test-e2etests-1:/app/dataland-frontend/coverage/lcov.info ./lcov-${CYPRESS_TEST_GROUP}.info || true
docker cp dala-e2e-test-e2etests-1:/app/dataland-frontend/cypress/. ./cypress/${CYPRESS_TEST_GROUP}/ || true
docker cp dala-e2e-test-e2etests-1:/app/dataland-e2etests/build/reports/. ./reports/${CYPRESS_TEST_GROUP}/ || true

mkdir -p ./dbdumps/${CYPRESS_TEST_GROUP}
docker exec -i dala-e2e-test-backend-db-1 /bin/bash -c "PGPASSWORD=${BACKEND_DB_PASSWORD} pg_dump --username backend backend" > ./dbdumps/${CYPRESS_TEST_GROUP}/backend-db.sql || true
docker exec -i dala-e2e-test-api-key-manager-db-1 /bin/bash -c "PGPASSWORD=${API_KEY_MANAGER_DB_PASSWORD} pg_dump --username api_key_manager api_key_manager" > ./dbdumps/${CYPRESS_TEST_GROUP}/backend-db.sql || true

# Stop Backend causing JaCoCo to write Coverage Report, get it to pwd
docker exec dala-e2e-test-backend-1 pkill -f spring
timeout 90 sh -c "docker logs dala-e2e-test-backend-1 --follow" > /dev/null
docker cp dala-e2e-test-backend-1:/app/dataland-backend/build/jacoco/bootRun.exec ./bootRun-${CYPRESS_TEST_GROUP}.exec


# This test exists, because an update of SLF4J-API lead to no logging output after the spring logo was printed.
# This was discovered only after the PR was merged.
docker logs dala-e2e-test-backend-1 | grep "Searching for known Datatypes"


# Testing admin-tunnel database connections
pg_isready -d backend -h "localhost" -p 5433
pg_isready -d keycloak -h "localhost" -p 5434
pg_isready -d api_key_manager -h "localhost" -p 5435

# Check execution success of Test Container
TEST_EXIT_CODE=`docker inspect -f '{{.State.ExitCode}}' dala-e2e-test-e2etests-1`
echo "Docker E2E Testcontainer exited with code $TEST_EXIT_CODE"
exit $((TEST_EXIT_CODE))
