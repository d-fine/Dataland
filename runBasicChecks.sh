#!/bin/bash
#This script is intended for developers to check whether or not basic tests in the pipeline will fail.
#It takes one parameter to switch between full mode (if nothing or "full" is provided) and short mode (any other input).
#The script must be executed from the same directory it is located in.
set -euo pipefail

exit_log () {
  local rc=$1
  local message=$2
  echo "ERROR: $message"
  exit "$rc"
}

mode="${1:-full}"

log_dir=./log
mkdir -p "$log_dir"
rm "$log_dir"/*.log 2>/dev/null || echo "No files to clean up."

declare -A pids
declare -A commands

#Commands for the setup block
commands[clean]=clean
commands[client_frontend]=dataland-frontend:generateAPIClientFrontend
commands[client_e2e]=dataland-e2etests:generateBackendClient

#Commands for the test block
commands[ktlint]="./gradlew ktlintFormat"
commands[detekt]="./gradlew detekt"
commands[backend_unit_tests]="./gradlew dataland-backend:test"
commands[csv_converter_test]="./gradlew dataland-csvconverter:test"
commands[e2e_compilation]="./gradlew dataland-e2etests:compileTestKotlin"
commands[eslint]="npm --prefix ./dataland-frontend run lint"
commands[cypress_compilation]="npm --prefix ./dataland-frontend run checkcypresscompilation"
commands[frontend_compilation]="npm --prefix ./dataland-frontend run build"
commands[fixture_compilation]="npm --prefix ./dataland-frontend run checkfakefixturecompilation"
commands[dependency]="npm --prefix ./dataland-frontend run checkdependencies"
commands[frontend_component_tests]="npm --prefix ./dataland-frontend run testcomponent"

tests="ktlint detekt eslint dependency frontend_compilation cypress_compilation fixture_compilation e2e_compilation"
if [[ $mode == full ]]; then
  if curl -L https://local-dev.dataland.com/api/actuator/health/ping 2>/dev/null | grep -q UP; then
    echo "ERROR: The backend is currently running. This will interfere with the generation of the new OpenAPI specs."
    echo "Shut down the running process and restart the script."
    exit 1
  fi
  setup="clean client_frontend client_e2e"
  tests+=" backend_unit_tests csv_converter_test frontend_component_tests"
  echo "Preparing a clean state for running the tests."
else
  setup=""
  echo "Running in short mode, setup steps will be skipped."
fi

for step in $setup; do
  echo "Executing step: $step"
  ./gradlew ${commands[$step]} &> "$log_dir/$step.log" || exit_log $? "Step $step failed. Please check the log file $log_dir/$step.log"
done

echo "Starting tests in parallel:"

for check in $tests; do
  echo "Executing test: $check"
  ${commands[$check]} &> $log_dir/"$check".log &
  pids[$check]=$!
done

for check in $tests; do
  echo "Waiting for test $check (pid ${pids[$check]})"
  wait ${pids[$check]} || echo "Test $check failed. Please check the log file $log_dir/$check"
done

echo "Finished script execution."