#!/usr/bin/env bash
set -euxo pipefail

./build-utils/base_rebuild_single_docker_image.sh dataland_automated_qa_service ./dataland-automated-qa-service/Dockerfile ./dataland-automated-qa-service/
