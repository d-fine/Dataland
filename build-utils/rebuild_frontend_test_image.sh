#!/bin/bash
set -euxo pipefail

./build-utils/base_rebuild_single_docker_image.sh dataland_frontend_test ./dataland-frontend/DockerfileTest \
         ./dataland-frontend/ ./dataland-backend/backendOpenApi.json \
         ./build.gradle.kts ./gradle.properties ./settings.gradle.kts
