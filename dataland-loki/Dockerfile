# basis image from grafana loki
FROM grafana/loki:3.6.3@sha256:cd6e176883a90c21755f0315688668991634143423f75bdedfef41441b0fdc3c

COPY ./dataland-loki/config /etc/loki

CMD ["-config.file=/etc/loki/loki-config.yaml"]
