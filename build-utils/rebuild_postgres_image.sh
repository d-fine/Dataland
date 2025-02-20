#!/usr/bin/env bash
set -euxo pipefail

dependencies="./dataland-postgres/ ./environments/.env.uncritical ./versions.properties"

./build-utils/base_rebuild_single_docker_image.sh dataland_postgres ./dataland-postgres/Dockerfile $dependencies
