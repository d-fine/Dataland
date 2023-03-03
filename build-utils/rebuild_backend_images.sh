#!/bin/bash
set -euxo pipefail

./build-utils/base_rebuild_single_docker_image.sh dataland_backend_base ./dataland-backend/DockerfileBase \
         ./dataland-backend/ ./dataland-backend-utils/ ./dataland-keycloak-adapter/ \
         ./dataland-message-queue-utils/ \
         ./dataland-api-key-manager/apiKeyManagerOpenApi.json ./dataland-internal-storage/internalStorageOpenApi.json \
         ./build.gradle.kts ./gradle.properties ./settings.gradle.kts

set -o allexport
source ./*github_env.log
set +o allexport

./build-utils/base_rebuild_single_docker_image.sh dataland_backend_production ./dataland-backend/Dockerfile ./dataland-backend/ \
         ./dataland-backend-utils/ ./dataland-keycloak-adapter/ ./dataland-api-key-manager/apiKeyManagerOpenApi.json \
         ./dataland-message-queue-utils/ \
         ./dataland-internal-storage/internalStorageOpenApi.json \
         ./build.gradle.kts ./gradle.properties ./settings.gradle.kts

./build-utils/base_rebuild_single_docker_image.sh dataland_backend_test ./dataland-backend/DockerfileTest ./dataland-backend/ \
         ./dataland-backend-utils/ ./dataland-keycloak-adapter/ \
         ./dataland-message-queue-utils/ \
         ./dataland-api-key-manager/apiKeyManagerOpenApi.json ./dataland-internal-storage/internalStorageOpenApi.json \
         ./build.gradle.kts ./gradle.properties ./settings.gradle.kts
