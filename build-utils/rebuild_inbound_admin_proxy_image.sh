#!/bin/bash
# adapted
set -euxo pipefail

./build-utils/base_rebuild_single_docker_image.sh dataland_inbound_admin_proxy ./dataland-inbound-admin-proxy/Dockerfile \
        ./dataland-inbound-admin-proxy/
