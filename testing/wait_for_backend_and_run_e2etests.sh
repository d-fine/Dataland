#!/bin/bash
# This script validates, whether the backend-container and other services are running.
# It ensures that the e2e-test do not fail due to unreachable services.

set -euxo pipefail

is_infrastructure_up () {
  declare -A services
  services["backend"]=https://local-dev.dataland.com/api/public/actuator/health/ping
  services["internal-storage"]=http://local-dev.dataland.com/internal-storage/actuator/health/ping
  services["keycloak"]=http://local-dev.dataland.com/keycloak/realms/datalandsecurity/
  services["api-key-manager"]=http://local-dev.dataland.com/api-keys/actuator/health/ping
  services["qa-service"]=http://local-dev.dataland.com/qa/actuator/health/ping

  for service in "${!services[@]}"; do
    if ! curl -L ${services[$service]} 2>/dev/null | grep -q 'UP\|alive\|datalandsecurity'; then
      echo "$service not yet there"
      return 1
    fi
  done
  echo "-----------------------------"
  echo "All infrastructure is up now!"
  echo "-----------------------------"
}
export -f is_infrastructure_up

timeout 240 bash -c "while ! is_infrastructure_up; do echo 'infrastructure not yet completely there - retrying in 1s'; sleep 1; done; echo 'infrastructure up!'"

if [[ $CYPRESS_TEST_GROUP -eq 0 ]]; then
  ./gradlew :dataland-e2etests:test --no-daemon --stacktrace
else
  npm --prefix ./dataland-frontend run testpipeline
fi