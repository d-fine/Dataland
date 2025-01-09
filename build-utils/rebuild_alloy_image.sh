#!/usr/bin/env bash
set -euxo pipefail

echo "{\"commit\":\"$(git rev-parse HEAD)\",\"time\":\"$(git show -s --format=%aI HEAD)\",\"branch\":\"$(git rev-parse --abbrev-ref HEAD)\"}" > gitinfo
dependencies="./dataland-alloy/ ./gitinfo ./environments/.env.uncritical ./versions.properties"

./build-utils/base_rebuild_single_docker_image.sh dataland_alloy ./dataland-alloy/Dockerfile $dependencies
