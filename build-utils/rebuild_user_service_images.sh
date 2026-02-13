#!/usr/bin/env bash
set -euxo pipefail

gradle_dependencies=$(grep gradle_dependencies ./build-utils/common.conf | cut -d'=' -f2)
dependencies="./dataland-user-service/ ./dataland-backend-utils/ ./dataland-message-queue-utils/ ./dataland-keycloak-adapter/ $gradle_dependencies"

./build-utils/base_rebuild_single_docker_image.sh dataland_user_service_base ./dataland-user-service/DockerfileBase "dataland-user-service:assemble" $dependencies

set -o allexport
source ./*github_env.log
set +o allexport

if [[ "${LOCAL:-}" != "true" ]]; then
  ./build-utils/base_rebuild_single_docker_image.sh dataland_user_service_production ./dataland-user-service/Dockerfile "" $dependencies
fi

./build-utils/base_rebuild_single_docker_image.sh dataland_user_service_test ./dataland-user-service/DockerfileTest "" $dependencies
