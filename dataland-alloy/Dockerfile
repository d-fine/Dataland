# basis image from grafana aaloy
FROM grafana/alloy:v1.7.2@sha256:aad06b8d64f32bd39adb9a77beb203e18f3f1a3dd98e301876f8450a24aad135

COPY ./dataland-alloy/config /etc/alloy
RUN apt-get update && apt-get install -y curl

CMD ["run", "--server.http.listen-addr=alloy:8080", "--storage.path=/var/lib/alloy/data", "/etc/alloy/alloy-config.alloy"]

HEALTHCHECK --interval=30s --timeout=3s CMD curl --fail http://alloy:8080 || exit 1