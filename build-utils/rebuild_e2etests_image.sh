#!/usr/bin/env bash
set -euxo pipefail

gradle_dependencies=$(grep gradle_dependencies ./build-utils/common.conf | cut -d'=' -f2)

./build-utils/base_rebuild_single_docker_image.sh dataland_e2etests_base ./dataland-e2etests/DockerfileBase \
       ./gradle/ ./gradlew ./environments/.env.uncritical $gradle_dependencies

set -o allexport
source ./*github_env.log
set +o allexport

./build-utils/base_rebuild_single_docker_image.sh dataland_e2etests ./dataland-e2etests/Dockerfile \
       ./dataland-e2etests/ ./dataland-frontend/ ./testing/ \
       ./dataland-backend/backendOpenApi.json  ./dataland-api-key-manager/apiKeyManagerOpenApi.json \
       ./dataland-document-manager/documentManagerOpenApi.json  ./dataland-qa-service/qaServiceOpenApi.json \
       ./dataland-community-manager/communityManagerOpenApi.json ./dataland-user-service/userServiceOpenApi.json \
       ./environments/.env.uncritical $gradle_dependencies
