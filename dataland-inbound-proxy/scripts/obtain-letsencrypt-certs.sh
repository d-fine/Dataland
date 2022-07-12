#!/bin/sh
if [ ! -d "/etc/letsencrypt/live" ]; then
  echo "Requesting LetsEncrypt certificates"
  mkdir -p /var/www/certbot && certbot certonly --non-interactive --webroot --webroot-path /var/www/certbot --agree-tos --preferred-challenges http "$@"
else
  echo "It looks like LetsEncrypt is already configured. Skipping"
fi
