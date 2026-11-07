FROM nginx:1.31.7@sha256:8632b3314d74c61f0e27f59055bab13f7793b988c94c090154d867bc29e0f39b

COPY ./dataland-inbound-admin-proxy/nginx.conf /etc/nginx/nginx.conf

HEALTHCHECK --interval=30s --timeout=5s --start-period=180s --start-interval=60s --retries=6 CMD curl -f http://localhost:6789/health/admin-proxy | tee /dev/stderr | grep -q 'UP' || false
