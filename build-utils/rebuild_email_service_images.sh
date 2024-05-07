#!/usr/bin/env bash
set -euxo pipefail

gradle_dependencies=$(grep gradle_dependencies ./build-utils/common.conf | cut -d'=' -f2)
dependencies="./dataland-email-service/ ./dataland-backend-utils/ ./dataland-message-queue-utils $gradle_dependencies"

./build-utils/base_rebuild_single_docker_image.sh dataland_email_service_base ./dataland-email-service/DockerfileBase $dependencies

set -o allexport
source ./*github_env.log
set +o allexport

./build-utils/base_rebuild_single_docker_image.sh dataland_email_service_production ./dataland-email-service/Dockerfile $dependencies

./build-utils/base_rebuild_single_docker_image.sh dataland_email_service_test ./dataland-email-service/DockerfileTest $dependencies
