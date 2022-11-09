#!/bin/bash
if [ -d "/etc/letsencrypt/live" ]; then
  # Renew certbot certificate (and reload nginx if required)
  certbot renew --post-hook "sh /scripts/reload-certificates.sh"
fi
