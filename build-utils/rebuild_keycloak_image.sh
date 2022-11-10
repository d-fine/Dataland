#!/bin/bash
#adapted
set -euxo pipefail

./build-utils/base_rebuild_single_docker_image.sh dataland_keycloak ./dataland-keycloak/Dockerfile ./dataland-keycloak/ \
         ./build.gradle.kts ./gradle.properties ./settings.gradle.kts
