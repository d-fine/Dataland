#!/usr/bin/env bash
set -euxo pipefail

dependencies_v1="./dataland-postgres-v1/"
dependencies_v2="./dataland-postgres-v2/"

./build-utils/base_rebuild_single_docker_image.sh dataland_postgres_v1 ./dataland-postgres-v1/Dockerfile $dependencies_v1
./build-utils/base_rebuild_single_docker_image.sh dataland_postgres_v2 ./dataland-postgres-v2/Dockerfile $dependencies_v2
