FROM nginx
ARG PROXY_ENVIRONMENT
# Install certbot and cron and delete default nginx config
RUN apt-get update && apt-get install -y certbot cron && rm -rf /etc/nginx/* && mkdir -p /etc/nginx/conf.d && apt-get clean

# Add CronJob for certificate renewal (Add 00:00 on every day)
RUN echo "0 0 * * * root sh /scripts/renew-certificates.sh" > /etc/cron.d/certbot-renew

COPY config /etc/nginx
COPY templates/*-dataland-$PROXY_ENVIRONMENT.template /etc/nginx/templates/
COPY templates/http-letsencrypt.template /etc/nginx/templates/http-letsencrypt.template
COPY dhparam.pem /certs/dhparam.pem
COPY docker-entrypoint.d/* /docker-entrypoint.d/
COPY scripts /scripts
COPY custom_errors.html /var/wwww/html/custom_errors.html
COPY robots.txt /var/wwww/html/robots.txt

RUN chmod -R +x /docker-entrypoint.d && chmod -R +x /scripts

VOLUME /etc/letsencrypt
