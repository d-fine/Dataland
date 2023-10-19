#!/bin/bash
serverUrl="$PROXY_PRIMARY_URL"
serverUrl="$GITHUB_SERVER_URL"
serverName=$(echo "$serverUrl" | sed -e 's|https://||' | cut -d'/' -f1)
datalandServerName = "dataland.com"

echo "Deploying to server: $serverName"
fileName = "robots.txt"

blockingText = "User-agent: *\n Disallow: /"
defaultText = "User-agent: *\n Allow: / \n Disallow: /keycloak/ \n testabc"

cd ..
rm fileName
if [ serverName == datalandServerName ]; then
  printf defaultText > "$fileName"
else
  printf blockingText > "$fileName"
fi






