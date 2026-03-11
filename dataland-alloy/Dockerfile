# basis image from grafana aaloy
FROM grafana/alloy:v1.13.2@sha256:8abcd0d97a97099cabb34be8dcfc448fca6c35931ae9291aaf74056105dedcca

COPY ./dataland-alloy/config /etc/alloy
RUN apt-get update && apt-get install -y curl

CMD ["run", "--server.http.listen-addr=alloy:8080", "--storage.path=/var/lib/alloy/data", "/etc/alloy/alloy-config.alloy"]

HEALTHCHECK --interval=30s --timeout=3s CMD curl --fail http://alloy:8080 || exit 1