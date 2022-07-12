#!/bin/bash
set -ux

environment=$1

if [[ $IN_MEMORY == true ]]; then
  profile=productionInMemory
else
  profile=production
  echo "Checking if EuroDaT is available before deploying to target server."
  if ! curl -f -X 'GET' "${TRUSTEE_BASE_URL}/${TRUSTEE_ENVIRONMENT_NAME}/api/ids/description" -H 'accept: application/json' >/dev/null 2>&1; then
    echo "EuroDaT is not available."
    exit 1
  fi
  echo "EuroDat is available."
fi

echo "Starting $environment server"
curl "$TARGETSERVER_STARTUP_URL" > /dev/null
echo "Setting $environment server as deployment target"
target_server_url="$TARGETSERVER_URL"

mkdir -p ~/.ssh/
echo "$TARGETSERVER_HOST_KEYS" >  ~/.ssh/known_hosts
echo "$SSH_PRIVATE_KEY" > ~/.ssh/id_rsa
chmod 600 ~/.ssh/id_rsa

timeout 300 bash -c "while ! ssh -o ConnectTimeout=3 ubuntu@$target_server_url exit; do echo '$environment server not yet there - retrying in 1s'; sleep 1; done" || exit

location=/home/ubuntu/dataland
# shut down currently running dataland application and purge files on server
ssh ubuntu@"$target_server_url" "cd $location && sudo docker-compose down"
# make sure no remnants remain when docker-compose file changes
ssh ubuntu@"$target_server_url" 'sudo docker kill $(sudo docker ps -q); sudo docker system prune --force; sudo docker info'
ssh ubuntu@"$target_server_url" "sudo rm -rf $location; mkdir -p $location/jar; mkdir -p $location/dataland-keycloak"

if [[ $INITIALIZE_KEYCLOAK == true ]]; then
  echo "Deployment configuration requires Keycloak to be set up from scratch."
  "$(dirname "$0")"/initialize_keycloak.sh "$target_server_url" "$location" || exit 1
fi

envsubst < environments/.env.template > .env

scp ./.env ubuntu@"$target_server_url":$location
scp -r ./dataland-frontend/dist ./docker-compose.yml ./dataland-inbound-proxy/ ./dataland-frontend/default.conf ubuntu@$target_server_url:$location
scp -r ./dataland-keycloak/dataland_theme ubuntu@$target_server_url:$location/dataland-keycloak
scp ./dataland-frontend/Dockerfile ubuntu@"$target_server_url":$location/DockerfileFrontend
scp ./dataland-backend/Dockerfile ubuntu@"$target_server_url":$location/DockerfileBackend
scp ./dataland-keycloak/Dockerfile ubuntu@"$target_server_url":$location/DockerfileKeycloak
scp ./dataland-backend/build/libs/dataland-backend*.jar ubuntu@"$target_server_url":$location/jar/dataland-backend.jar

echo "Starting docker compose stack."
ssh ubuntu@"$target_server_url" "cd $location; sudo docker-compose pull; sudo docker-compose --profile $profile up -d --build"

# Wait for backend to finish boot process
# Using the insecure flag here as valid ssl certificates will be obtained later
timeout 240 bash -c "while ! curl --insecure https://$target_server_url/api/actuator/health/ping 2>/dev/null | grep -q UP; do echo 'Waiting for backend to finish boot process.'; sleep 5; done; echo 'Backend available!'"

ssh ubuntu@"$target_server_url" "cd $location; sudo docker-compose exec proxy_prod sh /scripts/obtain-letsencrypt-certs.sh $PROXY_LETSENCRYPT_ARGS"

timeout 240 bash -c "while ! curl https://$target_server_url/api/actuator/health/ping 2>/dev/null | grep -q UP; do echo 'Waiting for backend to finish boot process (with proper SSL).'; sleep 5; done; echo 'Backend available!'"
