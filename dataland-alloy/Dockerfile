# basis image from grafana aaloy
FROM grafana/alloy:v1.6.1@sha256:25db7cff68ec18a4991bed5205589229f3a5b60168ce4db7313ad8e4a997adec

COPY ./dataland-alloy/config /etc/alloy
RUN apt-get update && apt-get install -y curl

CMD ["run", "--server.http.listen-addr=alloy:8080", "--storage.path=/var/lib/alloy/data", "/etc/alloy/alloy-config.alloy"]

HEALTHCHECK --interval=30s --timeout=3s CMD curl --fail http://alloy:8080 || exit 1