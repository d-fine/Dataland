#!/bin/bash

targetserver_url=$1

ssh ubuntu@"$targetserver_url" 'mkdir -p ./dataland/dataland-keycloak'
scp ./deployment/{migrate_keycloak_users,deployment_utils}.sh ubuntu@"$targetserver_url":dataland/dataland-keycloak