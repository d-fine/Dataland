#!/bin/bash
echo "Renewing Certificates from LetsEncrypt"
if [ -d "/etc/letsencrypt/live" ]; then
  # Renew certbot certificate (and reload nginx if required)
  certbot renew
fi
