# basis image from grafana aaloy
FROM grafana/alloy:v1.7.1@sha256:da938bc7dc6a96be31d3c93badc445aed115ca39357734de7a338f78fa342cf7

COPY ./dataland-alloy/config /etc/alloy
RUN apt-get update && apt-get install -y curl

CMD ["run", "--server.http.listen-addr=alloy:8080", "--storage.path=/var/lib/alloy/data", "/etc/alloy/alloy-config.alloy"]

HEALTHCHECK --interval=30s --timeout=3s CMD curl --fail http://alloy:8080 || exit 1