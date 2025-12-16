# basis image from grafana loki
FROM grafana/loki:3.6.2@sha256:fad94db64eeb0e14544953789c942453df68a860b6f760d867f06c5b31f461ab

COPY ./dataland-loki/config /etc/loki

CMD ["-config.file=/etc/loki/loki-config.yaml"]
