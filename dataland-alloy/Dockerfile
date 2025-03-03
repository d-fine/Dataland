# basis image from grafana aaloy
FROM grafana/alloy:v1.7.0@sha256:afcc9d32975dd567b2e7c87f0c2f0e4ebf9385ecc36e1c0d44ba42de6dcf291c

COPY ./dataland-alloy/config /etc/alloy
RUN apt-get update && apt-get install -y curl

CMD ["run", "--server.http.listen-addr=alloy:8080", "--storage.path=/var/lib/alloy/data", "/etc/alloy/alloy-config.alloy"]

HEALTHCHECK --interval=30s --timeout=3s CMD curl --fail http://alloy:8080 || exit 1