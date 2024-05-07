#!/usr/bin/env bash
set -euxo pipefail

gradle_dependencies=$(grep gradle_dependencies ./build-utils/common.conf | cut -d'=' -f2)
dependencies="./dataland-batch-manager/ ./dataland-backend-utils/ ./dataland-backend/backendOpenApi.json $gradle_dependencies"

./build-utils/base_rebuild_single_docker_image.sh dataland_batch_manager_base ./dataland-batch-manager/DockerfileBase $dependencies

set -o allexport
source ./*github_env.log
set +o allexport

./build-utils/base_rebuild_single_docker_image.sh dataland_batch_manager ./dataland-batch-manager/Dockerfile $dependencies



