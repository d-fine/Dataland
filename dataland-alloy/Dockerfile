# basis image from grafana aaloy
FROM grafana/alloy:v1.8.3@sha256:ab04df3936e4d71d31b6f55e0c58a7e749091f59635dd8c2bc731ba1b6c93701

COPY ./dataland-alloy/config /etc/alloy
RUN apt-get update && apt-get install -y curl

CMD ["run", "--server.http.listen-addr=alloy:8080", "--storage.path=/var/lib/alloy/data", "/etc/alloy/alloy-config.alloy"]

HEALTHCHECK --interval=30s --timeout=3s CMD curl --fail http://alloy:8080 || exit 1