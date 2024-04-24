#!/usr/bin/env bash
set -euxo pipefail

dependencies=" ./dataland-email-service/ ./dataland-backend-utils/ ./build.gradle.kts ./gradle.properties \
              ./settings.gradle.kts ./versions.properties ./dataland-message-queue-utils"

./build-utils/base_rebuild_single_docker_image.sh dataland_email_service_base ./dataland-email-service/DockerfileBase $dependencies

set -o allexport
source ./*github_env.log
set +o allexport

./build-utils/base_rebuild_single_docker_image.sh dataland_email_service_production ./dataland-email-service/Dockerfile  $dependencies

./build-utils/base_rebuild_single_docker_image.sh dataland_email_service_test ./dataland-email-service/DockerfileTest  $dependencies
