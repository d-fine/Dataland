FROM nginx:1.28.0@sha256:eaa7e36decc3421fc04478c586dfea0d931cebe47d5bc0b15d758a32ba51126f

COPY ./dataland-inbound-admin-proxy/nginx.conf /etc/nginx/nginx.conf

HEALTHCHECK --interval=30s --timeout=3s CMD curl -f http://localhost:6789/health/admin-proxy | tee /dev/stderr | grep -q 'UP' || false
