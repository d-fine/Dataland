#!/bin/bash
set -euxo pipefail

./build-utils/rebuild_single_docker_image.sh dataland_inbound_admin_proxy ./dataland-inbound-admin-proxy/Dockerfile \
        ./dataland-pgadmin/
