#!/usr/bin/env bash
source "$(dirname "${BASH_SOURCE[0]}")/logging_functions.sh"
source "$(dirname "${BASH_SOURCE[0]}")/env_functions.sh"

# These profiles cover the full local dev stack for cleanup/start-stop operations regardless of current frontend/backend mode.
development_profiles=(--profile development --profile developmentContainerBackend)

run_step() {
  local description="$1"
  shift

  log_step "$description"
  run_logged_command "$@"
}

run_docker_compose() {
  run_logged_command docker compose "$@"
}

# Named volumes that must survive Docker pruning regardless of whether they are currently
# attached to a running container. In particular, the devcontainer persists opencode's
# session/state data in these volumes across devcontainer rebuilds
# (see .devcontainer/devcontainer.json), so `docker volume prune --all` must never remove them.
protected_volume_names=(
  "dataland-opencode-local-share"
  "dataland-opencode-local-state"
)

# Removes unused volumes, but never the volumes listed in `protected_volume_names`.
#
# We can't just run `docker volume prune --all` with a `name!=` filter: Docker's volume
# prune/ls filters don't support negated name matching, only `dangling`/`label`. So instead
# we compute the set of unused volumes ourselves (all volumes minus those referenced by any
# container, running or stopped, minus the protected ones) and remove exactly those.
prune_unused_docker_volumes() {
  local all_volumes referenced_volumes unused_volumes
  mapfile -t all_volumes < <(docker volume ls -q)

  mapfile -t referenced_volumes < <(
    docker ps -aq | xargs -r docker inspect \
      --format '{{range .Mounts}}{{if eq .Type "volume"}}{{.Name}}{{"\n"}}{{end}}{{end}}' 2>/dev/null |
      sort -u
  )

  unused_volumes=()
  local volume is_protected is_referenced protected referenced
  for volume in "${all_volumes[@]}"; do
    is_protected=false
    for protected in "${protected_volume_names[@]}"; do
      if [[ "$volume" == "$protected" ]]; then
        is_protected=true
        break
      fi
    done
    [[ "$is_protected" == true ]] && continue

    is_referenced=false
    for referenced in "${referenced_volumes[@]}"; do
      if [[ "$volume" == "$referenced" ]]; then
        is_referenced=true
        break
      fi
    done
    [[ "$is_referenced" == true ]] && continue

    unused_volumes+=("$volume")
  done

  if [[ ${#unused_volumes[@]} -eq 0 ]]; then
    log_info "No unused Docker volumes to remove"
    return
  fi

  log_info "Removing unused Docker volumes: ${unused_volumes[*]}"
  run_logged_command docker volume rm "${unused_volumes[@]}"
}

determine_compose_profiles() {
  local container_backend="$1"
  local compose_profiles=(--profile development)

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
  prune_unused_docker_volumes
  run_logged_command docker image prune --all --force
  run_logged_command docker builder prune --all --force
  run_logged_command docker container prune --force
  clear_loki_bind_mount
}

prune_docker_environment() {
  log_step "Docker disk usage before pruning"
  run_logged_command docker system df

  log_step "Pruning unused Docker containers, images, volumes, and build cache"
  run_logged_command docker container prune --force
  prune_unused_docker_volumes
  run_logged_command docker image prune --all --force
  run_logged_command docker builder prune --all --force

  log_step "Docker disk usage after pruning"
  run_logged_command docker system df

  if command -v fstrim &>/dev/null; then
    log_step "Trimming filesystem (lets the host reclaim freed blocks, relevant on WSL2)"
    run_logged_command sudo fstrim -av
  fi

  log_info "On WSL2, Docker's freed space is not automatically returned to Windows."
  log_info "To reclaim it on your C: drive, run 'wsl --shutdown' in PowerShell, then compact"
  log_info "the distro's ext4.vhdx via diskpart. See the internal wiki for full instructions."
}

clear_loki_bind_mount() {
  # LOKI_VOLUME is a bind mount (not a Docker volume), so docker volume prune never cleans it up.
  # Left unchecked, Loki log data accumulates indefinitely on the host disk.
  if [[ -z "${LOKI_VOLUME:-}" ]]; then
    log_info "LOKI_VOLUME is not set, skipping Loki data cleanup"
    return
  fi

  log_info "Clearing Loki bind-mounted log data at ${LOKI_VOLUME}"
  rm -rf "${LOKI_VOLUME:?}"/*
}

rebuild_docker_images() {
  local log_folder="./log/build/"
  local max_parallel=6
  mkdir -p "$log_folder"

  for rebuild_script in ./build-utils/rebuild*.sh; do
    # Limit concurrent rebuilds to keep Docker and Gradle resource usage manageable locally.
    if [[ "$VERBOSE" == true && $(jobs -r | wc -l) -ge $max_parallel ]]; then
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
    keycloak_logs=$(docker compose --profile init logs --no-color 2>&1 || true)
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
