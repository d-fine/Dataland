# basis image from grafana aaloy
FROM grafana/alloy:v1.14.0@sha256:f50931848bd8178774521767bb46b905e1a081301950ff28d7623c9db7c01076

COPY ./dataland-alloy/config /etc/alloy
RUN apt-get update && apt-get install -y curl

CMD ["run", "--server.http.listen-addr=alloy:8080", "--storage.path=/var/lib/alloy/data", "/etc/alloy/alloy-config.alloy"]

HEALTHCHECK --interval=30s --timeout=3s CMD curl --fail http://alloy:8080 || exit 1