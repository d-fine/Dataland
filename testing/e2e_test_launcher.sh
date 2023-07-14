set -ex

mkdir -p ~/.docker/cli-plugins/
curl -SL https://github.com/docker/compose/releases/download/v2.19.0/docker-compose-linux-x86_64 -o ~/.docker/cli-plugins/docker-compose
chmod +x ~/.docker/cli-plugins/docker-compose
docker compose version

if [[ $TEST_EXECUTOR = "CYPRESS" ]]; then
  ./testing/execute_e2e_tests.sh
elif [[ $TEST_EXECUTOR = "E2ETESTS" ]]; then
  ./testing/execute_e2e_tests.sh
elif [[ $TEST_EXECUTOR = "RESTARTABILITY" ]]; then
  ./testing/execute_backend_restartability_test.sh
fi