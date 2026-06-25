# basis image from grafana aaloy
FROM grafana/alloy:v1.14.2@sha256:eadfe35ea52b26cbec4d4d780fbcc31edb31108c1c9e537ca59557a5a102c712

COPY ./dataland-alloy/config /etc/alloy
RUN apt-get update && apt-get install -y curl

CMD ["run", "--server.http.listen-addr=alloy:8080", "--storage.path=/var/lib/alloy/data", "/etc/alloy/alloy-config.alloy"]

HEALTHCHECK --interval=30s --timeout=3s CMD curl --fail http://alloy:8080 || exit 1