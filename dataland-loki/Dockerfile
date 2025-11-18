# basis image from grafana loki
FROM grafana/loki:3.5.7@sha256:0eaee7bf39cc83aaef46914fb58f287d4f4c4be6ec96b86c2ed55719a75e49c8

COPY ./dataland-loki/config /etc/loki

CMD ["-config.file=/etc/loki/loki-config.yaml"]

HEALTHCHECK --interval=30s --timeout=3s CMD wget -nv -O- -t 1 http://localhost:3100/ready | tee /dev/stderr | grep -q 'ready' || false
