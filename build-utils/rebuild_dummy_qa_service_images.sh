#!/bin/bash
set -euxo pipefail

./build-utils/base_rebuild_single_docker_image.sh dataland_dummy_qa_service_base ./dataland-dummy-qa-service/DockerfileBase \
         ./dataland-dummy-qa-service/ ./build.gradle.kts ./gradle.properties ./settings.gradle.kts

set -o allexport
source ./*github_env.log
set +o allexport

./build-utils/base_rebuild_single_docker_image.sh dataland_dummy_qa_service_production ./dataland-dummy-qa-service/Dockerfile ./dataland-dummy-qa-service/ \
          ./build.gradle.kts ./gradle.properties ./settings.gradle.kts

./build-utils/base_rebuild_single_docker_image.sh dataland_dummy_qa_service_test ./dataland-dummy-qa-service/DockerfileTest ./dataland-dummy-qa-service/ \
          ./build.gradle.kts ./gradle.properties ./settings.gradle.kts
