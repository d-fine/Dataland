#!/usr/bin/env bash
set -euxo pipefail

gradle_dependencies=$(grep gradle_dependencies ./build-utils/common.conf | cut -d'=' -f2)
dependencies="./dataland-user-service/ ./dataland-backend-utils/ $gradle_dependencies"

./build-utils/base_rebuild_single_docker_image.sh dataland_user_service_base ./dataland-user-service/DockerfileBase $dependencies

set -o allexport
source ./*github_env.log
set +o allexport

./build-utils/base_rebuild_single_docker_image.sh dataland_user_service_production ./dataland-user-service/Dockerfile $dependencies

./build-utils/base_rebuild_single_docker_image.sh dataland_user_service_test ./dataland-user-service/DockerfileTest $dependencies
