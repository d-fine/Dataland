# basis image from grafana aaloy
FROM grafana/alloy:v1.12.2@sha256:f94b1c82957accb77cd11385fd5e3600758ef1b1ab79557c97319ad28590735a

COPY ./dataland-alloy/config /etc/alloy
RUN apt-get update && apt-get install -y curl

CMD ["run", "--server.http.listen-addr=alloy:8080", "--storage.path=/var/lib/alloy/data", "/etc/alloy/alloy-config.alloy"]

HEALTHCHECK --interval=30s --timeout=3s CMD curl --fail http://alloy:8080 || exit 1