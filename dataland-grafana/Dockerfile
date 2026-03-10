# Base image from Grafana
FROM grafana/grafana:12.3.4@sha256:c9d279370af2395775fc02b804e4d4aec7ca40e3211b00a4ff547189e2cf890e

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
