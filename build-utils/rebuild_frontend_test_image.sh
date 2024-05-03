#!/usr/bin/env bash
set -euxo pipefail

gradle_dependencies=$(grep gradle_dependencies ./build-utils/common.conf | cut -d'=' -f2)
dependencies="./dataland-frontend/ ./dataland-backend/backendOpenApi.json ./dataland-document-manager/documentManagerOpenApi.json ./dataland-api-key-manager/apiKeyManagerOpenApi.json"
dependencies+=" ./dataland-qa-service/qaServiceOpenApi.json ./dataland-community-manager/communityManagerOpenApi.json ./environments/.env.uncritical $gradle_dependencies"

./build-utils/base_rebuild_single_docker_image.sh dataland_frontend_test ./dataland-frontend/DockerfileTest $dependencies




