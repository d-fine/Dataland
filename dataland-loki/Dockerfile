# basis image from grafana loki
FROM grafana/loki:3.5.1@sha256:a74594532eec4cc313401beedc4dd2708c43674c032084b1aeb87c14a5be1745

COPY ./dataland-loki/config /etc/loki

CMD ["-config.file=/etc/loki/loki-config.yaml"]

HEALTHCHECK --interval=30s --timeout=3s CMD wget -nv -O- -t 1 http://localhost:3100/ready | tee /dev/stderr | grep -q 'ready' || false
