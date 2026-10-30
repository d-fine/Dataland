#!/usr/bin/env bash
set -euxo pipefail

gradle_dependencies=$(grep gradle_dependencies ./build-utils/common.conf | cut -d'=' -f2)
dependencies="./dataland-proxy-finder/ ./dataland-backend-utils/ ./dataland-keycloak-adapter/ ./environments/.env.uncritical $gradle_dependencies"
dependencies+=" ./dataland-backend/backendOpenApi.json"

./build-utils/base_rebuild_single_docker_image.sh dataland_proxy_finder_base ./dataland-proxy-finder/DockerfileBase "dataland-proxy-finder:assemble" $dependencies

set -o allexport
source ./*github_env.log
set +o allexport

if [[ "${LOCAL:-}" != "true" ]]; then
  ./build-utils/base_rebuild_single_docker_image.sh dataland_proxy_finder_production ./dataland-proxy-finder/Dockerfile "" $dependencies
fi

./build-utils/base_rebuild_single_docker_image.sh dataland_proxy_finder_test ./dataland-proxy-finder/DockerfileTest "" $dependencies
