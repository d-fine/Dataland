#!/bin/bash
set -ux

source ./deployment/deployment_utils.sh

setup_ssh

ssh -L 6789:localhost:6789 ubuntu@"$TARGETSERVER_URL" &
wait_for_health "https://localhost:6789/health" "Tunneled Admin Server"