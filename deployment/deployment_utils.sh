#!/usr/bin/env bash
set -euo pipefail
source "$(dirname "${BASH_SOURCE[0]}")"/docker_utils.sh

setup_ssh () {
  mkdir -p ~/.ssh/
  echo "$TARGETSERVER_HOST_KEYS" >  ~/.ssh/known_hosts
  echo "$SSH_PRIVATE_KEY" > ~/.ssh/id_rsa
  chmod 600 ~/.ssh/id_rsa
}

wait_for_health () {
  timeout 240 bash -c "while ! curl -L $1 2>/dev/null | grep -q UP; do echo 'Waiting for $2 to finish boot process.'; sleep 5; done; echo '$2 available!'"
}

delete_docker_volume_if_existent () {
  volume_name=$(search_volume "$1")
  if [[ -n $volume_name ]]; then
    delete_docker_volume $volume_name
  fi
}

delete_docker_volume_if_existent_remotely () {
  volume_name_fragment="$1"
  target_server_url="$2"
  location="$3"
  ssh ubuntu@"$target_server_url" "source \"$location\"/dataland-keycloak/deployment_utils.sh; delete_docker_volume_if_existent \"$volume_name_fragment\""
}

delete_docker_volume () {
    echo "Removing old database volume with name $1."
    docker volume rm "$1"
}

search_volume () {
  volume_found=$(docker volume ls -q | grep "$1") || true
  echo "$volume_found"
}

build_directories () {
  target_dir=$1
  echo "Assembling deployment folder."
  mkdir -p "$target_dir"

  mkdir -p $target_dir/dataland-keycloak/users
  mkdir -p $target_dir/environments
  mkdir -p $target_dir/health-check

  echo "Copying env variable files."
  cat ./*github_env.log > "$target_dir"/.env
  set -o allexport
  source "$target_dir"/.env
  set +o allexport
  envsubst < environments/.env.template >> "$target_dir"/.env
  cp ./environments/.env.uncritical "$target_dir"/environments

  echo "Copying docker compose file."
  cp ./docker-compose.yml "$target_dir"

  echo "Copying keycloak files."
  cp -r ./dataland-keycloak/realms "$target_dir"/dataland-keycloak

  cp ./deployment/{initialize_keycloak,migrate_keycloak_users,deployment_utils,docker_utils}.sh "$target_dir"/dataland-keycloak

  echo "Copying health check configuration file."
  cp ./deployment/configure_container_health_check.sh "$target_dir"/health-check
}

wait_for_docker_containers_healthy_remote () {
  target_server_url="$1"
  location="$2"
  docker_compose_profile="$3"
  ssh ubuntu@"$target_server_url" "cd \"$location\"; source ./dataland-keycloak/deployment_utils.sh; timeout 600 bash -c \"wait_for_services_healthy_in_compose_profile $docker_compose_profile\""
}

create_loki_dir () {
  target_server_url="$1"
  loki_volume="$2"
  ssh ubuntu@"$target_server_url" "if [ ! -d '$loki_volume' ]; then
      echo 'Creating $loki_volume dir as volume for Loki container'
      sudo mkdir -p '$loki_volume'
      sudo chmod a+w '$loki_volume'
  fi"
}
