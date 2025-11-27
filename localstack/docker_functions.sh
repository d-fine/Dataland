#!/usr/bin/env bash
source "$(dirname "${BASH_SOURCE[0]}")/env_functions.sh"

determine_compose_profiles() {
  local local_frontend="$1"
  local container_backend="$2"
  local compose_profiles=(--profile development)

  if [[ "$local_frontend" == false ]]; then
    compose_profiles+=(--profile developmentContainerFrontend)
  fi

  if [[ "$container_backend" == true ]]; then
    compose_profiles+=(--profile developmentContainerBackend)
  fi

  echo "${compose_profiles[@]}"
}

stop_and_cleanup_containers() {
  docker compose --profile development --profile developmentContainerFrontend --profile developmentContainerBackend down
  docker compose --profile development --profile developmentContainerFrontend --profile developmentContainerBackend pull --ignore-pull-failures --include-deps
}

start_configured_services() {
  local wait_flag="$1"
  shift
  local compose_profiles=("$@")

  while read -r service; do
    echo "Starting service $service"
    docker compose "${compose_profiles[@]}" up -d --build $wait_flag "$service"
  done < ./localContainer.conf
}

start_all_services() {
  local wait_flag="$1"
  shift
  local compose_profiles=("$@")

  echo "Starting stack in mode development."
  docker compose "${compose_profiles[@]}" up -d --build $wait_flag
}

start_docker_services() {
  local container_backend="$1"
  shift
  local compose_profiles=("$@")

  local wait_flag=""
  if [[ "$container_backend" == "true" ]]; then
    wait_flag="--wait"
  fi

  if [[ -s ./localContainer.conf ]]; then
    echo "Starting only configured services."
    start_configured_services "$wait_flag" "${compose_profiles[@]}"
  else
    start_all_services "$wait_flag" "${compose_profiles[@]}"
  fi
}

clear_docker_completely() {
  echo "Clearing Docker..."
  docker compose --profile development --profile developmentContainerFrontend --profile developmentContainerBackend down
  docker compose --profile init down
  docker compose down --remove-orphans
  docker volume prune --force --all
}

rebuild_docker_images() {
  local log_folder="./log/build/"
  local max_parallel=6
  mkdir -p "$log_folder"

  for rebuild_script in ./build-utils/rebuild*.sh; do
    while [[ $(jobs -r | wc -l) -ge $max_parallel ]]; do
      echo "Waiting for builds to finish. Running at most $max_parallel in parallel."
      sleep 1
    done

    echo "Executing rebuild script $rebuild_script"
    LOCAL=true "$rebuild_script" &> "./$log_folder/$(basename "$rebuild_script").log" &
  done

  echo "Waiting for all build processes to terminate."
  echo "Progress may be monitored using the logs in $log_folder"
  wait
}

rebuild_postgres_image() {
  ./build-utils/rebuild_postgres_image.sh
  source_github_env_log
}

rebuild_keycloak_image() {
  ./build-utils/rebuild_keycloak_image.sh
  source_github_env_log
}

initialize_keycloak() {
  echo "Initializing Keycloak..."
  docker compose --profile init up --build -d

  while true; do
    if docker compose --profile init logs --no-color | grep -q "Added user 'admin' to realm 'master'"; then
      break
    fi
    echo "Waiting for Keycloak to finish initializing..."
    sleep 5
  done

  docker compose --profile init down
}

stop_development_stack() {
  set -x
  docker compose --profile development --profile developmentContainerFrontend --profile developmentContainerBackend down
  set +x
}

wait_for_admin_proxy() {
  if [[ -s ./localContainer.conf ]]; then
    until docker ps | grep admin-proxy | grep -q \(healthy\)
    do
      echo "Waiting for admin-proxy to be healthy as it is required for executing the backend."
      sleep 5
    done
  fi
}
