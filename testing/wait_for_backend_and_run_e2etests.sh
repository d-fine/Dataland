#!/bin/bash
# This script validates, whether the backend-container and other services are running.
# It ensures that the e2e-test do not fail due to unreachable services.

set -ex

is_infrastructure_up () {
  declare -A services
  services["backend"]=http://proxy:80/api/actuator/health/ping
  services["skyminder-dummyserver"]=http://proxy:80/api/actuator/health/skyminderDummyServer
  services["edc-dummyserver"]=http://proxy:80/api/actuator/health/edcDummyServer

  for service in ${!services[@]}; do
    if ! curl ${services[$service]} 2>/dev/null | grep -q UP; then
      echo "$service not yet there"
      return 1
    fi
  done
}
export -f is_infrastructure_up
timeout 240 bash -c "while ! is_infrastructure_up; do echo 'infrastructure not yet completely there - retrying in 1s'; sleep 1; done; echo 'infrastructure up!'"
./gradlew :dataland-e2etests:test :dataland-frontend:npm_run_testpipeline --no-daemon



#timeout 240 sh -c "while ! curl http://proxy:80/api/actuator/health/ping 2>/dev/null | grep -q UP; do echo 'backend server not yet there - retrying in 1s'; sleep 1; done; echo 'backend server responded'"
#timeout 240 sh -c "while ! curl http://proxy:80/api/actuator/health/skyminderDummyServer 2>/dev/null | grep -q UP; do echo 'skyminder dummy server not yet there - retrying in 1s'; sleep 1; done; echo 'skyminder dummy server responded'"
#timeout 240 sh -c "while ! curl http://proxy:80/api/actuator/health/edcDummyServer 2>/dev/null | grep -q UP; do echo 'edc dummy server not yet there - retrying in 1s'; sleep 1; done; echo 'edc dummy server responded'"
#./gradlew :dataland-e2etests:test :dataland-frontend:npm_run_testpipeline --no-daemon