#!/usr/bin/env bash
set -euxo pipefail

gradle_dependencies=$(grep gradle_dependencies ./build-utils/common.conf | cut -d'=' -f2)
dependencies="./dataland-company-service/ ./dataland-backend-utils/ ./dataland-keycloak-adapter/ ./environments/.env.uncritical $gradle_dependencies"
dependencies+=" ./dataland-backend/backendOpenApi.json"

./build-utils/base_rebuild_single_docker_image.sh dataland_company_service_base ./dataland-company-service/DockerfileBase "dataland-company-service:assemble" $dependencies

set -o allexport
source ./*github_env.log
set +o allexport

if [[ "${LOCAL:-}" != "true" ]]; then
  ./build-utils/base_rebuild_single_docker_image.sh dataland_company_service_production ./dataland-company-service/Dockerfile "" $dependencies
fi

./build-utils/base_rebuild_single_docker_image.sh dataland_company_service_test ./dataland-company-service/DockerfileTest "" $dependencies
