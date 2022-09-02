#!/bin/bash
set -ux

source ./deployment/deployment_utils.sh

setup_ssh

sudo echo "127.0.0.1 dataland-admin" | sudo tee -a /etc/hosts
ssh -L 6789:localhost:6789 -N -f ubuntu@"$TARGETSERVER_URL"
wait_for_health "https://localhost:6789/health" "Tunneled Admin Server"