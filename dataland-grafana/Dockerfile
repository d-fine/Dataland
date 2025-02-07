# basis image from grafana
FROM grafana/grafana:11.4.1@sha256:413503f21b7e854811d74ef68ae1d3dce4c4814aef5daf4424c4a7ef6d42c31d

COPY ./dataland-grafana/provisioning/datasources/loki.yaml /etc/grafana/provisioning/datasources/loki.yaml

CMD ["/run.sh"]

HEALTHCHECK --interval=30s --timeout=3s --start-period=30s CMD curl --fail http://localhost:3000/api/health || exit 1
