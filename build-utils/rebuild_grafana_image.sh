#!/usr/bin/env bash
set -euxo pipefail

# Dependencies required for building the image
dependencies="./dataland-grafana/ ./environments/.env.uncritical ./versions.properties"

# Build the Docker image using the adjusted dependencies and Dockerfile
./build-utils/base_rebuild_single_docker_image.sh dataland_grafana ./dataland-grafana/Dockerfile $dependencies
