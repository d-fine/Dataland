#!/bin/sh
set -x
./gradlew dataland-keycloak:dataland_theme:login:buildTheme --no-daemon --stacktrace
#Start E2E Test and wait for E2E Test completion
docker compose --project-name dala-e2e-test --profile testing up -d || exit
timeout 2400 sh -c "docker logs dala-e2e-test-e2etests-1 --follow"
E2ETEST_TIMEOUT_EXIT_CODE=$?
mkdir -p ./cypress/${CYPRESS_TEST_GROUP}
mkdir -p ./reports/${CYPRESS_TEST_GROUP}
mkdir -p ./lcov-reports/${CYPRESS_TEST_GROUP}
docker cp dala-e2e-test-e2etests-1:/app/dataland-frontend/coverage/e2e/lcov.info ./lcov-${CYPRESS_TEST_GROUP}.info
docker cp dala-e2e-test-e2etests-1:/app/dataland-frontend/coverage/e2e/lcov-report/. ./lcov-reports/${CYPRESS_TEST_GROUP}/
docker cp dala-e2e-test-e2etests-1:/app/dataland-frontend/cypress/. ./cypress/${CYPRESS_TEST_GROUP}/
docker cp dala-e2e-test-e2etests-1:/app/dataland-e2etests/build/reports/. ./reports/${CYPRESS_TEST_GROUP}/

mkdir -p ./dbdumps/${CYPRESS_TEST_GROUP}
docker exec -i dala-e2e-test-backend-db-1 /bin/bash -c "PGPASSWORD=${BACKEND_DB_PASSWORD} pg_dump --username backend backend" > ./dbdumps/${CYPRESS_TEST_GROUP}/backend-db.sql

# Stop Backend causing JaCoCo to write Coverage Report, get it to pwd
docker exec dala-e2e-test-backend-1 pkill -f spring
timeout 90 sh -c "docker logs dala-e2e-test-backend-1 --follow" > /dev/null
BACKEND_TIMEOUT_EXIT_CODE=$?
docker cp dala-e2e-test-backend-1:/app/dataland-backend/build/jacoco/bootRun.exec ./bootRun-${CYPRESS_TEST_GROUP}.exec

# Write the logs of the docker container for later upload and analysis
mkdir -p ./dockerLogs/${CYPRESS_TEST_GROUP}
docker ps -a > ./dockerLogs/${CYPRESS_TEST_GROUP}/ps.log
for docker_service in $(sudo docker ps --all --format "{{.Names}}");
do
  docker logs "$docker_service" > ./dockerLogs/${CYPRESS_TEST_GROUP}/"$docker_service".log 2>&1
done

# This test exists, because an update of SLF4J-API lead to no logging output after the spring logo was printed.
# This was discovered only after the PR was merged.
grep "Searching for known Datatypes" ./dockerLogs/${CYPRESS_TEST_GROUP}/dala-e2e-test-backend-1.log
LOG_TEST_EXIT_CODE=$?

# Check execution success of Test Container
TEST_EXIT_CODE=`docker inspect -f '{{.State.ExitCode}}' dala-e2e-test-e2etests-1`
echo "E2ETEST Timeout exited with exit code $E2ETEST_TIMEOUT_EXIT_CODE"
echo "BACKEND Timeout exited with exit code $BACKEND_TIMEOUT_EXIT_CODE"
echo "Docker E2E Testcontainer exited with code $TEST_EXIT_CODE"
echo "Log-Existence test existed with exit code $LOG_TEST_EXIT_CODE"
exit $((E2ETEST_TIMEOUT_EXIT_CODE+BACKEND_TIMEOUT_EXIT_CODE+TEST_EXIT_CODE+LOG_TEST_EXIT_CODE))
