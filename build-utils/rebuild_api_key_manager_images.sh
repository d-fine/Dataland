#!/usr/bin/env bash
set -euxo pipefail

gradle_dependencies=$(grep gradle_dependencies ./build-utils/common.conf | cut -d'=' -f2)
dependencies="./dataland-api-key-manager/ ./dataland-backend-utils/ ./environments/.env.uncritical $gradle_dependencies"

./build-utils/base_rebuild_single_docker_image.sh dataland_api_key_manager_base ./dataland-api-key-manager/DockerfileBase $dependencies

set -o allexport
source ./*github_env.log
set +o allexport

./build-utils/base_rebuild_single_docker_image.sh dataland_api_key_manager_production ./dataland-api-key-manager/Dockerfile $dependencies

./build-utils/base_rebuild_single_docker_image.sh dataland_api_key_manager_test ./dataland-api-key-manager/DockerfileTest $dependencies
