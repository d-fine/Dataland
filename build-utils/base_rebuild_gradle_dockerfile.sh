#!/bin/bash
set -euxo pipefail

./build-utils/base_rebuild_single_docker_image.sh dataland_gradle_base ./base-dockerfiles/DockerfileGradle \
         ./gradle/ ./gradlew ./build.gradle.kts ./gradle.properties ./settings.gradle.kts
