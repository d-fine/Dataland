#!/bin/bash
set -u

deployTo=$1

preview_server_ip="3.71.162.94"
preview_server_host_keys="$preview_server_ip ecdsa-sha2-nistp256 AAAAE2VjZHNhLXNoYTItbmlzdHAyNTYAAAAIbmlzdHAyNTYAAABBBNGocXXehCSfKoYwGdaYUpjvNm7gZE2LS7Nl/gGGXSxqwbGT+X6b+q7AGwhwZpFY9u17wv4NY3EOCK1cGaeot4k="

dev_server_ip="XXX"
dev_server_host_keys="$dev_server_ip XXX"

if [[ deployTo == preview_server ]]; then
  echo "Starting deployment for preview server"
  export target_server_ip=preview_server_ip
  export target_server_host_keys=preview_server_host_keys
elif [[ $SERVER == dev_server ]]; then
  echo "Starting deployment for dev server"
  export target_server_ip=dev_server_ip
  export target_server_host_keys=dev_server_host_keys
fi

mkdir -p ~/.ssh/
echo "$target_server_host_keys" >  ~/.ssh/known_hosts
echo "$SSH_PRIVATE_KEY" > ~/.ssh/id_rsa
chmod 600 ~/.ssh/id_rsa

timeout 300 bash -c "while ! ssh -o ConnectTimeout=3 ubuntu@$target_server_ip exit; do echo 'target server not yet there - retrying in 1s'; sleep 1; done" || exit

location=/home/ubuntu/dataland
# shut down currently running dataland application and purge files on server
ssh ubuntu@$target_server_ip "cd $location && sudo docker-compose down"
# make sure no remnants remain when docker-compose file changes
ssh ubuntu@$target_server_ip 'sudo docker kill $(sudo docker ps -q); sudo docker system prune --force; sudo docker info'
ssh ubuntu@$target_server_ip "sudo rm -rf $location; mkdir -p $location/jar"

envsubst < environments/.env.preview > .env

scp ./.env ubuntu@$target_server_ip:$location
scp -r ./dataland-frontend/dist ./docker-compose.yml ./dataland-inbound-proxy/ ./dataland-frontend/default.conf ubuntu@$target_server_ip:$location
scp ./dataland-frontend/Dockerfile ubuntu@$target_server_ip:$location/DockerfileFrontend
scp ./dataland-backend/Dockerfile ubuntu@$target_server_ip:$location/DockerfileBackend
scp ./dataland-backend/build/libs/dataland-backend*.jar ubuntu@$target_server_ip:$location/jar/dataland-backend.jar
ssh ubuntu@$target_server_ip "cd $location; sudo docker-compose pull; source ./.env; sudo -E docker-compose --profile production up -d --build"
