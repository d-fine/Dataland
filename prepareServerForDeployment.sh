#!/bin/bash

set -euxo pipefail

targetserver_url=$1

ssh ubuntu@"$targetserver_url" 'mkdir -p ./dataland/dataland-keycloak'
scp ./deployment/{migrate_keycloak_users,deployment_utils,docker_utils}.sh ubuntu@"$targetserver_url":dataland/dataland-keycloak
