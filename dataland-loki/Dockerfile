# basis image from grafana loki
FROM grafana/loki:3.3.3@sha256:835f65ab76c243dfd926e4014d8a48de066e6359699b7f63ffedd12d54a483ba

COPY ./dataland-loki/config /etc/loki

CMD ["-config.file=/etc/loki/loki-config.yaml"]

HEALTHCHECK --interval=30s --timeout=3s CMD wget -nv -O- -t 1 http://localhost:3100/ready | tee /dev/stderr | grep -q 'ready' || false
