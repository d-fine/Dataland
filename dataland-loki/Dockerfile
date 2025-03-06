# basis image from grafana loki
FROM grafana/loki:3.4.1@sha256:1d0c5ddc7644b88956aa0bd775ad796d9635180258a225d6ab3552751d5e2a66

COPY ./dataland-loki/config /etc/loki

CMD ["-config.file=/etc/loki/loki-config.yaml"]

HEALTHCHECK --interval=30s --timeout=3s CMD wget -nv -O- -t 1 http://localhost:3100/ready | tee /dev/stderr | grep -q 'ready' || false
