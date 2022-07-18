#!/bin/sh
set -x
timeout 2400 sh -c "docker-compose --profile testing up --build"
#Start E2E Test and wait for E2E Test completion
docker-compose --project-name dala-e2e-test --profile testing up -d --build || exit
timeout 2400 sh -c "docker logs dala-e2e-test_e2etests_1 --follow"
E2ETEST_TIMEOUT_EXIT_CODE=$?
docker cp dala-e2e-test_e2etests_1:/app/dataland-frontend/coverage/e2e/lcov.info .
docker cp dala-e2e-test_e2etests_1:/app/dataland-frontend/cypress/ .
docker cp dala-e2e-test_e2etests_1:/app/dataland-e2etests/build/reports/ .

# Stop Backend causing JaCoCo to write Coverage Report, get it to pwd
docker exec dala-e2e-test_backend_1 pkill -f spring
timeout 90 sh -c "docker logs dala-e2e-test_backend_1 --follow" > /dev/null
BACKEND_TIMEOUT_EXIT_CODE=$?
docker cp dala-e2e-test_backend_1:/app/dataland-backend/build/jacoco/bootRun.exec .

# Write the logs of the docker container for later upload and analysis
mkdir -p ./dockerLogs
docker ps -a > ./dockerLogs/ps.log
for docker_service in $(sudo docker ps --all --format "{{.Names}}");
do
  docker logs "$docker_service" > ./dockerLogs/"$docker_service".log 2>&1
done

# Check execution success of Test Container
TEST_EXIT_CODE=`docker inspect -f '{{.State.ExitCode}}' dala-e2e-test_e2etests_1`
echo "E2ETEST Timeout exited with exit code $E2ETEST_TIMEOUT_EXIT_CODE"
echo "BACKEND Timeout exited with exit code $BACKEND_TIMEOUT_EXIT_CODE"
echo "Docker E2E Testcontainer exited with code $TEST_EXIT_CODE"
exit $((TEST_EXIT_CODE+E2ETEST_TIMEOUT_EXIT_CODE+BACKEND_TIMEOUT_EXIT_CODE))
