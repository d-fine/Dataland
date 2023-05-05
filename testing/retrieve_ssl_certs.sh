#!/bin/sh
mkdir -p ~/.ssh/
echo "TARGETSERVER_HOST_KEYS" >>  ~/.ssh/known_hosts
echo "$SSH_PRIVATE_KEY" > ~/.ssh/id_rsa
chmod 600 ~/.ssh/id_rsa && mkdir -p ./local/certs
scp ubuntu@letsencrypt.dataland.com:/etc/letsencrypt/live/local-dev.dataland.com/* ./local/certs
