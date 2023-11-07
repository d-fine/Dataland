#!/usr/bin/env bash
set -euxo pipefail

./build-utils/base_rebuild_single_docker_image.sh dataland_community_manager_base ./dataland-community-manager/DockerfileBase \
          ./dataland-backend-utils/ ./dataland-backend/ ./dataland-api-key-manager/ ./dataland-keycloak-adapter/ \
          ./dataland-community-manager/ ./build.gradle.kts ./gradle.properties ./settings.gradle.kts

set -o allexport
source ./*github_env.log
set +o allexport

./build-utils/base_rebuild_single_docker_image.sh dataland_community_manager_production ./dataland-community-manager/Dockerfile ./dataland-community-manager/ \
        ./dataland-backend-utils/ ./dataland-backend/ ./dataland-api-key-manager/ ./dataland-keycloak-adapter/ \
        ./build.gradle.kts ./gradle.properties ./settings.gradle.kts

./build-utils/base_rebuild_single_docker_image.sh dataland_community_manager_test ./dataland-community-manager/DockerfileTest ./dataland-community-manager/ \
        ./dataland-backend-utils/ ./dataland-backend/ ./dataland-api-key-manager/ ./dataland-keycloak-adapter/ \
        ./build.gradle.kts ./gradle.properties ./settings.gradle.kts
