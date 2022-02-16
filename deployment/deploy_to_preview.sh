#!/bin/bash
mkdir -p ~/.ssh/
echo "3.71.162.94 ecdsa-sha2-nistp256 AAAAE2VjZHNhLXNoYTItbmlzdHAyNTYAAAAIbmlzdHAyNTYAAABBBNGocXXehCSfKoYwGdaYUpjvNm7gZE2LS7Nl/gGGXSxqwbGT+X6b+q7AGwhwZpFY9u17wv4NY3EOCK1cGaeot4k=" >  ~/.ssh/known_hosts
ssh-add - <<< "$SSH_PRIVATE_KEY"
chmod 600 ~/.ssh/id_rsa

ssh ubuntu@3.71.162.94 ls

