#!/usr/bin/env bash
set -euxo pipefail

dependencies="./dataland-pgadmin/ ./versions.properties"

./build-utils/base_rebuild_single_docker_image.sh dataland_pgadmin ./dataland-pgadmin/Dockerfile $dependencies
