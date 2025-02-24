#!/bin/bash
set -euxo pipefail

# Replace $ENV in the template file with environment name; make sure /etc/grafana/provisioning has correct permissions
sed 's|"\$ENV"|'\"${ENVNAME}\"'|g' /etc/grafana/provisioning/alerting/alert-rules-template.yaml > /etc/grafana/provisioning/alerting/alert-rules.yaml

# Start Grafana
exec grafana server