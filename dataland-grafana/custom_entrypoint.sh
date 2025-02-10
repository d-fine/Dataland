#!/bin/sh

envsubst < /etc/grafana/provisioning/alerting/contact_points.template.yaml > /etc/grafana/provisioning/alerting/contact_points.yaml

exec /run.sh