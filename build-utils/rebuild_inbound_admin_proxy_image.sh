#!/usr/bin/env bash
set -euxo pipefail

dependencies="./dataland-inbound-admin-proxy/ ./versions.properties"

./build-utils/base_rebuild_single_docker_image.sh dataland_inbound_admin_proxy ./dataland-inbound-admin-proxy/Dockerfile $dependencies
