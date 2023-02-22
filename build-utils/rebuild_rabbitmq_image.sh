#!/bin/bash
set -euxo pipefail

./build-utils/base_rebuild_single_docker_image.sh dataland_rabbitmq ./dataland-rabbitmq/Dockerfile ./dataland-rabbitmq/
