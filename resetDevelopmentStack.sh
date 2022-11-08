#!/bin/bash

set -euox pipefail

if curl -L https://dataland-local.duckdns.org/api/actuator/health/ping 2>/dev/null | grep -q UP; then
  echo "ERROR: The backend is currently running. This will prevent the new backend from starting."
  echo "Shut down the running process and restart the script."
  exit 1
fi

echo "Clearing Docker..."
docker compose down --remove-orphans
docker volume prune -f
docker image prune -a -f

echo "Clearing frontend clients..."
./gradlew clean
./gradlew assemble

./build-utils/rebuild_backend_production_image.sh &
./build-utils/rebuild_backend_test_image.sh &
./build-utils/rebuild_e2etests_image.sh &
./build-utils/rebuild_frontend_prod_image.sh &
./build-utils/rebuild_frontend_test_image.sh &
./build-utils/rebuild_inbound_admin_proxy_image.sh &
./build-utils/rebuild_inbound_proxy_images.sh &
./build-utils/rebuild_keycloak_image.sh &
./build-utils/rebuild_pgadmin_image.sh &
wait

set -o allexport
source ./*github_env.log
set +o allexport

echo "Initializing Keycloak..."
docker compose --profile init up --build &

keycloak_initializer_container=dataland-keycloak-initializer-1
while ! docker logs $keycloak_initializer_container 2>/dev/null | grep -q "Added user 'admin' to realm 'master'"; do
 echo "Waiting for Keycloak to finish initializing..."
 sleep 5
done

docker compose --profile init down

echo "Starting development stack..."
./startDevelopmentStack.sh