#!/bin/bash
echo "Certbot obtained new certificate. Deploying..."
ln -sTf $RENEWED_LINEAGE /certs/dataland

echo "Done. Reloading NGINX"
nginx -s reload
