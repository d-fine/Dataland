#!/usr/bin/env bash
set -euxo pipefail

gradle_dependencies=$(grep gradle_dependencies ./build-utils/common.conf | cut -d'=' -f2)
dependencies="./dataland-document-manager/ ./dataland-backend-utils/ ./dataland-keycloak-adapter/ $gradle_dependencies"
dependencies+=" ./dataland-message-queue-utils/ ./environments/.env.uncritical"

./build-utils/base_rebuild_single_docker_image.sh dataland_document_manager_base ./dataland-document-manager/DockerfileBase $dependencies

set -o allexport
source ./*github_env.log
set +o allexport

./build-utils/base_rebuild_single_docker_image.sh dataland_document_manager_production ./dataland-document-manager/Dockerfile $dependencies

./build-utils/base_rebuild_single_docker_image.sh dataland_document_manager_test ./dataland-document-manager/DockerfileTest $dependencies