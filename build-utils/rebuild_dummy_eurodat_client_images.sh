#!/usr/bin/env bash
set -euxo pipefail

if [[ "${LOCAL:-}" == "true" ]]; then
  echo "Skipping dummy eurodat client images build in LOCAL mode"
  exit 0
fi

gradle_dependencies=$(grep gradle_dependencies ./build-utils/common.conf | cut -d'=' -f2)
dependencies="./dataland-dummy-eurodat-client/ ./dataland-eurodat-client/eurodatClientOpenApi.json $gradle_dependencies"

./build-utils/base_rebuild_single_docker_image.sh dataland_dummy_eurodat_client_base ./dataland-dummy-eurodat-client/DockerfileBase "dataland-dummy-eurodat-client:assemble" $dependencies

set -o allexport
source ./*github_env.log
set +o allexport

./build-utils/base_rebuild_single_docker_image.sh dataland_dummy_eurodat_client_test ./dataland-dummy-eurodat-client/DockerfileTest  "" $dependencies
