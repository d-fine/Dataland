# basis image from grafana aaloy
FROM grafana/alloy:v1.7.4@sha256:fd2d274beeed926ffe62cf353df12a69d37af61e9a27eb4dc897a127755e4d95

COPY ./dataland-alloy/config /etc/alloy
RUN apt-get update && apt-get install -y curl

CMD ["run", "--server.http.listen-addr=alloy:8080", "--storage.path=/var/lib/alloy/data", "/etc/alloy/alloy-config.alloy"]

HEALTHCHECK --interval=30s --timeout=3s CMD curl --fail http://alloy:8080 || exit 1