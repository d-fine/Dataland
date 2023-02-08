#!/bin/bash
# Remount certificate directory
echo "Reloading Certificates to nginx"
sh /scripts/configure-certificate-link.sh

# Reload NGINX
nginx -s reload
