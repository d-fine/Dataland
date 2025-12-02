#!/usr/bin/env bash
set -euxo pipefail

gradle_dependencies=$(grep gradle_dependencies ./build-utils/common.conf | cut -d'=' -f2)
dependencies="./dataland-accounting-service/ ./dataland-backend-utils/ ./dataland-message-queue-utils/ ./dataland-keycloak-adapter/ ./environments/.env.uncritical $gradle_dependencies"
dependencies+=" ./dataland-community-manager/communityManagerOpenApi.json"

./build-utils/base_rebuild_single_docker_image.sh dataland_accounting_service_base ./dataland-accounting-service/DockerfileBase "dataland-accounting-service:assemble" $dependencies

set -o allexport
source ./*github_env.log
set +o allexport

./build-utils/base_rebuild_single_docker_image.sh dataland_accounting_service_production ./dataland-accounting-service/Dockerfile "" $dependencies

./build-utils/base_rebuild_single_docker_image.sh dataland_accounting_service_test ./dataland-accounting-service/DockerfileTest "" $dependencies
