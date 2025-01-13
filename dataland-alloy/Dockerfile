# basis image from grafana aaloy
FROM grafana/alloy:v1.5.1@sha256:01a63f4e032ce54ee94b22049bc27f597e74f85566478c377f4b5c7f020c1eb3

COPY ./dataland-alloy/config /etc/alloy

CMD ["run", "--server.http.listen-addr=alloy:8080", "--storage.path=/var/lib/alloy/data", "/etc/alloy/alloy-config.alloy"]

HEALTHCHECK --interval=30s --timeout=3s CMD wget -nv -O- -t 1 http://localhost:8080/alloy/ready | tee /dev/stderr | grep -q 'ready' || false