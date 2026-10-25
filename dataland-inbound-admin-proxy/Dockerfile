FROM nginx:1.31.6@sha256:60f0d4e986561e08804f75c211dcaf7fc5455699eada29f42cbe8a0a7253dd86

COPY ./dataland-inbound-admin-proxy/nginx.conf /etc/nginx/nginx.conf

HEALTHCHECK --interval=30s --timeout=5s --start-period=180s --start-interval=60s --retries=6 CMD curl -f http://localhost:6789/health/admin-proxy | tee /dev/stderr | grep -q 'UP' || false
