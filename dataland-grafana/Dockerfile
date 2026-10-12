# Base image from Grafana
FROM grafana/grafana:12.4.12@sha256:83be3e511ede559bee80e2215bb1987cb1246075461cb961beb515b0341e7aea

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

HEALTHCHECK --interval=30s --timeout=5s --start-period=180s --start-interval=60s --retries=6 CMD curl --fail http://localhost:3000/api/health || exit 1
