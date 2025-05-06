#!/bin/bash
set -euxo pipefail

# Compute the environment label from TARGETSERVER_URL
env_value=$(echo "${TARGETSERVER_URL}" | awk -F. '{print $1}')
env_label=$([ "$env_value" = "dataland" ] && echo "prod" || echo "$env_value")

# Replace $ENV in the template file; make sure /etc/grafana/provisioning has correct permissions
sed 's|"\$ENV"|'\"${env_label}\"'|g' /etc/grafana/provisioning/alerting/alert-rules-template.yaml > /etc/grafana/provisioning/alerting/alert-rules.yaml

# Start Grafana
exec grafana server