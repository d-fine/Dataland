# basis image from grafana loki
FROM grafana/loki:3.2.2@sha256:030eee702c799da359c76e1fe305f03b522112ee34926b6c656495768fd5cb70

COPY ./dataland-loki/config /etc/loki

CMD ["-config.file=/etc/loki/loki-config.yaml"]

HEALTHCHECK --interval=30s --timeout=3s CMD wget -nv -O- -t 1 http://localhost:3100/ready | tee /dev/stderr | grep -q 'ready' || false
