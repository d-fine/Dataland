#!/bin/bash
set -euxo pipefail

./build-utils/rebuild_single_docker_image.sh dataland_e2etests ./dataland-e2etests/Dockerfile \
       ./dataland-backend/ ./dataland-e2etests/ ./dataland-frontend/tests/ \
       ./build.gradle.kts ./gradle.properties ./settings.gradle.kts
