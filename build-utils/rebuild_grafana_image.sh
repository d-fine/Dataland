#!/usr/bin/env bash
set -euxo pipefail

dependencies="./dataland-grafana/ ./environments/.env.uncritical ./versions.properties"

./build-utils/base_rebuild_single_docker_image.sh dataland_grafana ./dataland-grafana/Dockerfile $dependencies
