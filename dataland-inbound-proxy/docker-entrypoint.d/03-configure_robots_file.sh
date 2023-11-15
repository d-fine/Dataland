#!/bin/bash

set -euxo pipefail

prodFilePath="/var/www/html/robots-prod.txt"
targetFilePath="/var/www/html/robots.txt"

serverName="$PROXY_PRIMARY_URL"
echo "Configuring robots.txt on $serverName"

if [[ $serverName == "dataland.com" ]]; then
  echo "Replacing robots.txt for production on dataland.com"
  cp "$prodFilePath" "$targetFilePath"
fi
