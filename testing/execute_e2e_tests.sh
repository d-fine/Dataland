#!/bin/bash
set -euxo pipefail
source "$(dirname "$0")"/e2e_test_utils.sh

#Start E2E Test and wait for E2E Test completion
docker compose --project-name dala-e2e-test --profile testing up -d || exit
timeout 2400 sh -c "docker logs dala-e2e-test-e2etests-1 --follow"

# Check and validate that all docker containers are indeed healthy
health_check_results = $(require_services_healthy "proxy" "admin-proxy" "backend" "backend-db" "e2etests" "frontend" "keycloak-db" "keycloak-initializer" "pgadmin")
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

# Check execution success of Test Container
TEST_EXIT_CODE=`docker inspect -f '{{.State.ExitCode}}' dala-e2e-test-e2etests-1`
echo "Docker E2E Testcontainer exited with code $TEST_EXIT_CODE"
exit $((TEST_EXIT_CODE))
