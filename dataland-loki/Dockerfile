# basis image from grafana loki
FROM grafana/loki:3.5.0@sha256:4c431d2e6b9b38718694b31c5d56be7c80dc69c513215fde1aeb5b02cd4e2665

COPY ./dataland-loki/config /etc/loki

CMD ["-config.file=/etc/loki/loki-config.yaml"]

HEALTHCHECK --interval=30s --timeout=3s CMD wget -nv -O- -t 1 http://localhost:3100/ready | tee /dev/stderr | grep -q 'ready' || false
