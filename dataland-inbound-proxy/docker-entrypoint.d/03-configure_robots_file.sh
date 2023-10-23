#!/bin/bash
datalandServerName="dataland.com"
filePath="/var/www/html/robots.txt"
delimiter="/"

set -exo pipefail
echo "Letsencrypt path:"$PROXY_LETSENCRYPT_PATH
fullServerPath=$PROXY_LETSENCRYPT_PATH

serverName="${fullServerPath##*"$delimiter"}"
echo $serverName
set -u

devText="User-agent: *\nDisallow: /"
datalandText="User-agent: *\nAllow: / \nDisallow: /keycloak/"

scriptDir="$( dirname "${BASH_SOURCE[0]}" )"
cd "$scriptDir"
cd ..

if [[ $serverName == $datalandServerName ]]; then
  echo "This should be the server dataland.com"
  touch "$filePath"
  printf "$datalandText" > "$filePath"
else
  echo "This should not be the server dataland.com"
  touch "$filePath"
  printf "$devText" > "$filePath"
fi

