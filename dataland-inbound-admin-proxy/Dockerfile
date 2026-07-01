FROM nginx:1.29.8@sha256:1881968aff6f7cdcc4b888c00a11f4ce241ad7ec957e0cb4a9e19e93a3ff87ea

COPY ./dataland-inbound-admin-proxy/nginx.conf /etc/nginx/nginx.conf

HEALTHCHECK --interval=30s --timeout=3s CMD curl -f http://localhost:6789/health/admin-proxy | tee /dev/stderr | grep -q 'UP' || false
