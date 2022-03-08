#!/bin/bash
mkdir -p ~/.ssh/
echo "3.71.162.94 ecdsa-sha2-nistp256 AAAAE2VjZHNhLXNoYTItbmlzdHAyNTYAAAAIbmlzdHAyNTYAAABBBNGocXXehCSfKoYwGdaYUpjvNm7gZE2LS7Nl/gGGXSxqwbGT+X6b+q7AGwhwZpFY9u17wv4NY3EOCK1cGaeot4k=" >  ~/.ssh/known_hosts
echo "$SSH_PRIVATE_KEY" > ~/.ssh/id_rsa
chmod 600 ~/.ssh/id_rsa

timeout 300 bash -c "while ! ssh -o ConnectTimeout=3 ubuntu@3.71.162.94 exit; do echo 'target server not yet there - retrying in 1s'; sleep 1; done" || exit

scp ./deployment/start_app_on_server.sh ubuntu@3.71.162.94:/home/ubuntu/start_app_on_server.sh
scp ./deployment/docker-compose.yml ubuntu@3.71.162.94:/home/ubuntu/docker-compose.yml
scp ./nginx.conf ubuntu@3.71.162.94:/home/ubuntu/nginx.conf
scp "$1" ubuntu@3.71.162.94:/home/ubuntu/jar/dala-backend.jar
scp -r "$2" ubuntu@3.71.162.94:/home/ubuntu/
ssh ubuntu@3.71.162.94 "export SKYMINDER_URL=$SKYMINDER_URL; export SKYMINDER_PW=$SKYMINDER_PW; export SKYMINDER_USER=$SKYMINDER_USER; sudo -E /home/ubuntu/start_app_on_server.sh"
