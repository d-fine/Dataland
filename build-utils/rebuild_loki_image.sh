#!/usr/bin/env bash
set -euxo pipefail

dependencies="./dataland-loki/ ./gitinfo ./environments/.env.uncritical ./versions.properties"

./build-utils/base_rebuild_single_docker_image.sh dataland_loki ./dataland-loki/Dockerfile $dependencies
