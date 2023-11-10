#!/bin/bash

set -euxo pipefail

prodFilePath="/var/www/html/robots-prod.txt"
targetFilePath="/var/www/html/robots.txt"

serverName="$PROXY_PRIMARY_URL"
echo "Configuring robots.txt on $serverName"

if [[ $serverName == "dev2.dataland.com" ]]; then
  timeout 120 bash -c "while [[ ! -f $prodFilePath ]]; do echo 'robots-prod.txt not available for moving yet'; sleep 5; done;"
  echo "Replacing robots.txt for production on dataland.com"
  mv "$prodFilePath" "$targetFilePath"
fi
