#!/bin/bash
datalandServerName="dataland.com"
filePath="/var/www/html/robots.txt"
delimiter="/"

set -exo pipefail

serverName="$PROXY_PRIMARY_URL"
echo $serverName
set -u

devText="User-agent: *\nDisallow: /"
datalandText="User-agent: *\nAllow: / \nDisallow: /keycloak/"

scriptDir="$( dirname "${BASH_SOURCE[0]}" )"
cd "$scriptDir"
cd ..

touch "$filePath"
if [[ $serverName == $datalandServerName ]]; then
  echo "This should be the server dataland.com"
  printf "$datalandText" > "$filePath"
else
  echo "This should not be the server dataland.com"
  printf "$devText" > "$filePath"
fi

