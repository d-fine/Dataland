# basis image from grafana loki
FROM grafana/loki:3.2.2@sha256:030eee702c799da359c76e1fe305f03b522112ee34926b6c656495768fd5cb70

## Install certbot and cron and delete default nginx config
#RUN apt-get update && apt-get install -y certbot cron && rm -rf /etc/nginx/* && mkdir -p /etc/nginx/conf.d && apt-get clean && rm -rf /var/lib/apt/lists/*

COPY ./dataland-loki/config /etc/loki

CMD ["-config.file=/etc/loki/loki-config.yaml"]

#HEALTHCHECK --interval=30s --timeout=3s CMD curl -f http://localhost:3100/ready | tee /dev/stderr | grep -q 'ready' || false
