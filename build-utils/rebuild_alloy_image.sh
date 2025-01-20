#!/usr/bin/env bash
set -euxo pipefail

dependencies="./dataland-alloy/ ./gitinfo ./environments/.env.uncritical ./versions.properties"

./build-utils/base_rebuild_single_docker_image.sh dataland_alloy ./dataland-alloy/Dockerfile $dependencies
