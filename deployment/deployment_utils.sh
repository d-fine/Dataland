#!/bin/bash
set -euo pipefail

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

  mkdir -p $target_dir/dataland-keycloak/users;

  cat ./*github_env.log > "$target_dir"/.env
  set -o allexport
  source "$target_dir"/.env
  set +o allexport
  envsubst < environments/.env.template >> "$target_dir"/.env

  echo "Copying docker compose file."
  cp ./docker-compose.yml "$target_dir"

  echo "Copying keycloak files."
  cp -r ./dataland-keycloak/realms "$target_dir"/dataland-keycloak

  cp ./deployment/{initialize_keycloak,migrate_keycloak_users,deployment_utils}.sh "$target_dir"/dataland-keycloak
}

are_docker_containers_healthy_remote () {
  target_server_url=$1
  docker_healthcheck=$(ssh ubuntu@"$target_server_url" "docker inspect --format='\"{{index .Config.Labels \"com.docker.compose.service\"}}\";{{ if .State.Health}}{{.State.Health.Status}}{{else}}unknown{{end}}' \$(docker ps -aq)")
  service_list=("proxy" "admin-proxy" "backend" "backend-db" "frontend" "keycloak-db" "keycloak" "pgadmin")
  for service in ${service_list[@]}; do
    if [[ $docker_healthcheck != *"\"$service\";healthy"* ]]; then
      echo "Service $service not yet healthy... Waiting.."
      return 1
    fi
  done
}

export -f are_docker_containers_healthy_remote

wait_for_docker_containers_healthy_remote () {
  timeout 240 bash -c "while ! are_docker_containers_healthy_remote "$1"; do echo 'Containers not all healthy yet - retrying in 1s'; sleep 1; done; echo 'All containers healthy!'"
}
