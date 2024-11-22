#!/usr/bin/env bash
set -euxo pipefail
source "$(dirname "$0")"/../deployment/docker_utils.sh

#Start E2E Test and wait for E2E Test completion
docker compose --project-name dala-e2e-test --profile testing pull -q
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
mkdir -p ./coverage/${CYPRESS_TEST_GROUP}
docker cp dala-e2e-test-e2etests-1:/app/dataland-frontend/coverage/lcov.info ./lcov-${CYPRESS_TEST_GROUP}.info || true
docker cp dala-e2e-test-e2etests-1:/app/dataland-frontend/coverage/. ./coverage/${CYPRESS_TEST_GROUP} || true
docker cp dala-e2e-test-e2etests-1:/app/dataland-frontend/cypress/. ./cypress/${CYPRESS_TEST_GROUP}/ || true
docker cp dala-e2e-test-e2etests-1:/app/dataland-e2etests/build/reports/. ./reports/${CYPRESS_TEST_GROUP}/ || true

mkdir -p ./dbdumps/${CYPRESS_TEST_GROUP}
docker exec -i dala-e2e-test-backend-db-1 /bin/bash -c "PGPASSWORD=${BACKEND_DB_PASSWORD} pg_dump --username backend backend" > ./dbdumps/${CYPRESS_TEST_GROUP}/backend-db.sql || true
docker exec -i dala-e2e-test-api-key-manager-db-1 /bin/bash -c "PGPASSWORD=${API_KEY_MANAGER_DB_PASSWORD} pg_dump --username api_key_manager api_key_manager" > ./dbdumps/${CYPRESS_TEST_GROUP}/api-key-manager-db.sql || true
docker exec -i dala-e2e-test-internal-storage-db-1 /bin/bash -c "PGPASSWORD=${INTERNAL_STORAGE_DB_PASSWORD} pg_dump --username internal_storage internal_storage" > ./dbdumps/${CYPRESS_TEST_GROUP}/internal-storage-db.sql || true
docker exec -i dala-e2e-test-document-manager-db-1 /bin/bash -c "PGPASSWORD=${DOCUMENT_MANAGER_DB_PASSWORD} pg_dump --username document_manager document_manager" > ./dbdumps/${CYPRESS_TEST_GROUP}/document-manager-db.sql || true
docker exec -i dala-e2e-test-qa-service-db-1 /bin/bash -c "PGPASSWORD=${QA_SERVICE_DB_PASSWORD} pg_dump --username qa_service qa_service" > ./dbdumps/${CYPRESS_TEST_GROUP}/qa-service-db.sql || true
docker exec -i dala-e2e-test-community-manager-db-1 /bin/bash -c "PGPASSWORD=${COMMUNITY_MANAGER_DB_PASSWORD} pg_dump --username community_manager community_manager" > ./dbdumps/${CYPRESS_TEST_GROUP}/community-manager-db.sql || true
docker exec -i dala-e2e-test-email-service-db-1 /bin/bash -c "PGPASSWORD=${EMAIL_SERVICE_DB_PASSWORD} pg_dump --username email_service email_service" > ./dbdumps/${CYPRESS_TEST_GROUP}/email-service-db.sql || true

# Stop services to make JaCoCo write the Coverage Reports and copy them to pwd
services="backend api-key-manager document-manager internal-storage qa-service community-manager email-service external-storage data-exporter specification-service"
for service in $services
do
  docker exec dala-e2e-test-${service}-1 pkill -f java
  timeout 90 sh -c "docker logs dala-e2e-test-${service}-1 --follow" > /dev/null
  docker cp dala-e2e-test-${service}-1:/jacoco.exec ./${service}-bootRun-${CYPRESS_TEST_GROUP}.exec
done

# This test exists, because an update of SLF4J-API lead to no logging output after the spring logo was printed.
# This was discovered only after the PR was merged.
docker logs dala-e2e-test-backend-1 | grep "Searching for known Datatypes"

# Testing admin-tunnel database connections
pg_isready -d backend -h "localhost" -p 5433
pg_isready -d keycloak -h "localhost" -p 5434
pg_isready -d api_key_manager -h "localhost" -p 5435
pg_isready -d internal_storage -h "localhost" -p 5436
pg_isready -d document_manager -h "localhost" -p 5437
pg_isready -d qa_service -h "localhost" -p 5438
pg_isready -d community_manager -h "localhost" -p 5439
pg_isready -d email_service -h "localhost" -p 5440

# Check execution success of Test Container
TEST_EXIT_CODE=`docker inspect -f '{{.State.ExitCode}}' dala-e2e-test-e2etests-1`
echo "Docker E2E Testcontainer exited with code $TEST_EXIT_CODE"
exit $((TEST_EXIT_CODE))
