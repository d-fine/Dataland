#!/usr/bin/env bash
set -euxo pipefail

gradle_dependencies=$(grep gradle_dependencies ./build-utils/common.conf | cut -d'=' -f2)
dependencies="./dataland-data-exporter/ ./dataland-backend-utils/ ./dataland-keycloak-adapter/ ./environments/.env.uncritical $gradle_dependencies"
dependencies+=" ./dataland-backend/backendOpenApi.json"

./build-utils/base_rebuild_single_docker_image.sh dataland_data_exporter_base ./dataland-data-exporter/DockerfileBase "dataland-data-exporter:assemble" $dependencies

set -o allexport
source ./*github_env.log
set +o allexport

if [[ "${LOCAL:-}" != "true" ]]; then
  ./build-utils/base_rebuild_single_docker_image.sh dataland_data_exporter_production ./dataland-data-exporter/Dockerfile "" $dependencies
fi

./build-utils/base_rebuild_single_docker_image.sh dataland_data_exporter_test ./dataland-data-exporter/DockerfileTest "" $dependencies
