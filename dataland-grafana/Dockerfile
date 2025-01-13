# basis image from grafana
FROM grafana/grafana:11.4.0@sha256:d8ea37798ccc41061a62ab080f2676dda6bf7815558499f901bdb0f533a456fb

COPY ./dataland-grafana/provisioning/datasources/loki.yaml /etc/grafana/provisioning/datasources/loki.yaml

#COPY ./dataland-grafana/config /etc/grafana
#RUN grafana-cli plugins install grafana-loki-datasource

CMD ["/run.sh"]

HEALTHCHECK --interval=30s --timeout=3s --start-period=30s CMD curl --fail http://localhost:3000/api/health || exit 1
