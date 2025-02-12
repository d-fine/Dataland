# basis image from grafana loki
FROM grafana/loki:3.3.2@sha256:8af2de1abbdd7aa92b27c9bcc96f0f4140c9096b507c77921ffddf1c6ad6c48f

COPY ./dataland-loki/config /etc/loki

CMD ["-config.file=/etc/loki/loki-config.yaml"]

HEALTHCHECK --interval=30s --timeout=3s CMD wget -nv -O- -t 1 http://localhost:3100/ready | tee /dev/stderr | grep -q 'ready' || false
