#!/bin/bash

target_directory="secret_files"
mkdir -p "$target_directory"

envsubst < ./secret_files_templates/tls.crt.template > ./secret_files/tls.crt
#envsubst < ./secret_files_templates/client.env.template > ./secret_files/client.env
