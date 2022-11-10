#!/bin/bash
#adapted
set -euxo pipefail

./build-utils/base_rebuild_single_docker_image.sh dataland_proxy_base \
        ./dataland-inbound-proxy/DockerfileBase ./dataland-inbound-proxy/ ./dataland-frontend/src/assets/images/
set -o allexport
source ./*github_env.log
set +o allexport
echo "{\"commit\":\"$(git rev-parse HEAD)\",\"time\":\"$(git show -s --format=%aI HEAD)\",\"branch\":\"$(git rev-parse --abbrev-ref HEAD)\"}" > gitinfo
PROXY_ENVIRONMENT="production" ./build-utils/base_rebuild_single_docker_image.sh dataland_inbound_proxy_production \
        ./dataland-inbound-proxy/Dockerfile ./dataland-inbound-proxy/ ./dataland-frontend/src/assets/images/ ./gitinfo
PROXY_ENVIRONMENT="development" ./build-utils/base_rebuild_single_docker_image.sh dataland_inbound_proxy_development \
        ./dataland-inbound-proxy/Dockerfile ./dataland-inbound-proxy/ ./dataland-frontend/src/assets/images/ ./gitinfo
