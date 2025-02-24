# basis image from grafana
FROM grafana/grafana:11.5.2@sha256:8b37a2f028f164ce7b9889e1765b9d6ee23fec80f871d156fbf436d6198d32b7

COPY ./dataland-grafana/provisioning/datasources/loki.yaml /etc/grafana/provisioning/datasources/loki.yaml

CMD ["/run.sh"]

HEALTHCHECK --interval=30s --timeout=3s --start-period=30s CMD curl --fail http://localhost:3000/api/health || exit 1
