# basis image from grafana loki
FROM grafana/loki:3.5.5@sha256:31628519045e7f28692a7ae73b4a3fd293dccb425585ed5d16ceea3b5c9592e6

COPY ./dataland-loki/config /etc/loki

CMD ["-config.file=/etc/loki/loki-config.yaml"]

HEALTHCHECK --interval=30s --timeout=3s CMD wget -nv -O- -t 1 http://localhost:3100/ready | tee /dev/stderr | grep -q 'ready' || false
