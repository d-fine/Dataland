#!/usr/bin/env bash
set -euxo pipefail

./build-utils/base_rebuild_single_docker_image.sh dataland_qa_service_base ./dataland-qa-service/DockerfileBase \
          ./dataland-backend-utils/ ./dataland-message-queue-utils/ ./dataland-api-key-manager/ ./dataland-keycloak-adapter/ \
          ./dataland-qa-service/ ./build.gradle.kts ./gradle.properties ./settings.gradle.kts ./versions.properties

set -o allexport
source ./*github_env.log
set +o allexport

./build-utils/base_rebuild_single_docker_image.sh dataland_qa_service_production ./dataland-qa-service/Dockerfile ./dataland-qa-service/ \
        ./dataland-backend-utils/ ./dataland-message-queue-utils/ ./dataland-api-key-manager/ ./dataland-keycloak-adapter/ \
        ./build.gradle.kts ./gradle.properties ./settings.gradle.kts ./versions.properties

./build-utils/base_rebuild_single_docker_image.sh dataland_qa_service_test ./dataland-qa-service/DockerfileTest ./dataland-qa-service/ \
        ./dataland-backend-utils/ ./dataland-message-queue-utils/ ./dataland-api-key-manager/ ./dataland-keycloak-adapter/ \
        ./build.gradle.kts ./gradle.properties ./settings.gradle.kts ./versions.properties
