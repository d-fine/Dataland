#!/usr/bin/env bash
set -euxo pipefail

gradle_dependencies=$(grep gradle_dependencies ./build-utils/common.conf | cut -d'=' -f2)
dependencies="./dataland-specification-service/ ./dataland-backend-utils/ ./dataland-specification-lib/ $gradle_dependencies"

./build-utils/base_rebuild_single_docker_image.sh dataland_specification_service_base ./dataland-specification-service/DockerfileBase $dependencies

set -o allexport
source ./*github_env.log
set +o allexport

./build-utils/base_rebuild_single_docker_image.sh dataland_specification_service_production ./dataland-specification-service/Dockerfile $dependencies

./build-utils/base_rebuild_single_docker_image.sh dataland_specification_service_test ./dataland-specification-service/DockerfileTest $dependencies
