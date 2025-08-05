# basis image from grafana aaloy
FROM grafana/alloy:v1.10.0@sha256:1bc130e909116a4bf950eb5187a346007d3b796f96832b529acb14a64e41230f

COPY ./dataland-alloy/config /etc/alloy
RUN apt-get update && apt-get install -y curl

CMD ["run", "--server.http.listen-addr=alloy:8080", "--storage.path=/var/lib/alloy/data", "/etc/alloy/alloy-config.alloy"]

HEALTHCHECK --interval=30s --timeout=3s CMD curl --fail http://alloy:8080 || exit 1