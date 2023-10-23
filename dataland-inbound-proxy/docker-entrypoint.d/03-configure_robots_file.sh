#!/bin/bash
datalandServerName="dataland.com"
filePath="/var/www/html/robots.txt"
delimiter="/"

set -exo pipefail
echo "Letsencrypt path:"$PROXY_LETSENCRYPT_PATH
fullServerPath=$PROXY_LETSENCRYPT_PATH

serverName="${fullServerPath##*"$delimiter"}"
echo serverName
set -u

devText="User-agent: *\nDisallow: /"
datalandText="User-agent: *\nAllow: / \nDisallow: /keycloak/"

scriptDir="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
cd "$scriptDir"
cd ..

rm "$filePath"
if [[ $serverName == $datalandServerName ]]; then
  echo "This should be the server dataland.com"
  printf "$datalandText" > "$filePath"
else
  echo "This should not be the server dataland.com"
  printf "$devText" > "$filePath"
fi

