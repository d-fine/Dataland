#!/bin/bash
set -exo pipefail
echo "Letsencrypt path:"$PROXY_LETSENCRYPT_PATH
currentEnv=$PROXY_LETSENCRYPT_PATH
set -u
echo $currentEnv
datalandServerDomain="dataland.com"

echo "Deploying to server: $currentEnv"
fileName="robots.txt"

blockingText="User-agent: *\n Disallow: /"
defaultText="User-agent: *\n Allow: / \n Disallow: /keycloak/ \n test"
cd ..
rm fileName
if [[ $currentEnv == $datalandServerDomain ]]; then
  echo "This should be dataland.com"
  printf defaultText > "$fileName"
else
  echo "This should not be dataland.com"
  printf blockingText > "$fileName"
fi

