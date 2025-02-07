# basis image from grafana
FROM grafana/grafana:11.5.0@sha256:0a2874cf39c6487093c682215f7c7903ed8646d78ae5f911af945d2dfcc0a447

COPY ./dataland-grafana/provisioning/datasources/loki.yaml /etc/grafana/provisioning/datasources/loki.yaml

CMD ["/run.sh"]

HEALTHCHECK --interval=30s --timeout=3s --start-period=30s CMD curl --fail http://localhost:3000/api/health || exit 1
