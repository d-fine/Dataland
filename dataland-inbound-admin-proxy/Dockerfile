FROM nginx:1.27.0@sha256:67682bda769fae1ccf5183192b8daf37b64cae99c6c3302650f6f8bf5f0f95df

COPY ./dataland-inbound-admin-proxy/nginx.conf /etc/nginx/nginx.conf

HEALTHCHECK --interval=30s --timeout=3s CMD curl -f http://localhost:6789/health/admin-proxy | tee /dev/stderr | grep -q 'UP' || false
