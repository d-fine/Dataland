#!/usr/bin/env bash
set -euxo pipefail

gradle_dependencies=$(grep gradle_dependencies ./build-utils/common.conf | cut -d'=' -f2)
dependencies="./dataland-backend/ ./dataland-backend-utils/ ./dataland-keycloak-adapter/ ./dataland-message-queue-utils/ $gradle_dependencies"
dependencies+=" ./dataland-api-key-manager/apiKeyManagerOpenApi.json ./dataland-community-manager/communityManagerOpenApi.json"
dependencies+=" ./dataland-internal-storage/internalStorageOpenApi.json ./environments/.env.uncritical"
dependencies+=" ./dataland-specification-service/specificationServiceOpenApi.json ./dataland-external-storage/externalStorageOpenApi.json"
dependencies+=" ./dataland-document-manager/documentManagerOpenApi.json"

./build-utils/base_rebuild_single_docker_image.sh dataland_backend_base ./dataland-backend/DockerfileBase $dependencies

set -o allexport
source ./*github_env.log
set +o allexport

./build-utils/base_rebuild_single_docker_image.sh dataland_backend_production ./dataland-backend/Dockerfile $dependencies

./build-utils/base_rebuild_single_docker_image.sh dataland_backend_test ./dataland-backend/DockerfileTest $dependencies
