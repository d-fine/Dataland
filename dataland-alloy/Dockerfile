# basis image from grafana aaloy
FROM grafana/alloy:v1.7.5@sha256:b92703c13c32c20d629ffd89f40287ed385c12a335a7c9f31c720a5f912f9d6f

COPY ./dataland-alloy/config /etc/alloy
RUN apt-get update && apt-get install -y curl

CMD ["run", "--server.http.listen-addr=alloy:8080", "--storage.path=/var/lib/alloy/data", "/etc/alloy/alloy-config.alloy"]

HEALTHCHECK --interval=30s --timeout=3s CMD curl --fail http://alloy:8080 || exit 1