#!/bin/bash
datalandServerName="dev2.dataland.com"
prodFilePath="/var/www/html/robots-prod.txt"
targetFilePath="/var/www/html/robots.txt"

set -exo pipefail

serverName="$PROXY_PRIMARY_URL"
echo $serverName
set -u

scriptDir="$( dirname "${BASH_SOURCE[0]}" )"
cd "$scriptDir"
cd ..

if [[ $serverName == $datalandServerName ]]; then
  echo "This should be the server dataland.com"
  rm "$targetFilePath"
  mv "$prodFilePath" "$targetFilePath"
else
  echo "This should not be the server dataland.com"
fi

