# basis image from grafana aaloy
FROM grafana/alloy:v1.9.2@sha256:5039cc7a3cbde07120e757decc40f8f55f82c9bd0db45892a8b0e0c9a3371f4b

COPY ./dataland-alloy/config /etc/alloy
RUN apt-get update && apt-get install -y curl

CMD ["run", "--server.http.listen-addr=alloy:8080", "--storage.path=/var/lib/alloy/data", "/etc/alloy/alloy-config.alloy"]

HEALTHCHECK --interval=30s --timeout=3s CMD curl --fail http://alloy:8080 || exit 1