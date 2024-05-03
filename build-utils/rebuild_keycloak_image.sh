#!/usr/bin/env bash
set -euxo pipefail

gradle_dependencies=$(grep gradle_dependencies ./build-utils/common.conf | cut -d'=' -f2)
dependencies="./dataland-keycloak/ $gradle_dependencies"

./build-utils/base_rebuild_single_docker_image.sh dataland_keycloak ./dataland-keycloak/Dockerfile $dependencies
