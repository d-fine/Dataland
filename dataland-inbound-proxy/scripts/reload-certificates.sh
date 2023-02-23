#!/bin/bash
# Remount certificate directory
echo "Reloading Certificates to nginx"
/scripts/configure-certificate-link.sh

# Reload NGINX
nginx -s reload
