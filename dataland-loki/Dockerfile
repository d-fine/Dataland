# basis image from grafana loki
FROM grafana/loki:3.5.6@sha256:2b10e3c0ba66ee115c018736b351c435006c809d32ea2c14be62be88d80f8372

COPY ./dataland-loki/config /etc/loki

CMD ["-config.file=/etc/loki/loki-config.yaml"]

HEALTHCHECK --interval=30s --timeout=3s CMD wget -nv -O- -t 1 http://localhost:3100/ready | tee /dev/stderr | grep -q 'ready' || false
