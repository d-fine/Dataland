FROM nginx:1.27.4@sha256:124b44bfc9ccd1f3cedf4b592d4d1e8bddb78b51ec2ed5056c52d3692baebc19
# Install certbot and cron and delete default nginx config
RUN apt-get update && apt-get install -y certbot cron && rm -rf /etc/nginx/* && mkdir -p /etc/nginx/conf.d && apt-get clean && rm -rf /var/lib/apt/lists/*

COPY dataland-inbound-proxy/config /etc/nginx
COPY dataland-inbound-proxy/templates/*.template /etc/nginx/templates/
COPY dataland-inbound-proxy/dhparam.pem /certs/dhparam.pem
COPY dataland-inbound-proxy/docker-entrypoint.d/* /docker-entrypoint.d/
COPY dataland-inbound-proxy/scripts /scripts
COPY dataland-inbound-proxy/error_documents /var/www/html/error_documents
COPY dataland-inbound-proxy/robots.txt /var/www/html/robots.txt
COPY dataland-inbound-proxy/robots-prod.txt /var/www/html/robots-prod.txt
COPY dataland-frontend/src/assets/images /var/www/images
COPY ./gitinfo /var/www/html/gitinfo

RUN chmod -R +x /docker-entrypoint.d && chmod -R +x /scripts
RUN mkdir -p /certs /data/nginx/cache \
    && chown -R root:root /certs  \
    && chmod -R 600 /certs  \
    && mkdir -p /certs/dummy  \
    && openssl req -x509 -nodes -newkey rsa:2048 -days 36500 \
          -keyout "/certs/dummy/privkey.pem" \
          -out "/certs/dummy/fullchain.pem" \
          -subj "/CN=Dummy Certificate"

VOLUME /etc/letsencrypt

HEALTHCHECK --interval=30s --timeout=3s CMD curl -fk --header "Host: ${PROXY_PRIMARY_URL}" https://localhost/health/proxy | tee /dev/stderr | grep -q 'UP' || false
