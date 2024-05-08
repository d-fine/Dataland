#!/usr/bin/env bash
set -euxo pipefail

dependencies="./dataland-automated-qa-service/ ./dataland-backend/backendOpenApi.json ./dataland-document-manager/documentManagerOpenApi.json"

./build-utils/base_rebuild_single_docker_image.sh dataland_automated_qa_service_base ./dataland-automated-qa-service/DockerfileBase $dependencies

set -o allexport
source ./*github_env.log
set +o allexport

./build-utils/base_rebuild_single_docker_image.sh dataland_automated_qa_service_test ./dataland-automated-qa-service/DockerfileTest $dependencies

./build-utils/base_rebuild_single_docker_image.sh dataland_automated_qa_service_production ./dataland-automated-qa-service/Dockerfile $dependencies
