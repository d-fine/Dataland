#!/bin/bash
echo "Certbot obtained new certificate. Deploying..."
rm /certs/dataland
ln -s $RENEWED_LINEAGE /certs/dataland

echo "Done. Reloading NGINX"
nginx -s reload
