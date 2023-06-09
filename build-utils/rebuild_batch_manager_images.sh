#!/bin/bash
set -euxo pipefail

dependencies="./dataland-batch-manager/ ./dataland-backend/backendOpenApi.json ./build.gradle.kts ./gradle.properties ./settings.gradle.kts"

./build-utils/base_rebuild_single_docker_image.sh dataland_batch_manager_base ./dataland-batch-manager/DockerfileBase $dependencies

set -o allexport
source ./*github_env.log
set +o allexport

./build-utils/base_rebuild_single_docker_image.sh dataland_batch_manager ./dataland-batch-manager/Dockerfile $dependencies



