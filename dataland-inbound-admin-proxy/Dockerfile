FROM nginx:1.29.0@sha256:84ec966e61a8c7846f509da7eb081c55c1d56817448728924a87ab32f12a72fb

COPY ./dataland-inbound-admin-proxy/nginx.conf /etc/nginx/nginx.conf

HEALTHCHECK --interval=30s --timeout=3s CMD curl -f http://localhost:6789/health/admin-proxy | tee /dev/stderr | grep -q 'UP' || false
