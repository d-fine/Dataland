FROM nginx

# Install certbot and cron and delete default nginx config
RUN apt-get update && apt-get install -y certbot cron && rm -rf /etc/nginx/*

# Add CronJob for certificate renewal (Add 00:00 on every day)
RUN echo "0 0 * * * root sh /scripts/renew-certbot.sh" > /etc/cron.d/certbot-renew

COPY config /etc/nginx
COPY templates/dataland-prod.template /etc/nginx/templates/dataland-prod.template
COPY docker-entrypoint.d/* /docker-entrypoint.d/
COPY scripts /scripts

RUN chmod -R +x /docker-entrypoint.d
RUN chmod -R +x /scripts

VOLUME /etc/letsencrypt