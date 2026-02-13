#!/usr/bin/env bash
set -euxo pipefail

gradle_dependencies=$(grep gradle_dependencies ./build-utils/common.conf | cut -d'=' -f2)
dependencies="./dataland-email-service/ ./dataland-backend-utils/ ./dataland-message-queue-utils $gradle_dependencies"

./build-utils/base_rebuild_single_docker_image.sh dataland_email_service_base ./dataland-email-service/DockerfileBase "dataland-email-service:assemble --rerun-tasks" $dependencies

set -o allexport
source ./*github_env.log
set +o allexport

if [[ "${LOCAL:-}" != "true" ]]; then
  ./build-utils/base_rebuild_single_docker_image.sh dataland_email_service_production ./dataland-email-service/Dockerfile "" $dependencies
fi

./build-utils/base_rebuild_single_docker_image.sh dataland_email_service_test ./dataland-email-service/DockerfileTest "" $dependencies
