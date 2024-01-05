#!/usr/bin/env bash
set -euxo pipefail

./build-utils/base_rebuild_single_docker_image.sh dataland_automated_qa_service_base ./dataland-automated-qa-service/DockerfileBase ./dataland-automated-qa-service/

set -o allexport
source ./*github_env.log
set +o allexport

./build-utils/base_rebuild_single_docker_image.sh dataland_automated_qa_service ./dataland-automated-qa-service/Dockerfile ./dataland-automated-qa-service/
