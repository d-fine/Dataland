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
  old_volume=$(search_volume "$1")
  if [[ -n $old_volume ]]; then
    delete_docker_volume $old_volume
  fi
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

  envsubst < environments/.env.template > "$target_dir"/.env
  cat ./*github_env.log >> "$target_dir"/.env

  echo "Copying general files."
  cp -r ./dataland-frontend/dist ./docker-compose.yml ./dataland-inbound-proxy/ ./dataland-inbound-admin-proxy/ ./dataland-frontend/default.conf "$target_dir"
  cp -r ./dataland-pgadmin "$target_dir"
  cp ./dataland-frontend/Dockerfile "$target_dir"/DockerfileFrontend
  cp ./dataland-backend/Dockerfile "$target_dir"/DockerfileBackend
  cp ./dataland-backend/build/libs/dataland-backend*.jar "$target_dir"/jar/dataland-backend.jar
  cp -r ./dataland-frontend/src/assets/images "$target_dir"/dataland-frontend/src/assets

  echo "Copying keycloak files."
  cp -r ./dataland-keycloak/realms "$target_dir"/dataland-keycloak

  cp ./deployment/{initialize_keycloak,migrate_keycloak_users,deployment_utils}.sh "$target_dir"/dataland-keycloak
}