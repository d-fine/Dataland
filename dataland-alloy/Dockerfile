# basis image from grafana aaloy
FROM grafana/alloy:v1.11.2@sha256:6ab34b8201f0e8b0c4346be4934c9965723af3f7f21dd9a65fd73f270f69b451

COPY ./dataland-alloy/config /etc/alloy
RUN apt-get update && apt-get install -y curl

CMD ["run", "--server.http.listen-addr=alloy:8080", "--storage.path=/var/lib/alloy/data", "/etc/alloy/alloy-config.alloy"]

HEALTHCHECK --interval=30s --timeout=3s CMD curl --fail http://alloy:8080 || exit 1