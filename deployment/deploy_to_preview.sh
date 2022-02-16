#!/bin/bash
mkdir -p ~/.ssh/
echo "3.71.162.94 ecdsa-sha2-nistp256 AAAAE2VjZHNhLXNoYTItbmlzdHAyNTYAAAAIbmlzdHAyNTYAAABBBNGocXXehCSfKoYwGdaYUpjvNm7gZE2LS7Nl/gGGXSxqwbGT+X6b+q7AGwhwZpFY9u17wv4NY3EOCK1cGaeot4k=" >  ~/.ssh/known_hosts
echo "$SSH_PRIVATE_KEY"
echo "$SSH_PRIVATE_KEY" > ~/.ssh/id_rsa
cat ~/.ssh/id_rsa
echo "$SSH_PRIVATE_KEY" | tr -d '\r' > ~/.ssh/id_rsa
cat ~/.ssh/id_rsa

chmod 600 ~/.ssh/id_rsa

ssh ubuntu@3.71.162.94 ls

