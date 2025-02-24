#!/bin/bash
set -euxo pipefail

# Get hostname as environment label for alert template
env_label=$(echo "${ENVNAME}" | awk -F. '{print $1}')

# Replace $ENV in the template file; make sure /etc/grafana/provisioning has correct permissions
sed 's|"\$ENV"|'\"${env_label}\"'|g' /etc/grafana/provisioning/alerting/alert-rules-template.yaml > /etc/grafana/provisioning/alerting/alert-rules.yaml

# Start Grafana
exec grafana server