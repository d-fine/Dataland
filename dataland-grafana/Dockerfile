# basis image from grafana
FROM grafana/grafana:11.5.1@sha256:5781759b3d27734d4d548fcbaf60b1180dbf4290e708f01f292faa6ae764c5e6

COPY ./dataland-grafana/provisioning/datasources/loki.yaml /etc/grafana/provisioning/datasources/loki.yaml

CMD ["/run.sh"]

HEALTHCHECK --interval=30s --timeout=3s --start-period=30s CMD curl --fail http://localhost:3000/api/health || exit 1
