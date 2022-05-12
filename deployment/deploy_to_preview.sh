#!/bin/bash
set -u

export server="3.71.162.94"
mkdir -p ~/.ssh/
echo "$server ecdsa-sha2-nistp256 AAAAE2VjZHNhLXNoYTItbmlzdHAyNTYAAAAIbmlzdHAyNTYAAABBBNGocXXehCSfKoYwGdaYUpjvNm7gZE2LS7Nl/gGGXSxqwbGT+X6b+q7AGwhwZpFY9u17wv4NY3EOCK1cGaeot4k=" >  ~/.ssh/known_hosts
echo "$SSH_PRIVATE_KEY" > ~/.ssh/id_rsa
chmod 600 ~/.ssh/id_rsa

timeout 300 bash -c "while ! ssh -o ConnectTimeout=3 ubuntu@$server exit; do echo 'target server not yet there - retrying in 1s'; sleep 1; done" || exit

location=/home/ubuntu/dataland
# shut down currently running dataland application and purge files on server
ssh ubuntu@$server "cd $location && sudo docker-compose down"
# make sure no remnants remain when docker-compose file changes
ssh ubuntu@$server 'sudo docker kill $(sudo docker ps -q); sudo docker system prune --force; sudo docker info'
ssh ubuntu@$server "sudo rm -rf $location; mkdir -p $location/jar"

envsubst < environments/.env.preview > .env

scp ./.env ubuntu@$server:$location
scp -r ./dataland-frontend/dist ./docker-compose.yml ./dataland-inbound-proxy/ ./dataland-frontend/default.conf ubuntu@$server:$location
scp ./dataland-frontend/Dockerfile ubuntu@$server:$location/DockerfileFrontend
scp ./dataland-backend/Dockerfile ubuntu@$server:$location/DockerfileBackend
scp ./dataland-backend/build/libs/dataland-backend*.jar ubuntu@$server:$location/jar/dataland-backend.jar
ssh ubuntu@$server "cd $location; sudo docker-compose pull; sudo -E docker-compose --profile production up -d --build"
