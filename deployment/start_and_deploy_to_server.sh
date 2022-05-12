#!/bin/bash
set -u

environment=$1

preview_server_url="preview-dataland.duckdns.org"
preview_server_host_keys="$preview_server_url ecdsa-sha2-nistp256 AAAAE2VjZHNhLXNoYTItbmlzdHAyNTYAAAAIbmlzdHAyNTYAAABBBNGocXXehCSfKoYwGdaYUpjvNm7gZE2LS7Nl/gGGXSxqwbGT+X6b+q7AGwhwZpFY9u17wv4NY3EOCK1cGaeot4k="
preview_server_startup_url=$PREVIEW_STARTUP_URL

dev_server_url="dev-dataland.duckdns.org"
dev_server_host_keys="$dev_server_url ecdsa-sha2-nistp256 AAAAE2VjZHNhLXNoYTItbmlzdHAyNTYAAAAIbmlzdHAyNTYAAABBBHcwEGUE3yhkWxJ/dwafmNrVZDMZn62o26CoNo4ScNgwEaAxfeHDddpROrghaZ/avibYmzAAU8bwR76QG01v2RI="
dev_server_startup_url=$DEV_STARTUP_URL

if [[ $environment == preview ]]; then
  echo "Starting preview server"
  curl $preview_server_startup_url > /dev/null
  echo "Setting preview server as deployment target"
  target_server_url=$preview_server_url
  target_server_host_keys=$preview_server_host_keys
elif [[ $environment == development ]]; then
  echo "Starting development server"
  curl $dev_server_startup_url > /dev/null
  echo "Setting dev server as deployment target"
  target_server_url=$dev_server_url
  target_server_host_keys=$dev_server_host_keys
fi

mkdir -p ~/.ssh/
echo "$target_server_host_keys" >  ~/.ssh/known_hosts
echo "$SSH_PRIVATE_KEY" > ~/.ssh/id_rsa
chmod 600 ~/.ssh/id_rsa

timeout 300 bash -c "while ! ssh -o ConnectTimeout=3 ubuntu@$target_server_url exit; do echo 'target server not yet there - retrying in 1s'; sleep 1; done" || exit

location=/home/ubuntu/dataland
# shut down currently running dataland application and purge files on server
ssh ubuntu@$target_server_url "cd $location && sudo docker-compose down"
# make sure no remnants remain when docker-compose file changes
ssh ubuntu@$target_server_url 'sudo docker kill $(sudo docker ps -q); sudo docker system prune --force; sudo docker info'
ssh ubuntu@$target_server_url "sudo rm -rf $location; mkdir -p $location/jar"

envsubst < "environments/.env.${environment}" > .env

scp ./.env ubuntu@$target_server_url:$location
scp -r ./dataland-frontend/dist ./docker-compose.yml ./dataland-inbound-proxy/ ./dataland-frontend/default.conf ubuntu@$target_server_url:$location
scp ./dataland-frontend/Dockerfile ubuntu@$target_server_url:$location/DockerfileFrontend
scp ./dataland-backend/Dockerfile ubuntu@$target_server_url:$location/DockerfileBackend
scp ./dataland-backend/build/libs/dataland-backend*.jar ubuntu@$target_server_url:$location/jar/dataland-backend.jar
ssh ubuntu@$target_server_url "cd $location; sudo docker-compose pull; source ./.env; sudo -E docker-compose --profile production up -d --build"
