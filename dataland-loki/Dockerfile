# basis image from grafana loki
FROM grafana/loki:3.6.5@sha256:847c287ada0e12603910589f42038c5cdaaad04e248bd1dc6c6e0920a235f427

COPY ./dataland-loki/config /etc/loki

CMD ["-config.file=/etc/loki/loki-config.yaml"]
