#!/usr/bin/env bash
set -euxo pipefail

./build-utils/base_rebuild_single_docker_image.sh dataland_specification_service ./dataland-specification-service/Dockerfile
