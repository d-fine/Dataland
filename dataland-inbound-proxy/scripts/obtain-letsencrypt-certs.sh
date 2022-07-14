#!/bin/sh
echo "Giving NGINX some time to finish starting up..."
sleep 7;
if [ ! -d "/etc/letsencrypt/live" ] && [ ! -f "/certs/custom/privkey.pem" ]; then
  echo "Requesting LetsEncrypt certificates"
  mkdir -p /var/www/certbot && certbot certonly --non-interactive --webroot --webroot-path /var/www/certbot --agree-tos --preferred-challenges http $PROXY_LETSENCRYPT_ARGS
  sh /scripts/reload-certificates.sh
else
  echo "It looks like LetsEncrypt is already configured or custom certificates have been provided. Skipping"
fi
