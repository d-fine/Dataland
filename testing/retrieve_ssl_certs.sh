#!/bin/sh
mkdir -p ~/.ssh/
echo "$DATALAND_LETSENCRYPT_HOST_KEYS" >>  ~/.ssh/known_hosts
echo "$SSH_PRIVATE_KEY" > ~/.ssh/id_rsa
chmod 600 ~/.ssh/id_rsa && mkdir -p ./local/certs
scp ubuntu@dataland-letsencrypt.duckdns.org:/etc/letsencrypt/live/dataland-local.duckdns.org/* ./local/certs