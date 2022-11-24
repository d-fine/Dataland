#!/bin/bash
set -euxo pipefail

./build-utils/base_rebuild_single_docker_image.sh dataland_e2etests_core ./dataland-e2etests/e2etestsCoreDockerfile \
       ./gradle/ ./gradlew ./build.gradle.kts ./gradle.properties ./settings.gradle.kts

set -o allexport
source ./*github_env.log
set +o allexport

./build-utils/base_rebuild_single_docker_image.sh dataland_e2etests ./dataland-e2etests/Dockerfile \
       ./dataland-backend/ ./dataland-backend-utils ./dataland-api-key-manager \
       ./dataland-keycloak-adapter/ ./dataland-e2etests/ ./dataland-frontend/ ./testing/ \
       ./build.gradle.kts ./gradle.properties ./settings.gradle.kts
