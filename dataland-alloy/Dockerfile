# basis image from grafana aaloy
FROM grafana/alloy:v1.9.1@sha256:b5fc87ff9a8941d6ed3ae5f099d9cb8598b3cd42fef9a8af128ed782258b4017

COPY ./dataland-alloy/config /etc/alloy
RUN apt-get update && apt-get install -y curl

CMD ["run", "--server.http.listen-addr=alloy:8080", "--storage.path=/var/lib/alloy/data", "/etc/alloy/alloy-config.alloy"]

HEALTHCHECK --interval=30s --timeout=3s CMD curl --fail http://alloy:8080 || exit 1