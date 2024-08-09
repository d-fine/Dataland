#!/bin/bash

set -euo pipefail

script_dir="$(dirname "$0")"
cd $script_dir
secret_files_dir="secret_files"

# Check if the secret file keystore.jks is present. For local development they need to be manually added there before
# starting the Dataland-stack.
if [ ! -f "$secret_files_dir/keystore.jks" ]; then
    echo "Error: The required file keystore.jks was not found in the expected location $secret_files_dir"
    exit 1
fi

# Sourcing the .env file if running on remote server
if [ -f "../.env" ]; then
    source ../.env
fi
export EURODAT_CLIENT_TLS_CERT="${EURODAT_CLIENT_TLS_CERT}"
export EURODAT_BASE_URL="${EURODAT_BASE_URL}"
export KEY_STORE_FILE_PASSWORD="${KEY_STORE_FILE_PASSWORD}"
export QUARKUS_HTTP_SSL_CERTIFICATE_KEY_STORE_PASSWORD="${QUARKUS_HTTP_SSL_CERTIFICATE_KEY_STORE_PASSWORD}"
export QUARKUS_OIDC_CLIENT_CREDENTIALS_JWT_KEY_PASSWORD="${QUARKUS_OIDC_CLIENT_CREDENTIALS_JWT_KEY_PASSWORD}"
export QUARKUS_OIDC_CLIENT_CREDENTIALS_JWT_KEY_STORE_PASSWORD="${QUARKUS_OIDC_CLIENT_CREDENTIALS_JWT_KEY_STORE_PASSWORD}"

# Write the missing two secret files.
envsubst < ./secret_files_templates/tls.crt.template > ./$secret_files_dir/tls.crt
envsubst < ./secret_files_templates/client.env.template > ./$secret_files_dir/client.env
