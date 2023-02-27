#!/bin/bash
set -euxo pipefail

target_server_url="$TARGETSERVER_URL"
ssh ubuntu@"$target_server_url" 'if [[ "$(docker ps | awk '\''{print $2}'\'' | grep -c _test:)" -eq 0 ]]; then exit 0; else exit 1; fi'