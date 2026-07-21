# basis image from grafana lokia
FROM grafana/loki:3.6.11@sha256:44148ad243c07957113baea950ee5ba9b0c8eff8d7e38ff3cc1c0af705790945

USER ubuntu

COPY ./dataland-loki/config /etc/loki

CMD ["-config.file=/etc/loki/loki-config.yaml"]
