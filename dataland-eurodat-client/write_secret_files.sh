#!/bin/bash

# set pipefail ex etc. noch setzen TODO

#TODO hier code einführen um in das richtige dir zu gehen mit der $0 Variable

secret_files_dir="secret_files"

# Check if the two secret files keystore.jks and test.jks are already in the secret_files directory.
# They need to be manually added there before starting the Dataland-stack.
# They can be found in the Dataland internal repo on GitHub.
if [ ! -f "$secret_files_dir/keystore.jks" ] || [ ! -f "$secret_files_dir/test.jks" ]; then
    echo "Error: One or both of the files keystore.jks and test.jks not found in the $secret_files_dir directory"
    exit 1
fi

# Sourcing the .env file if running on remote server
if [ -f "../.env" ]; then
    source ../.env
fi


export EURODAT_CLIENT_TLS_CERT="$EURODAT_CLIENT_TLS_CERT"
export EURODAT_BASE_URL="$EURODAT_BASE_URL"
export KEY_STORE_FILE_PASSWORD="$KEY_STORE_FILE_PASSWORD"
export QUARKUS_HTTP_SSL_CERTIFICATE_KEY_STORE_PASSWORD="$QUARKUS_HTTP_SSL_CERTIFICATE_KEY_STORE_PASSWORD"
export QUARKUS_OIDC_CLIENT_CREDENTIALS_JWT_KEY_PASSWORD="$QUARKUS_OIDC_CLIENT_CREDENTIALS_JWT_KEY_PASSWORD"
export QUARKUS_OIDC_CLIENT_CREDENTIALS_JWT_KEY_STORE_PASSWORD="${QUARKUS_OIDC_CLIENT_CREDENTIALS_JWT_KEY_STORE_PASSWORD}"
export QUARKUS_OIDC_CLIENT_TLS_TRUST_STORE_PASSWORD="${QUARKUS_OIDC_CLIENT_TLS_TRUST_STORE_PASSWORD}"
export QUARKUS_REST_CLIENT_TRUST_STORE_PASSWORD="${QUARKUS_REST_CLIENT_TRUST_STORE_PASSWORD}"

# Write the missing two secret files.
envsubst < ./secret_files_templates/tls.crt.template > ./$secret_files_dir/tls.crt
envsubst < ./secret_files_templates/client.env.template > ./$secret_files_dir/client.env
