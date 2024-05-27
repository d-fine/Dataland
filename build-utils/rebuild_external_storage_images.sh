#!/usr/bin/env bash
set -euxo pipefail

gradle_dependencies=$(grep gradle_dependencies ./build-utils/common.conf | cut -d'=' -f2)
dependencies="./dataland-external-storage/ ./dataland-backend/backendOpenApi.json ./dataland-backend-utils/ ./dataland-message-queue-utils/ $gradle_dependencies"

./build-utils/base_rebuild_single_docker_image.sh dataland_external_storage_base ./dataland-external-storage/DockerfileBase $dependencies

set -o allexport
source ./*github_env.log
set +o allexport

./build-utils/base_rebuild_single_docker_image.sh dataland_external_storage_test ./dataland-external-storage/DockerfileTest $dependencies

./build-utils/base_rebuild_single_docker_image.sh dataland_external_storage_production ./dataland-external-storage/Dockerfile $dependencies