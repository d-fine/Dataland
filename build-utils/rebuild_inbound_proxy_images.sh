#!/bin/bash
set -euxo pipefail

echo "{\"commit\":\"$(git rev-parse HEAD)\",\"time\":\"$(git show -s --format=%aI HEAD)\",\"branch\":\"$(git rev-parse --abbrev-ref HEAD)\"}" > gitinfo

./build-utils/base_rebuild_single_docker_image.sh dataland_proxy_base \
        ./dataland-inbound-proxy/Dockerfile ./dataland-inbound-proxy/ ./dataland-frontend/src/assets/images/ ./gitinfo
