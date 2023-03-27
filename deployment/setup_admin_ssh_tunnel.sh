#!/bin/bash
set -eux

source ./deployment/deployment_utils.sh

setup_ssh

sudo echo "127.0.0.1 dataland-admin" | sudo tee -a /etc/hosts
ssh -L 6789:localhost:6789 -L 5433:localhost:5433 -L 5434:localhost:5434 -L 5435:localhost:5435 -L 5436:localhost:5436 -L 5437:localhost:5437 -N -f ubuntu@"$TARGETSERVER_URL"

wait_for_health "http://dataland-admin:6789/health/admin-proxy" "Tunneled Admin Server"

pg_isready -d backend -h "localhost" -p 5433
pg_isready -d keycloak -h "localhost" -p 5434
pg_isready -d api_key_manager -h "localhost" -p 5435
pg_isready -d internal_storage -h "localhost" -p 5436
pg_isready -d document_manager -h "localhost" -p 5437
