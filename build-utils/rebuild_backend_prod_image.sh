#!/bin/bash
set -euxo pipefail

./build-utils/base_rebuild_single_docker_image.sh dataland_backend_base ./dataland-backend/DatalandBackendBaseDockerfile \
         ./dataland-backend/ ./dataland-backend-utils/ ./dataland-keycloak-adapter/ ./build.gradle.kts ./gradle.properties ./settings.gradle.kts

set -o allexport
source ./*github_env.log
set +o allexport

./build-utils/base_rebuild_single_docker_image.sh dataland_backend_production ./dataland-backend/Dockerfile ./dataland-backend/ \
        ./dataland-backend-utils/ ./dataland-keycloak-adapter/ ./build.gradle.kts ./gradle.properties ./settings.gradle.kts
