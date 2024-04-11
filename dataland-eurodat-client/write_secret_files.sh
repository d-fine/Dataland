#!/bin/bash

secret_files_dir="secret_files"

# Check if the two secret files keystore.jks and test.jks are already in the secret_files directory.
# They need to be manually added there before starting the Dataland-stack.
# They can be found in the Dataland internal repo on GitHub.
if [ ! -f "$secret_files_dir/keystore.jks" ] || [ ! -f "$secret_files_dir/test.jks" ]; then
    echo "Error: One or both of the files keystore.jks and test.jks not found in the $secret_files_dir directory"
    exit 1
fi

# Write the missing two secret files.
envsubst < ./secret_files_templates/tls.crt.template > ./$secret_files_dir/tls.crt
envsubst < ./secret_files_templates/client.env.template > ./$secret_files_dir/client.env
