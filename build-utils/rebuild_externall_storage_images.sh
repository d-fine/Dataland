#!/usr/bin/env bash
set -euxo pipefail

./build-utils/base_rebuild_single_docker_image.sh dataland_external_storage_base \
          ./dataland-external-storage/DockerfileBase ./dataland-external-storage/ \
          ./dataland-backend/backendOpenApi.json \
          ./dataland-backend-utils/ ./dataland-message-queue-utils/ \
          ./build.gradle.kts ./gradle.properties ./settings.gradle.kts ./versions.properties

set -o allexport
source ./*github_env.log
set +o allexport

./build-utils/base_rebuild_single_docker_image.sh dataland_external_storage_test \
          ./dataland-external-storage/DockerfileTest ./dataland-external-storage/ \
          ./dataland-backend/backendOpenApi.json \
          ./dataland-backend-utils/ ./dataland-message-queue-utils/ \
          ./build.gradle.kts ./gradle.properties ./settings.gradle.kts ./versions.properties

./build-utils/base_rebuild_single_docker_image.sh dataland_external_storage_production \
          ./dataland-external-storage/Dockerfile ./dataland-external-storage/ \
          ./dataland-backend/backendOpenApi.json \
          ./dataland-backend-utils/ ./dataland-message-queue-utils/ \
          ./build.gradle.kts ./gradle.properties ./settings.gradle.kts ./versions.properties
