#!/bin/bash
# This script validates, whether the backend-container and other services are running.
# It ensures that the e2e-test do not fail due to unreachable services.

set -ex

is_infrastructure_up () {
  declare -A services
  services["backend"]=https://dataland-local.duckdns.org/api/actuator/health/ping
  services["skyminder-dummyserver"]=http://skyminder-dummyserver:8080/actuator/health
  services["edc-dummyserver"]=http://dataland-edc:9191/api/dataland/health

  for service in "${!services[@]}"; do
    if ! curl -L ${services[$service]} 2>/dev/null | grep -q 'UP\|alive'; then
      echo "$service not yet there"
      return 1
    fi
  done
}
export -f is_infrastructure_up
./gradlew :dataland-e2etests:compileTestKotlin :dataland-frontend:generateAPIClientFrontend :dataland-frontend:build --no-daemon --stacktrace
timeout 240 bash -c "while ! is_infrastructure_up; do echo 'infrastructure not yet completely there - retrying in 1s'; sleep 1; done; echo 'infrastructure up!'"
./gradlew :dataland-e2etests:test :dataland-frontend:generateAPIClientFrontend :dataland-frontend:npm_run_testpipeline --no-daemon --stacktrace
GRADLE_EXIT_CODE=$?
echo "gradle exit code $GRADLE_EXIT_CODE"
exit $GRADLE_EXIT_CODE
