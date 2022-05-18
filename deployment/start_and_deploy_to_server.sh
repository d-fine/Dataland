#!/bin/bash
set -u

environment=$1

echo "Checking if EuroDaT is available before deploying to target server."
if ! curl -f -X 'GET' "http://${TRUSTEE_IP}/api/ids/description" -H 'accept: application/json' >/dev/null 2>&1; then
  echo "EuroDaT is not available."
  exit 1
fi
echo "EuroDat is available."

echo "Starting ${environment} server"
curl "${TARGETSERVER_STARTUP_URL}" > /dev/null
echo "Setting ${environment} server as deployment target"
target_server_url="${TARGETSERVER_URL}"
target_server_host_keys="${TARGETSERVER_HOST_KEYS}"

mkdir -p ~/.ssh/
echo "$target_server_host_keys" >  ~/.ssh/known_hosts
echo "$SSH_PRIVATE_KEY" > ~/.ssh/id_rsa
chmod 600 ~/.ssh/id_rsa

timeout 300 bash -c "while ! ssh -o ConnectTimeout=3 ubuntu@$target_server_url exit; do echo '$environment server not yet there - retrying in 1s'; sleep 1; done" || exit

location=/home/ubuntu/dataland
# shut down currently running dataland application and purge files on server
ssh ubuntu@$target_server_url "cd $location && sudo docker-compose down"
# make sure no remnants remain when docker-compose file changes
ssh ubuntu@$target_server_url 'sudo docker kill $(sudo docker ps -q); sudo docker system prune --force; sudo docker info'
ssh ubuntu@$target_server_url "sudo rm -rf $location; mkdir -p $location/jar"

envsubst < environments/.env.template > .env

scp ./.env ubuntu@$target_server_url:$location
scp -r ./dataland-frontend/dist ./docker-compose.yml ./dataland-inbound-proxy/ ./dataland-frontend/default.conf ubuntu@$target_server_url:$location
scp ./dataland-frontend/Dockerfile ubuntu@$target_server_url:$location/DockerfileFrontend
scp ./dataland-backend/Dockerfile ubuntu@$target_server_url:$location/DockerfileBackend
scp ./dataland-backend/build/libs/dataland-backend*.jar ubuntu@$target_server_url:$location/jar/dataland-backend.jar
ssh ubuntu@$target_server_url "cd $location; sudo docker-compose pull; sudo docker-compose --profile production up -d --build"
