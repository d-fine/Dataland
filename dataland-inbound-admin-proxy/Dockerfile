FROM nginx:1.25.5@sha256:a484819eb60211f5299034ac80f6a681b06f89e65866ce91f356ed7c72af059c

COPY ./dataland-inbound-admin-proxy/nginx.conf /etc/nginx/nginx.conf

HEALTHCHECK --interval=30s --timeout=3s CMD curl -f http://localhost:6789/health/admin-proxy | tee /dev/stderr | grep -q 'UP' || false
