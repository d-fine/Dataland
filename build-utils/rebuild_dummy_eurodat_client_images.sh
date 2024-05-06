#!/usr/bin/env bash
set -euxo pipefail

dependencies=" ./dataland-dummy-eurodat-client/ ./dataland-eurodat-client/eurodatClientOpenApi.json ./build.gradle.kts ./gradle.properties \
              ./settings.gradle.kts ./versions.properties"

./build-utils/base_rebuild_single_docker_image.sh dataland_dummy_eurodat_client_base ./dataland-dummy-eurodat-client/DockerfileBase $dependencies

set -o allexport
source ./*github_env.log
set +o allexport

./build-utils/base_rebuild_single_docker_image.sh dataland_dummy_eurodat_client_test ./dataland-dummy-eurodat-client/DockerfileTest  $dependencies
