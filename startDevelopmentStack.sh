#!/usr/bin/env bash

local_frontend=false

while getopts ':lh' opt; do
  case "$opt" in
    l)
      echo "Launching in local frontend mode."
      local_frontend=true
      export FRONTEND_LOCATION_CONFIG="Localhost"
      ;;

    h)
      echo "Usage: $(basename $0) [-l] [-h]"
      echo "  -l: Run in local frontend mode. Do not launch a frontend docker container. Instead, redirect frontend traffic to localhost."
      echo "      This significantly speeds up frontend development. Nevertheless, at some point, the frontend may freeze requiring a full restart of the docker engine."
      echo "      Note: You have to run the frontend locally on your machine (npm run dev) to make this work."
      echo "  -h: Display this help message."
      exit 0
      ;;

    :)
      echo -e "option requires an argument.\n"
      exit 1
      ;;

    ?)
      echo -e "Invalid command option.\n"
      exit 1
      ;;
  esac
done

set -euxo pipefail
./verifyEnvironmentVariables.sh

# Login to the docker repository
docker login ghcr.io -u $GITHUB_USER -p $GITHUB_TOKEN

# Retrieve the SSL-Certificates for local-dev.dataland.com
mkdir -p ./local/certs
scp ubuntu@letsencrypt.dataland.com:/etc/letsencrypt/live/local-dev.dataland.com/* ./local/certs

rm ./*github_env.log || true
./build-utils/base_rebuild_gradle_dockerfile.sh
set -o allexport
source ./*github_env.log
source ./environments/.env.uncritical
set +o allexport

log_folder="./log/build/"
mkdir -p "$log_folder"

for rebuild_script in ./build-utils/rebuild*.sh; do
  if [[ $rebuild_script =~ (prod|test|backend) ]]; then
    echo "Skipping $rebuild_script as it is not required for a local build."
    continue
  fi
  echo "Executing rebuild script $rebuild_script"
  $rebuild_script &> "./$log_folder/$(basename "$rebuild_script").log" &
done

sleep 5
echo "Waiting for all build processes to terminate."
echo "Progress may be monitored using the logs in $log_folder"
wait

set -o allexport
source ./*github_env.log
set +o allexport

compose_profiles=(--profile development)
if [[ $local_frontend == false ]]; then
  compose_profiles+=(--profile developmentContainerFrontend)
fi

# start containers with the stack except backend
docker compose --profile development --profile developmentContainerFrontend down
docker volume rm $(docker volume ls -q | grep _pgadmin_config) || true
docker volume rm $(docker volume ls -q | grep _qa_service_data) || true
docker volume rm $(docker volume ls -q | grep _community_manager_data) || true
docker compose --profile development --profile developmentContainerFrontend pull --ignore-pull-failures --include-deps

if [[ -s ./localContainer.conf ]]; then
  echo "Starting only configured services."
  for service in $(cat ./localContainer.conf); do
    echo "Starting service $service"
    docker compose "${compose_profiles[@]}" up -d --build "$service"
  done
else
  echo "Starting stack in mode development."
  docker compose "${compose_profiles[@]}" up -d --build
fi

# Setup the docker container health status check job
while true; do
  ./health-check/healthCheck.sh
  sleep 60  # check health status every minute
done

#start the backend
./gradlew dataland-backend:bootRun --args='--spring.profiles.active=development' --no-daemon --stacktrace
