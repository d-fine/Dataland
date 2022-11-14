#!/bin/bash
# This script validates, whether the backend-container and other services are running.
# It ensures that the e2e-test do not fail due to unreachable services.

set -euxo pipefail

is_infrastructure_up () {
  declare -A services
  services["backend"]=https://local-dev.dataland.com/api/actuator/health/ping
  services["edc-dummyserver"]=http://dataland-edc:9191/api/dataland/health
  services["keycloak"]=http://local-dev.dataland.com/keycloak/realms/datalandsecurity/

  for service in "${!services[@]}"; do
    if ! curl -L ${services[$service]} 2>/dev/null | grep -q 'UP\|alive\|datalandsecurity'; then
      echo "$service not yet there"
      return 1
    fi
  done
}
export -f is_infrastructure_up

timeout 240 bash -c "while ! is_infrastructure_up; do echo 'infrastructure not yet completely there - retrying in 1s'; sleep 1; done; echo 'infrastructure up!'"

if [[ $CYPRESS_TEST_GROUP -eq 0 ]]; then
  ./gradlew :dataland-e2etests:test --no-daemon --stacktrace
else
  /usr/local/bin/npm --prefix ./dataland-frontend run testpipeline
fi