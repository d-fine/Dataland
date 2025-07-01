FROM nginx:1.28.0@sha256:e2d0edf0e20951a190fe2322e27c6d790c675e9ff0018ba493947972f816f567

COPY ./dataland-inbound-admin-proxy/nginx.conf /etc/nginx/nginx.conf

HEALTHCHECK --interval=30s --timeout=3s CMD curl -f http://localhost:6789/health/admin-proxy | tee /dev/stderr | grep -q 'UP' || false
