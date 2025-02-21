#!/usr/bin/env bash
set -euxo pipefail

dependencies="./dataland-postgres/"

./build-utils/base_rebuild_single_docker_image.sh dataland_postgres ./dataland-postgres-v1/Dockerfile $dependencies
./build-utils/base_rebuild_single_docker_image.sh dataland_postgres ./dataland-postgres-v2/Dockerfile $dependencies