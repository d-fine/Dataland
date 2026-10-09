# basis image from grafana loki
FROM grafana/loki:3.6.17@sha256:cd5471d6ae439eb0a7a9055b9b2fb3fd3c5485934dd576473a6289d3936b8b27

COPY ./dataland-loki/config /etc/loki

CMD ["-config.file=/etc/loki/loki-config.yaml"]
