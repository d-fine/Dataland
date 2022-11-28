#!/bin/bash
set -euxo pipefail

./build-utils/base_rebuild_single_docker_image.sh dataland_api_key_manager_base ./dataland-api-key-manager/DatalandApiKeyManagerBaseDockerfile \
         ./dataland-api-key-manager/ ./dataland-backend-utils/ ./build.gradle.kts ./gradle.properties ./settings.gradle.kts

set -o allexport
source ./*github_env.log
set +o allexport

./build-utils/base_rebuild_single_docker_image.sh dataland_api_key_manager_production ./dataland-api-key-manager/Dockerfile ./dataland-api-key-manager/ \
          ./dataland-backend-utils/ ./build.gradle.kts ./gradle.properties ./settings.gradle.kts

./build-utils/base_rebuild_single_docker_image.sh dataland_api_key_manager_test ./dataland-api-key-manager/DockerfileTest ./dataland-api-key-manager/ \
          ./dataland-backend-utils/ ./build.gradle.kts ./gradle.properties ./settings.gradle.kts
