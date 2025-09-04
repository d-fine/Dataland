# basis image from grafana aaloy
FROM grafana/alloy:v1.10.2@sha256:bcf27f18c4402869af112fb39e35e1db3804a404686f4caa20bdf77814219223

COPY ./dataland-alloy/config /etc/alloy
RUN apt-get update && apt-get install -y curl

CMD ["run", "--server.http.listen-addr=alloy:8080", "--storage.path=/var/lib/alloy/data", "/etc/alloy/alloy-config.alloy"]

HEALTHCHECK --interval=30s --timeout=3s CMD curl --fail http://alloy:8080 || exit 1