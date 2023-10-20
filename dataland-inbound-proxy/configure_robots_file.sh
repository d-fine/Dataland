#!/bin/bash
set -exo pipefail
currentEnv=$1
set -u
echo $currentEnv
#serverName=$(echo "$serverUrl" | sed -e 's|https://||' | cut -d'/' -f1)
datalandServerName="preview"

echo "Deploying to server: $currentEnv"
fileName="robots.txt"

blockingText="User-agent: *\n Disallow: /"
defaultText="User-agent: *\n Allow: / \n Disallow: /keycloak/ \n testabc"

rm fileName
if [[ $currentEnv == $datalandServerName ]]; then
  echo "This should be preview"
  printf defaultText > "$fileName"
else
  echo "This should not be preview"
  printf blockingText > "$fileName"
fi






