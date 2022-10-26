#!/bin/bash
#This script is intended for developers to check whether or not basic tests in the pipeline will fail.
#It takes one parameter to switch between full mode (if nothing or "full" is provided) and short mode (any other input).
#The script must be executed from the same directory it is located in.
set -u

check_rc () {
  local rc=$1
  local message=$2
  local mode=${3:-""}
  if [[ ! $rc -eq 0 ]]; then
    echo "ERROR: $message"
    if [[ ! $mode == --soft ]]; then
      exit 1
    fi
  fi
}

mode="${1:-full}"

log_dir=./log
mkdir -p "$log_dir"
if [[ ! -d $log_dir ]]; then
  echo "Log directory $log_dir does not exist and could not be created."
  exit 1
fi
rm "$log_dir"/*.log 2>/dev/null

declare -A pids
declare -A commands

#Commands for the setup block
commands[clean]=clean
commands[client_frontend]=dataland-frontend:generateAPIClientFrontend
commands[client_e2e]=dataland-e2etests:generateBackendClient

#Commands for the test block
commands[ktlint]="./gradlew ktlintCheck"
commands[detekt]="./gradlew detekt"
commands[backend_unit_tests]="./gradlew dataland-backend:test"
commands[csv_converter_test]="./gradlew dataland-csvconverter:test"
commands[e2e_compilation_test]="./gradlew dataland-e2etests:compileTestKotlin"
commands[eslint]="npm --prefix ./dataland-frontend run lintci"
commands[frontend_compilation_test]="npm --prefix ./dataland-frontend run checkcypresscompilation"
commands[dependency]="npm --prefix ./dataland-frontend run checkdependencies"
commands[frontend_component_tests]="npm --prefix ./dataland-frontend run testcomponent"

if [[ $mode == full ]]; then
  if curl -L https://dataland-local.duckdns.org/api/actuator/health/ping 2>/dev/null | grep -q UP; then
    echo "ERROR: The backend is currently running. This will interfere with the generation of the new OpenAPI specs."
    echo "Shut down the running process and restart the script."
    exit 1
  fi
  setup="clean client_frontend client_e2e"
  tests="ktlint detekt backend_unit_tests csv_converter_test eslint frontend_compilation_test e2e_compilation_test dependency frontend_component_tests"
  echo "Preparing a clean state for running the tests."
else
  setup=""
  tests="ktlint detekt backend_unit_tests csv_converter_test eslint dependency"
  echo "Running in short mode, setup steps will be skipped."
fi

for step in $setup; do
  echo "Executing step: $step"
  ./gradlew ${commands[$step]} &> $log_dir/"$step".log
  check_rc $? "Step $step failed. Please check the log file $log_dir/$step.log"
done

echo "Starting tests in parallel:"

for check in $tests; do
  echo "Executing test: $check"
  ${commands[$check]} &> $log_dir/"$check".log &
  pids[$check]=$!
done

for check in $tests; do
  echo "Waiting for test $check (pid ${pids[$check]})"
  wait ${pids[$check]}
  check_rc $? "Test $check failed. Please check the log file $log_dir/$check" --soft
done

echo "Finished script execution."