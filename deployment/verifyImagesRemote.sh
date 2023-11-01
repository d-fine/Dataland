#!/usr/bin/env bash
set -euxo pipefail

target_server_url="$TARGETSERVER_URL"
ssh ubuntu@"$target_server_url" 'failed_containers=$(docker ps --format '\''{{.Names}}'\'' | grep _test); if [[ -z "$failed_containers" ]]; then exit 0; else echo "$failed_containers"; exit 1; fi'

# TODO changes this script for debugging in CD