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

devText="User-agent: *\n Disallow: /test"
datalandText="User-agent: *\n Allow: / \n Disallow: /keycloak/"

scriptDir="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
cd "$scriptDir"
cd ..

rm "$filePath"
if [[ $serverName == $datalandServerName ]]; then
  echo "This should be dataland.com"
  printf "$datalandText" > "$filePath"
else
  echo "This should not be dataland.com"
  printf "$devText" > "$filePath"
fi

