FROM nginx:1.27.1@sha256:287ff321f9e3cde74b600cc26197424404157a72043226cbbf07ee8304a2c720

COPY ./dataland-inbound-admin-proxy/nginx.conf /etc/nginx/nginx.conf

HEALTHCHECK --interval=30s --timeout=3s CMD curl -f http://localhost:6789/health/admin-proxy | tee /dev/stderr | grep -q 'UP' || false
