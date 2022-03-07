#!/bin/sh
set -ex
#Start E2E Test and wait for E2E Test completion
docker-compose --project-name dala-e2e-test up -d
# is 15 min long enough for all tests?
timeout 900 sh -c "docker logs dala-e2e-test_e2etests_1 --follow"
docker cp dala-e2e-test_e2etests_1:/app/dataland-frontend/coverage/**/lcov.info .
# Stop Backend causing JaCoCo to write Coverage Report, get it to pwd
docker stop dala-e2e-test_backend_1
timeout 90 sh -c "docker logs dala-e2e-test_backend_1 --follow"
docker cp dala-e2e-test_backend_1:/app/dataland-backend/build/jacoco/bootRun.exec .
docker ps -q dala-e2e-test_e2etests_1
docker container ls
docker image ls

# Check execution success of Test Container
TEST_EXIT_CODE=`docker inspect -f '{{.State.ExitCode}}' dala-e2e-test_e2etests_1`
echo "Docker E2E Testcontainer exited with code $TEST_EXIT_CODE"
return $TEST_EXIT_CODE
