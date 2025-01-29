# basis image from grafana aaloy
FROM grafana/alloy:v1.6.0@sha256:647b223519857dccb1961e3d5dff3566fa9d75b7fd31dcd1f1575370bee8aea8

COPY ./dataland-alloy/config /etc/alloy
RUN apt-get update && apt-get install -y curl

## Copy the health check script and service file into the image
#COPY ./healthCheck.sh /usr/local/bin/healthCheck.sh
## Make the script executable
#RUN chmod +x /usr/local/bin/healthCheck.sh
#HEALTHCHECK --interval=30s --timeout=10s --retries=3 CMD /usr/local/bin/healthCheck.sh

CMD ["run", "--server.http.listen-addr=alloy:8080", "--storage.path=/var/lib/alloy/data", "/etc/alloy/alloy-config.alloy"]

HEALTHCHECK --interval=30s --timeout=3s CMD curl --fail http://alloy:8080 || exit 1