#!/usr/bin/env bash
set -euxo pipefail

dependencies="./dataland-rabbitmq/ ./versions.properties"

./build-utils/base_rebuild_single_docker_image.sh dataland_rabbitmq ./dataland-rabbitmq/Dockerfile $dependencies
