#!/bin/bash

# This script checks if LetsEncrypt certificates are available
# and rewrites the symlink for the nginx certificates accordingly
echo "Configuring Certificate Folder"
if [ -f "/certs/custom/privkey.pem" ]; then
  echo "Found custom certificates folder. Rewriting symlink"
  rm /certs/dataland
  ln -s /certs/custom /certs/dataland
elif [ -f "$PROXY_LETSENCRYPT_PATH/privkey.pem" ]; then
  echo "LetsEncrypt is there. Rewriting symlink"
  rm /certs/dataland
  ln -s $PROXY_LETSENCRYPT_PATH /certs/dataland
else
  echo "LetsEncrypt is NOT there. Rewriting symlink"
  rm /certs/dataland
  ln -s /certs/dummy /certs/dataland
fi
