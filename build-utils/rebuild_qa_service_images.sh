#!/usr/bin/env bash
set -euxo pipefail

gradle_dependencies=$(grep gradle_dependencies ./build-utils/common.conf | cut -d'=' -f2)
dependencies="./dataland-qa-service/ ./dataland-backend-utils/ ./dataland-message-queue-utils/ $gradle_dependencies"
dependencies+=" ./dataland-api-key-manager/ ./dataland-keycloak-adapter/"

./build-utils/base_rebuild_single_docker_image.sh dataland_qa_service_base ./dataland-qa-service/DockerfileBase $dependencies

set -o allexport
source ./*github_env.log
set +o allexport

./build-utils/base_rebuild_single_docker_image.sh dataland_qa_service_production ./dataland-qa-service/Dockerfile $dependencies

./build-utils/base_rebuild_single_docker_image.sh dataland_qa_service_test ./dataland-qa-service/DockerfileTest $dependencies
