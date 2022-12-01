#!/bin/bash
set -euxo pipefail

./build-utils/base_rebuild_single_docker_image.sh dataland_e2etests_base ./dataland-e2etests/DockerfileBase \
       ./gradle/ ./gradlew ./build.gradle.kts ./gradle.properties ./settings.gradle.kts

set -o allexport
source ./*github_env.log
set +o allexport

./build-utils/base_rebuild_single_docker_image.sh dataland_e2etests ./dataland-e2etests/Dockerfile \
       ./dataland-e2etests/ ./dataland-frontend/ ./testing/ \
       ./dataland-backend/backendOpenApi.json  ./dataland-api-key-manager/apiKeyManagerOpenApi.json \
       ./build.gradle.kts ./gradle.properties ./settings.gradle.kts
