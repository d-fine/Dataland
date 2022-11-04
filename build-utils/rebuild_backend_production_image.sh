#!/bin/bash
set -euxo pipefail

./build-utils/rebuild_single_docker_image.sh dataland_backend_production ./dataland-backend/Dockerfile ./dataland-backend/ \
         ./build.gradle.kts ./gradle.properties ./settings.gradle.kts
