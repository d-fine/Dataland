#!/usr/bin/env bash
set -euxo pipefail

dependencies="./dataland-postgres/"

./build-utils/base_rebuild_single_docker_image.sh dataland_postgres ./dataland-postgres-version-one/Dockerfile $dependencies
./build-utils/base_rebuild_single_docker_image.sh dataland_postgres ./dataland-postgres-version-two/Dockerfile $dependencies