#!/bin/bash
mkdir -p ~/.ssh/
echo "3.71.162.94 ecdsa-sha2-nistp256 AAAAE2VjZHNhLXNoYTItbmlzdHAyNTYAAAAIbmlzdHAyNTYAAABBBNGocXXehCSfKoYwGdaYUpjvNm7gZE2LS7Nl/gGGXSxqwbGT+X6b+q7AGwhwZpFY9u17wv4NY3EOCK1cGaeot4k=" >  ~/.ssh/known_hosts
echo "$SSH_PRIVATE_KEY" > ~/.ssh/id_rsa
chmod 600 ~/.ssh/id_rsa

scp ./deployment/start_app_on_server.sh ubuntu@3.71.162.94:/home/ubuntu/start_app_on_server.sh
scp "$1" ubuntu@3.71.162.94:/home/ubuntu/jar/dala-backend.jar
ssh ubuntu@3.71.162.94 "chmod +x /home/ubuntu/start_app_on_server.sh && sudo /home/ubuntu/start_app_on_server.sh"
