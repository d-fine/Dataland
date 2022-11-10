#!/bin/bash
set -euxo pipefail

while ! curl http://localhost/.well-known/d-statuscheck 2>/dev/null | grep -q UP; do
  echo "Waiting for NGINX to finish booting..."
  sleep 1;
done

if [ ! -d "/etc/letsencrypt/live" ] && [ ! -f "/certs/custom/privkey.pem" ]; then
  echo "Requesting LetsEncrypt certificates"
  mkdir -p /var/www/certbot && certbot certonly --non-interactive --webroot --webroot-path /var/www/certbot --agree-tos --preferred-challenges http $PROXY_LETSENCRYPT_ARGS
  sh /scripts/reload-certificates.sh
else
  echo "It looks like LetsEncrypt is already configured or custom certificates have been provided. Skipping"
fi
