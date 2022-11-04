#!/bin/bash
set -euxo pipefail

export GITHUB_ENV=${GITHUB_ENV:-./.env}
./build-utils/rebuild_single_docker_image.sh dataland_keycloak ./dataland-keycloak/Dockerfile \
         ./dataland-keycloak/dataland_theme/ ./dataland-keycloak/start_keycloak.sh \
         ./build.gradle.kts ./gradle.properties ./settings.gradle.kts
./build-utils/rebuild_single_docker_image.sh dataland_proxy_base \
        ./dataland-inbound-proxy/DockerfileBase ./dataland-inbound-proxy/ ./dataland-frontend/src/assets/images/
./build-utils/rebuild_single_docker_image.sh dataland_backend_production ./dataland-backend/Dockerfile ./dataland-backend/ \
         ./build.gradle.kts ./gradle.properties ./settings.gradle.kts
./build-utils/rebuild_single_docker_image.sh dataland_backend_test ./dataland-backend/DockerfileTest ./dataland-backend/ \
         ./build.gradle.kts ./gradle.properties ./settings.gradle.kts
./build-utils/rebuild_single_docker_image.sh dataland_inbound_admin_proxy ./dataland-inbound-admin-proxy/Dockerfile
./build-utils/rebuild_single_docker_image.sh dataland_e2etests ./dataland-e2etests/Dockerfile \
       ./dataland-backend/ ./dataland-e2etests/ ./dataland-frontend/tests/ \
       ./build.gradle.kts ./gradle.properties ./settings.gradle.kts
./build-utils/rebuild_single_docker_image.sh dataland_pgadmin ./dataland-pgadmin/Dockerfile
shopt -s extglob
./build-utils/rebuild_single_docker_image.sh dataland_frontend_production ./dataland-frontend/Dockerfile \
         ./dataland-frontend/!(tests*) ./build.gradle.kts ./gradle.properties ./settings.gradle.kts
./build-utils/rebuild_single_docker_image.sh dataland_frontend_test ./dataland-frontend/DockerfileTest \
         ./dataland-frontend/ ./build.gradle.kts ./gradle.properties ./settings.gradle.kts
set -o allexport
source $GITHUB_ENV
set +o allexport
echo "{\"commit\":\"$(git rev-parse HEAD)\",\"time\":\"$(git show -s --format=%aI HEAD)\",\"branch\":\"$(git rev-parse --abbrev-ref HEAD)\"}" > gitinfo
PROXY_ENVIRONMENT="production" ./build-utils/rebuild_single_docker_image.sh dataland_inbound_proxy_production \
        ./dataland-inbound-proxy/Dockerfile ./dataland-inbound-proxy/ ./dataland-frontend/src/assets/images/ ./gitinfo
PROXY_ENVIRONMENT="development" ./build-utils/rebuild_single_docker_image.sh dataland_inbound_proxy_development \
        ./dataland-inbound-proxy/Dockerfile ./dataland-inbound-proxy/ ./dataland-frontend/src/assets/images/ ./gitinfo
cp $GITHUB_ENV github_env.log