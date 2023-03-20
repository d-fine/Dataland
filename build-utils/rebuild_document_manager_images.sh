#!/bin/bash
set -euxo pipefail

./build-utils/base_rebuild_single_docker_image.sh dataland_document_manager_base ./dataland-document-manager/DockerfileBase \
         ./dataland-document-manager/ ./dataland-backend-utils/ ./dataland-keycloak-adapter/ \
         ./dataland-message-queue-utils/ \
         ./dataland-internal-storage/internalStorageOpenApi.json \
         ./build.gradle.kts ./gradle.properties ./settings.gradle.kts

set -o allexport
source ./*github_env.log
set +o allexport

./build-utils/base_rebuild_single_docker_image.sh dataland_document_manager_production ./dataland-document-manager/Dockerfile \
         ./dataland-document-manager/ ./dataland-backend-utils/ ./dataland-keycloak-adapter/ \
         ./dataland-message-queue-utils/ \
         ./dataland-internal-storage/internalStorageOpenApi.json \
         ./build.gradle.kts ./gradle.properties ./settings.gradle.kts

./build-utils/base_rebuild_single_docker_image.sh dataland_document_manager_test ./dataland-document-manager/DockerfileTest \
         ./dataland-document-manager/ ./dataland-backend-utils/ ./dataland-keycloak-adapter/ \
         ./dataland-message-queue-utils/ \
         ./dataland-internal-storage/internalStorageOpenApi.json \
         ./build.gradle.kts ./gradle.properties ./settings.gradle.kts
