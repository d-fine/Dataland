#!/bin/bash
set -euxo pipefail

./build-utils/base_rebuild_single_docker_image.sh dataland_internal_storage_base \
          ./dataland-internal-storage/DatalandInternalStorageBaseDockerfile ./dataland-internal-storage/ \
          ./dataland-backend-utils/ ./build.gradle.kts ./gradle.properties ./settings.gradle.kts

set -o allexport
source ./*github_env.log
set +o allexport

./build-utils/base_rebuild_single_docker_image.sh dataland_internal_storage_test \
          ./dataland-internal-storage/DockerfileTest ./dataland-internal-storage/ \
          ./dataland-backend-utils/ ./build.gradle.kts ./gradle.properties ./settings.gradle.kts

./build-utils/base_rebuild_single_docker_image.sh dataland_internal_storage_production \
          ./dataland-internal-storage/Dockerfile ./dataland-internal-storage/ \
          ./dataland-backend-utils/ ./build.gradle.kts ./gradle.properties ./settings.gradle.kts
