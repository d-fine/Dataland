#!/usr/bin/env bash
set -euxo pipefail

gradle_dependencies=$(grep gradle_dependencies ./build-utils/common.conf | cut -d'=' -f2)
dependencies="./dataland-data-sourcing-service/ ./dataland-backend-utils/ ./environments/.env.uncritical $gradle_dependencies"

./build-utils/base_rebuild_single_docker_image.sh dataland_data_sourcing_service_base ./dataland-data-sourcing-service/DockerfileBase $dependencies

set -o allexport
source ./*github_env.log
set +o allexport

./build-utils/base_rebuild_single_docker_image.sh dataland_data_sourcing_service_production ./dataland-data-sourcing-service/Dockerfile $dependencies

./build-utils/base_rebuild_single_docker_image.sh dataland_data_sourcing_service_test ./dataland-data-sourcing-service/DockerfileTest $dependencies
