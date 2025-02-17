# Base image from Grafana
FROM grafana/grafana:11.5.1@sha256:5781759b3d27734d4d548fcbaf60b1180dbf4290e708f01f292faa6ae764c5e6

# Copy the pre-configured provisioning directory
COPY ./dataland-grafana/provisioning /etc/grafana/provisioning

# Define build argument
ARG TARGETSERVER_URL
ENV TARGETSERVER_URL=${TARGETSERVER_URL}

# Switch to root user to make changes
USER root

# Extract the environment value and set the label for alert rules
RUN env_value=$(echo "${TARGETSERVER_URL}" | awk -F. '{print $1}') && \
    env_label=$([ "$env_value" = "dataland" ] && echo "prod" || echo "$env_value") && \
    sed 's|"\$ENV"|'"${env_label}"'|g' /etc/grafana/provisioning/alerting/alert-rules-template.yaml > \
    /etc/grafana/provisioning/alerting/alert-rules.yaml

# Set user to grafana for security best practices
USER grafana

# Command to start Grafana
CMD ["/run.sh"]

# Healthcheck configuration
HEALTHCHECK --interval=30s --timeout=3s --start-period=30s CMD curl --fail http://localhost:3000/api/health || exit 1