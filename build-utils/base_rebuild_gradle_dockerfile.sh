#!/usr/bin/env bash
set -euxo pipefail

gradle_dependencies=$(grep gradle_dependencies ./build-utils/common.conf | cut -d'=' -f2)
dependencies="./gradle/ ./gradlew $gradle_dependencies"

./build-utils/base_rebuild_single_docker_image.sh dataland_gradle_base ./base-dockerfiles/DockerfileGradle $dependencies
