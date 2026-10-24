# basis image from grafana loki
FROM grafana/loki:3.6.18@sha256:700eaaa9fbd4275914d6b11f77c080b5ba0b983f6690055e48264ea77cb0bf6f

COPY ./dataland-loki/config /etc/loki

CMD ["-config.file=/etc/loki/loki-config.yaml"]
