#!/usr/bin/env bash
set -euxo pipefail

# Dependencies required for building the image
dependencies="./dataland-grafana/ ./environments/.env.uncritical ./versions.properties"

# Set the TARGETSERVER_URL environment variable or default it
TARGETSERVER_URL=${TARGETSERVER_URL:-"default.dataland.com"}

# Extract the environment part from the PROXY_PRIMARY_URL
env_value=$(echo "$TARGETSERVER_URL" | awk -F. '{print $1}')
echo "Extracted environment: $env_value"

# Determine the appropriate env_label
if [ "$env_value" = "dataland" ]; then
  env_label="prod"
else
  env_label="$env_value"
fi
echo "Using env_label: $env_label"

# Path to the template and destination alert rules files
template_path="./dataland-grafana/provisioning/alerting/alert-rules-template.yaml"
output_path="./dataland-grafana/provisioning/alerting/alert-rules.yaml"

# Replace the placeholder in the template
sed "s|\$ENV|${env_label}|g" "$template_path" > "$output_path"

# Build the Docker image using the adjusted dependencies and Dockerfile
./build-utils/base_rebuild_single_docker_image.sh dataland_grafana ./dataland-grafana/Dockerfile $dependencies
