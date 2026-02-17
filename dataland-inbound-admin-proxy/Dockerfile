FROM nginx:1.29.4@sha256:9dd288848f4495869f76676e419ae2d767ca99fece2ec37ec0261f9fdaab5204

COPY ./dataland-inbound-admin-proxy/nginx.conf /etc/nginx/nginx.conf

HEALTHCHECK --interval=30s --timeout=3s CMD curl -f http://localhost:6789/health/admin-proxy | tee /dev/stderr | grep -q 'UP' || false
