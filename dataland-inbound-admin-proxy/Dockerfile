FROM nginx:1.29.4@sha256:ca871a86d45a3ec6864dc45f014b11fe626145569ef0e74deaffc95a3b15b430

COPY ./dataland-inbound-admin-proxy/nginx.conf /etc/nginx/nginx.conf

HEALTHCHECK --interval=30s --timeout=3s CMD curl -f http://localhost:6789/health/admin-proxy | tee /dev/stderr | grep -q 'UP' || false
