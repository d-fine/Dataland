#!/bin/bash
set -u
mkdir -p ~/.ssh/
echo "3.71.162.94 ecdsa-sha2-nistp256 AAAAE2VjZHNhLXNoYTItbmlzdHAyNTYAAAAIbmlzdHAyNTYAAABBBNGocXXehCSfKoYwGdaYUpjvNm7gZE2LS7Nl/gGGXSxqwbGT+X6b+q7AGwhwZpFY9u17wv4NY3EOCK1cGaeot4k=" >  ~/.ssh/known_hosts
echo "$SSH_PRIVATE_KEY" > ~/.ssh/id_rsa
chmod 600 ~/.ssh/id_rsa

timeout 300 bash -c "while ! ssh -o ConnectTimeout=3 ubuntu@3.71.162.94 exit; do echo 'target server not yet there - retrying in 1s'; sleep 1; done" || exit

location=/home/ubuntu/dataland
# shut down currently running dataland application and purge files on server
ssh ubuntu@3.71.162.94 "cd $location && sudo docker-compose down; rm -rf $location; mkdir -p $location/jar"
# make sure no remnants remain when docker-compose file changes
ssh ubuntu@3.71.162.94 'sudo docker kill $(sudo docker ps -q); sudo docker system prune --force; sudo docker info'

scp -r ./dataland-frontend/dist ./deployment/docker-compose.yml ./dataland-inbound-proxy/ ubuntu@3.71.162.94:$location
scp ./dataland-backend/build/libs/dataland-backend*.jar ubuntu@3.71.162.94:$location/jar/dataland-backend.jar
ssh ubuntu@3.71.162.94 "cd $location; docker-compose pull; SKYMINDER_URL=$SKYMINDER_URL SKYMINDER_PW=$SKYMINDER_PW SKYMINDER_USER=$SKYMINDER_USER sudo -E docker-compose up -d"
