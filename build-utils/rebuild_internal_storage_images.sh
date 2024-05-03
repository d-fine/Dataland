#!/usr/bin/env bash
set -euxo pipefail

gradle_dependencies=$(grep gradle_dependencies ./build-utils/common.conf | cut -d'=' -f2)
dependencies="./dataland-internal-storage/ ./dataland-backend-utils/ ./dataland-message-queue-utils/ $gradle_dependencies"
dependencies+=" ./dataland-backend/backendOpenApi.json ./dataland-document-manager/documentManagerOpenApi.json"

./build-utils/base_rebuild_single_docker_image.sh dataland_internal_storage_base ./dataland-internal-storage/DockerfileBase $dependencies

set -o allexport
source ./*github_env.log
set +o allexport

./build-utils/base_rebuild_single_docker_image.sh dataland_internal_storage_test ./dataland-internal-storage/DockerfileTest $dependencies

./build-utils/base_rebuild_single_docker_image.sh dataland_internal_storage_production ./dataland-internal-storage/Dockerfile $dependencies
