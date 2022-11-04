#!/bin/bash
# Login to the docker repository
set -euxo pipefail

docker login ghcr.io -u $GITHUB_USER -p $GITHUB_TOKEN

# Retrieve the SSL-Certificates for dataland-local.duckdns.org
mkdir -p ./local/certs
scp ubuntu@dataland-letsencrypt.duckdns.org:/etc/letsencrypt/live/dataland-local.duckdns.org/* ./local/certs

./build-utils/rebuild_backend_production_image.sh
./build-utils/rebuild_backend_test_image.sh
./build-utils/rebuild_e2etests_image.sh
./build-utils/rebuild_frontend_prod_image.sh
./build-utils/rebuild_frontend_test_image.sh
./build-utils/rebuild_inbound_admin_proxy_image.sh
./build-utils/rebuild_inbound_proxy_images.sh
./build-utils/rebuild_keycloak_image.sh
./build-utils/rebuild_pgadmin_image.sh
set -o allexport
source ./.env
set +o allexport

set -o allexport
source ./*github_env.log
set +o allexport

# start containers with the stack except frontend and backend
docker compose --profile development down
docker compose --profile development pull
docker compose --profile development up -d --build

#start the backend
./gradlew dataland-backend:bootRun --args='--spring.profiles.active=development' --no-daemon --stacktrace

