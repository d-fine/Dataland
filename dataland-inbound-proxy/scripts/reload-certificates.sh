#!/bin/bash
# Remount certificate directory
sh /scripts/configure-certificate-link.sh

# Reload NGINX
nginx -s reload
