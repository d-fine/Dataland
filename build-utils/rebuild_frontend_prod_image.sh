#!/usr/bin/env bash
set -euxo pipefail

gradle_dependencies=$(grep gradle_dependencies ./build-utils/common.conf | cut -d'=' -f2)
frontend_dependencies=$(grep frontend_dependencies ./build-utils/common.conf | cut -d'=' -f2)
dependencies="$frontend_dependencies $gradle_dependencies"

./build-utils/base_rebuild_single_docker_image.sh dataland_frontend_production ./dataland-frontend/Dockerfile $dependencies
