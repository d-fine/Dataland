#!/usr/bin/env bash
set -euxo pipefail

gradle_dependencies=$(grep gradle_dependencies ./build-utils/common.conf | cut -d'=' -f2)
dependencies="./dataland-community-manager/ ./dataland-backend-utils/ ./dataland-keycloak-adapter/ ./dataland-message-queue-utils/ $gradle_dependencies"
dependencies+=" ./dataland-backend/backendOpenApi.json"

./build-utils/base_rebuild_single_docker_image.sh dataland_community_manager_base ./dataland-community-manager/DockerfileBase $dependencies

set -o allexport
source ./*github_env.log
set +o allexport

./build-utils/base_rebuild_single_docker_image.sh dataland_community_manager_production ./dataland-community-manager/Dockerfile $dependencies

./build-utils/base_rebuild_single_docker_image.sh dataland_community_manager_test ./dataland-community-manager/DockerfileTest $dependencies
