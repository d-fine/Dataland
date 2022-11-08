#!/bin/bash
set -euxo pipefail

./build-utils/rebuild_single_docker_image.sh dataland_keycloak ./dataland-keycloak/Dockerfile \
         ./dataland-keycloak/dataland_theme/ ./dataland-keycloak/start_keycloak.sh ./dataland-keycloak/realms/ \
         ./build.gradle.kts ./gradle.properties ./settings.gradle.kts
