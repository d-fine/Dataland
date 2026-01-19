# basis image from grafana aaloy
FROM grafana/alloy:v1.12.1@sha256:c311595156fca7b1131683d383dae1acfd29e8879d347161d2a6f714ebac0c0b

COPY ./dataland-alloy/config /etc/alloy
RUN apt-get update && apt-get install -y curl

CMD ["run", "--server.http.listen-addr=alloy:8080", "--storage.path=/var/lib/alloy/data", "/etc/alloy/alloy-config.alloy"]

HEALTHCHECK --interval=30s --timeout=3s CMD curl --fail http://alloy:8080 || exit 1