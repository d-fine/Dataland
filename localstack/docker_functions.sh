#!/usr/bin/env bash
source "$(dirname "${BASH_SOURCE[0]}")/env_functions.sh"

login_to_docker_repository() {
  echo "Please provide your GitHub credentials:"
  echo "Note: Use your GitHub username and a GitHub Personal Access Token (PAT)"
  echo ""
  read -p "GitHub username: " github_user
  read -sp "GitHub PAT: " github_token
  echo ""
  docker login ghcr.io -u "$github_user" -p "$github_token"
}

determine_compose_profiles() {
  local local_frontend="$1"
  local compose_profiles=(--profile development)
  
  if [[ "$local_frontend" == false ]]; then
    compose_profiles+=(--profile developmentContainerFrontend)
  fi
  
  echo "${compose_profiles[@]}"
}

stop_and_cleanup_containers() {
  docker compose --profile development --profile developmentContainerFrontend down
  docker volume ls -q | grep _pgadmin_config | xargs -r docker volume rm || true
  docker volume ls -q | grep _qa_service_data | xargs -r docker volume rm || true
  docker volume ls -q | grep _community_manager_data | xargs -r docker volume rm || true
  docker compose --profile development --profile developmentContainerFrontend pull --ignore-pull-failures --include-deps
}

start_configured_services() {
  local compose_profiles=("$@")
  
  while read -r service; do
    echo "Starting service $service"
    docker compose "${compose_profiles[@]}" up -d --build "$service"
  done < ./localContainer.conf
}

start_all_services() {
  local compose_profiles=("$@")
  
  echo "Starting stack in mode development."
  docker compose "${compose_profiles[@]}" up -d --build
}

start_docker_services() {
  local compose_profiles=("$@")
  
  if [[ -s ./localContainer.conf ]]; then
    echo "Starting only configured services."
    start_configured_services "${compose_profiles[@]}"
  else
    start_all_services "${compose_profiles[@]}"
  fi
}

clear_docker_completely() {
  echo "Clearing Docker..."
  docker compose --profile development --profile developmentContainerFrontend down
  docker compose --profile init down
  docker compose down --remove-orphans
  docker volume prune --force --all
}

rebuild_docker_images() {
  local log_folder="./log/build/"
  local max_parallel=6
  mkdir -p "$log_folder"

  for rebuild_script in ./build-utils/rebuild*.sh; do
    if [[ $rebuild_script =~ (prod|test|backend) ]]; then
      echo "Skipping $rebuild_script as it is not required for a local build."
      continue
    fi
    
    while [[ $(jobs -r | wc -l) -ge $max_parallel ]]; do
      echo "Waiting for builds to finish. Running at most $max_parallel in parallel."
      sleep 1
    done
    
    echo "Executing rebuild script $rebuild_script"
    "$rebuild_script" &> "./$log_folder/$(basename "$rebuild_script").log" &
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
  docker compose --profile init up --build &

  local project_name
  project_name=$(basename "$(pwd)")
  local lowercase_project_name
  lowercase_project_name=$(echo "$project_name" | tr '[:upper:]' '[:lower:]')
  local keycloak_initializer_container="$lowercase_project_name"-keycloak-initializer-1
  
  while ! docker logs "$keycloak_initializer_container" 2>/dev/null | grep -q "Added user 'admin' to realm 'master'"; do
    echo "Waiting for Keycloak to finish initializing..."
    sleep 5
  done

  docker compose --profile init down
}

stop_development_stack() {
  set -x
  docker compose --profile development --profile developmentContainerFrontend down
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
