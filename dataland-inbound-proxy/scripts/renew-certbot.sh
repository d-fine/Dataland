#!/bin/sh
if [ -d "/etc/letsencrypt/live" ]; then
  # Renew certbot certificate (and reload nginx if required)
  certbot renew --post-hook "sh /scripts/certificate-renewal-hook.sh"
fi

