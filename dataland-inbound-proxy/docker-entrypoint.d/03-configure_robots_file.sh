#!/bin/bash
datalandServerName="dataland.com"
fileName="robots.txt"
delimiter="/"

set -exo pipefail
echo "Letsencrypt path:"$PROXY_LETSENCRYPT_PATH
fullServerPath=$PROXY_LETSENCRYPT_PATH

serverName="${fullServerPath##*"$delimiter"}"
echo serverName
set -u

devText="User-agent: *\n Disallow: /"
datalandText="User-agent: *\n Allow: / \n Disallow: /keycloak/ \n test"

scriptDir="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
cd "$scriptDir"
cd ..

rm $fileName
if [[ $serverName == $datalandServerName ]]; then
  echo "This should be dataland.com"
  printf datalandText > "$fileName"
else
  echo "This should not be dataland.com"
  printf devText > "$fileName"
fi

