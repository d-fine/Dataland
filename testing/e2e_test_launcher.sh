
echo "TEST_STH" $TEST_STH
echo "TEST_STH_ELSE" $TEST_STH_ELSE
echo "TEST_STH_ELSEE" $TEST_STH_ELSEE

set -ex
if [[ $TEST_EXECUTOR = "CYPRESS" ]]; then
  ./testing/execute_e2e_tests.sh
elif [[ $TEST_EXECUTOR = "E2ETESTS" ]]; then
  ./testing/execute_e2e_tests.sh
elif [[ $TEST_EXECUTOR = "RESTARTABILITY" ]]; then
  ./testing/execute_backend_restartability_test.sh
fi