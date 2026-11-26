FROM nginx:1.31.9@sha256:84f7876d21d154885f99554d7ae325d8c2213f9169726ea7ab60642f2573a787

COPY ./dataland-inbound-admin-proxy/nginx.conf /etc/nginx/nginx.conf

HEALTHCHECK --interval=30s --timeout=5s --start-period=180s --start-interval=60s --retries=6 CMD curl -f http://localhost:6789/health/admin-proxy | tee /dev/stderr | grep -q 'UP' || false
