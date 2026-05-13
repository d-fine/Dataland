#!/usr/bin/env bash
source "$(dirname "${BASH_SOURCE[0]}")/logging_functions.sh"
source "$(dirname "${BASH_SOURCE[0]}")/env_functions.sh"

# manageLocalStack always loads the base stack plus the local override that swaps Loki to a named Docker volume.
compose_files=(-f docker-compose.yml -f docker-compose.local.yml)
# These profiles cover the full local dev stack for cleanup/start-stop operations regardless of current frontend/backend mode.
development_profiles=(--profile development --profile developmentContainerFrontend --profile developmentContainerBackend)

# Suppress successful command output when silent mode is enabled, but replay captured output on failure.
run_quiet_command() {
  if [[ "$SILENT" != true ]]; then
    "$@"
    return
  fi

  local output_file
  output_file=$(mktemp)

  if "$@" >"$output_file" 2>&1; then
    rm -f "$output_file"
    return 0
  fi

  cat "$output_file" >&2
  rm -f "$output_file"
  return 1
}

run_step() {
  local description="$1"
  shift

  log_step "$description"
  run_quiet_command "$@"
}

run_docker_compose() {
  run_quiet_command docker compose "${compose_files[@]}" "$@"
}

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
  run_docker_compose "${development_profiles[@]}" down
  run_docker_compose "${development_profiles[@]}" pull --ignore-pull-failures
}

start_configured_services() {
  local wait_flag="$1"
  shift
  local compose_profiles=("$@")

  while read -r service; do
    [[ -n "$service" ]] || continue
    run_docker_compose "${compose_profiles[@]}" up -d --build ${wait_flag:+"$wait_flag"} "$service"
  done < ./localContainer.conf
}

start_all_services() {
  local wait_flag="$1"
  shift
  local compose_profiles=("$@")

  run_docker_compose "${compose_profiles[@]}" up -d --build ${wait_flag:+"$wait_flag"}
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
    log_info "Starting only services listed in localContainer.conf"
    start_configured_services "$wait_flag" "${compose_profiles[@]}"
  else
    start_all_services "$wait_flag" "${compose_profiles[@]}"
  fi
}

clear_docker_completely() {
  run_docker_compose "${development_profiles[@]}" --profile init down
  run_docker_compose down --remove-orphans
  run_quiet_command docker volume prune --force --all
}

rebuild_docker_images() {
  local log_folder="./log/build/"
  local max_parallel=6
  mkdir -p "$log_folder"

  for rebuild_script in ./build-utils/rebuild*.sh; do
    # Limit concurrent rebuilds to keep Docker and Gradle resource usage manageable locally.
    if [[ "$SILENT" != true && $(jobs -r | wc -l) -ge $max_parallel ]]; then
      log_info "Waiting for free build slot ($max_parallel parallel max)"
    fi

    while [[ $(jobs -r | wc -l) -ge $max_parallel ]]; do
      sleep 1
    done

    LOCAL=true "$rebuild_script" &> "./$log_folder/$(basename "$rebuild_script").log" &
  done

  log_info "Detailed build logs: $log_folder"
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
  run_docker_compose --profile init up --build -d

  while true; do
    local keycloak_logs
    # The init container exits on completion, so poll its logs for the success marker instead of health.
    keycloak_logs=$(docker compose "${compose_files[@]}" --profile init logs --no-color 2>&1 || true)
    if grep -q "Initialization of Keycloak finished\." <<< "$keycloak_logs"; then
      break
    fi
    log_info "Waiting for Keycloak to finish initialization"
    sleep 5
  done

  run_docker_compose --profile init down
}

stop_development_stack() {
  run_docker_compose "${development_profiles[@]}" down
}

wait_for_admin_proxy() {
  local compose_profiles=("$@")

  if [[ -s ./localContainer.conf ]]; then
    run_docker_compose "${compose_profiles[@]}" up -d --wait admin-proxy
  fi
}
