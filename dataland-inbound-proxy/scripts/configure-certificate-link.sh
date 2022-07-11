#!/bin/sh

# This script checks if LetsEncrypt certificates are available
# and rewrites the symlink for the nginx certificates accordingly

if [ -d "/certs/custom" ]; then
  echo "Found custom certificates folder. Rewring symlink"
  rm /certs/dataland
  ln -s /certs/custom /certs/dataland
elif [ -d "$PROXY_LETSENCRYPT_PATH" ]; then
  echo "LetsEncrypt is there. Rewriting symlink"
  rm /certs/dataland
  ln -s $PROXY_LETSENCRYPT_PATH /certs/dataland
else
  echo "LetsEncrypt is NOT there. Rewriting symlink"
  rm /certs/dataland
  ln -s /certs/dummy /certs/dataland
fi