# Base image from Grafana
FROM grafana/grafana:11.5.2@sha256:8b5858c447e06fd7a89006b562ba7bba7c4d5813600c7982374c41852adefaeb

# Copy the pre-configured provisioning directory and entrypoint script into the image
COPY ./dataland-grafana/provisioning /etc/grafana/provisioning
COPY ./dataland-grafana/custom-entrypoint.sh /grafana-entrypoint.sh

# Use root to update permissions and then switch back
USER root
# Allow grafana user to write to certain directories/files
RUN chmod +x /grafana-entrypoint.sh
RUN chown -R grafana /etc/grafana/provisioning
USER grafana

ENTRYPOINT ["/grafana-entrypoint.sh"]

HEALTHCHECK --interval=30s --timeout=3s --start-period=30s CMD curl --fail http://localhost:3000/api/health || exit 1
