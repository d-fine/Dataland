# basis image from grafana loki
FROM grafana/loki:3.6.4@sha256:be31579ac047e9f78b81e48f3b69d3af709e7299b431d5aa78bcda43382f9511

COPY ./dataland-loki/config /etc/loki

CMD ["-config.file=/etc/loki/loki-config.yaml"]
