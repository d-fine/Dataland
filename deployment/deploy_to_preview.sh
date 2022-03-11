#!/bin/bash
set -u
mkdir -p ~/.ssh/
echo "3.71.162.94 ecdsa-sha2-nistp256 AAAAE2VjZHNhLXNoYTItbmlzdHAyNTYAAAAIbmlzdHAyNTYAAABBBNGocXXehCSfKoYwGdaYUpjvNm7gZE2LS7Nl/gGGXSxqwbGT+X6b+q7AGwhwZpFY9u17wv4NY3EOCK1cGaeot4k=" >  ~/.ssh/known_hosts
echo "$SSH_PRIVATE_KEY" > ~/.ssh/id_rsa
chmod 600 ~/.ssh/id_rsa

timeout 300 bash -c "while ! ssh -o ConnectTimeout=3 ubuntu@3.71.162.94 exit; do echo 'target server not yet there - retrying in 1s'; sleep 1; done" || exit

location=/home/ubuntu/dataland
#shut down currently running dataland application and purge files on server
ssh ubuntu@3.71.162.94 "cd $location && sudo docker-compose down; rm -rf $location; mkdir -p $location/jar"

scp -r ./dist ./deployment/start_app_on_server.sh ./deployment/docker-compose.yml ./nginx.conf ubuntu@3.71.162.94:$location
scp "$1" ubuntu@3.71.162.94:$location/jar/dala-backend.jar
ssh ubuntu@3.71.162.94 "export SKYMINDER_URL=$SKYMINDER_URL; export SKYMINDER_PW=$SKYMINDER_PW; export SKYMINDER_USER=$SKYMINDER_USER; cd $location; sudo -E docker-compose up -d"
