# basis image from grafana aaloy
FROM grafana/alloy:v1.6.0@sha256:09958ec77e05bcf5305d956095c32cdb8ac06861a599aca6b281d190a0aa5a71

COPY ./dataland-alloy/config /etc/alloy
RUN apt-get update && apt-get install -y curl

CMD ["run", "--server.http.listen-addr=alloy:8080", "--storage.path=/var/lib/alloy/data", "/etc/alloy/alloy-config.alloy"]

HEALTHCHECK --interval=30s --timeout=3s CMD curl --fail http://alloy:8080 || exit 1