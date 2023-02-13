#!/bin/bash
# option pipefail does not work with bash in an nginx container
set -eux
echo "Getting Certificates after Boot"
while ! curl http://localhost/.well-known/d-statuscheck 2>/dev/null | grep -q UP; do
  echo "Waiting for NGINX to finish booting..."
  sleep 1;
done

if [ ! -d "/etc/letsencrypt/live" ] && [ ! -f "/certs/custom/privkey.pem" ]; then
  echo "Requesting LetsEncrypt certificates"
  mkdir -p /var/www/certbot && certbot certonly --non-interactive --webroot --webroot-path /var/www/certbot --agree-tos --preferred-challenges http $PROXY_LETSENCRYPT_ARGS
else
  echo "It looks like LetsEncrypt is already configured or custom certificates have been provided. Renewing"
  /scripts/renew-certificates.sh
fi
