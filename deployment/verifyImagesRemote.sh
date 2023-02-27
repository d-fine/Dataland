#!/bin/bash
set -euxo pipefail

target_server_url="$TARGETSERVER_URL"
location=/home/ubuntu/dataland

scp ./deployment/verifyImages.sh ubuntu@"$target_server_url":"$location"
ssh ubuntu@"$target_server_url" "\"$location\"/verifyImages.sh"