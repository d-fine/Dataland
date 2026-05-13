#!/usr/bin/env bash
set -euo pipefail

project_root="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "$project_root/localstack/docker_functions.sh"
source "$project_root/localstack/env_functions.sh"
source "$project_root/localstack/cert_functions.sh"

print_usage() {
  echo "Usage: $(basename "$0") [--start] [--stop] [--reset] [--local-frontend] [--dev-env] [--self-signed-certs] [--simple] [--no-container-backend] [--silent]"
  echo "  --start: Start the development stack"
  echo "  --stop: Stop the development stack"
  echo "  --reset: Reset and restart the development stack from scratch"
  echo "  --local-frontend: Run in local frontend mode (redirect traffic to localhost)"
  echo "  --dev-env: Load environments/.env.dev before starting/resetting"
  echo "  --self-signed-certs: Generate and use self-signed SSL certificates instead of retrieving them"
  echo "  --simple: Shortcut for --dev-env --self-signed-certs"
  echo "  --no-container-backend: Run backend without containers"
  echo "  --silent: Suppress subcommand output"
  echo ""
  echo "Multiple options can be combined in any order. Execution order is: stop, reset, start"
}

rebuild_gradle_dockerfile() {
  rm -f ./*github_env.log
  run_step "Rebuilding Gradle base image" ./build-utils/base_rebuild_gradle_dockerfile.sh
}

prepare_loki_volume() {
  # In WSL we get permission errors on local loki volume subdirs which is circumvented by preparing them and setting
  # permissive permissions upfront
  log_step "Preparing Loki volume"

  local loki_dirs=(
    "${LOKI_VOLUME}"
    "${LOKI_VOLUME}/chunks"
    "${LOKI_VOLUME}/compactor"
    "${LOKI_VOLUME}/rules"
    "${LOKI_VOLUME}/index"
    "${LOKI_VOLUME}/index_cache"
    "${LOKI_VOLUME}/wal"
    "${LOKI_VOLUME}/health-check-log"
  )

  mkdir -p "${loki_dirs[@]}"
  chmod 777 "${loki_dirs[@]}"

  log_success "Preparing Loki volume"
}

start_health_check() {
  mkdir -p "${LOKI_VOLUME}/health-check-log"
  ./health-check/healthCheck.sh &
}

start_backend() {
  ./gradlew dataland-backend:bootRun --args='--spring.profiles.active=development' --no-daemon --stacktrace
}

start_development_stack() {
  local local_frontend="$1"
  local self_signed="$2"
  local container_backend="$3"

  run_step "Verifying environment variables" ./verifyEnvironmentVariables.sh
  setup_certificates "$self_signed"
  assemble_all_projects
  rebuild_gradle_dockerfile
  source_github_env_log
  source_uncritical_environment
  rebuild_docker_images
  source_github_env_log
  source_uncritical_environment

  local compose_profiles
  read -ra compose_profiles <<< "$(determine_compose_profiles "$local_frontend" "$container_backend")"

  if [[ "$container_backend" = true ]]; then
    export INTERNAL_BACKEND_URL="http://backend:8080/api"
    export BACKEND_URL="http://backend:8080/api/"
  else
    export INTERNAL_BACKEND_URL="http://host.docker.internal:8080/api"
    export BACKEND_URL="http://host.docker.internal:8080/api/"
  fi

  stop_and_cleanup_containers
  prepare_loki_volume
  start_docker_services "$container_backend" "${compose_profiles[@]}"
  start_health_check
  wait_for_admin_proxy "${compose_profiles[@]}"

  if [[ "$container_backend" = false ]]; then
    log_success "Local stack services started. Launching backend locally."
    start_backend
    return
  fi

  log_success "Local stack started."
}

check_backend_not_running() {
  if curl -L https://local-dev.dataland.com/api/actuator/health/ping 2>/dev/null | grep -q UP; then
    log_error "The backend is currently running. This will prevent the new backend from starting."
    log_info "Shut down the running process and restart the script."
    exit 1
  fi
}

assemble_all_projects() {
  run_step "Assembling projects" ./gradlew assemble dataland-frontend:npmInstall dataland-website:npmBuild
}

reset_development_stack() {
  local self_signed="$1"

  run_step "Verifying environment variables" ./verifyEnvironmentVariables.sh
  check_backend_not_running
  clear_docker_completely
  run_step "Cleaning Gradle outputs" ./gradlew clean
  assemble_all_projects
  rebuild_gradle_dockerfile
  source_github_env_log
  source_uncritical_environment
  rebuild_postgres_image
  rebuild_keycloak_image
  initialize_keycloak
}

parse_arguments() {
  local local_frontend=false
  local dev_env=false
  local do_stop=false
  local do_reset=false
  local do_start=false
  
  local self_signed=false
  local container_backend=true
  SILENT=false

  if [[ $# -eq 0 ]]; then
    print_usage
    exit 1
  fi

  while [[ $# -gt 0 ]]; do
    case "$1" in
      --stop)
        do_stop=true
        shift
        ;;
      --reset)
        do_stop=true
        do_reset=true
        do_start=true
        shift
        ;;
      --start)
        do_start=true
        shift
        ;;
      --local-frontend)
        log_info "Launching in local frontend mode"
        local_frontend=true
        shift
        ;;
      --dev-env)
        dev_env=true
        shift
        ;;
      --self-signed-certs)
        self_signed=true
        shift
        ;;
      --simple)
        dev_env=true
        self_signed=true
        shift
        ;;
      --no-container-backend)
        container_backend=false
        shift
        ;;
      --silent)
        SILENT=true
        shift
        ;;
      *)
        log_error "Unknown option: $1"
        print_usage
        exit 1
        ;;
    esac
  done

  if [[ "$dev_env" = true ]]; then
    load_dev_environment
  fi

  if [[ "$local_frontend" = true ]]; then
    export FRONTEND_LOCATION_CONFIG="Localhost"
  fi

  if [[ "$do_stop" = true ]]; then
    stop_development_stack
  fi

  if [[ "$do_reset" = true ]]; then
    reset_development_stack "$self_signed"
  fi

  if [[ "$do_start" = true ]]; then
    start_development_stack "$local_frontend" "$self_signed" "$container_backend"
  fi
}

parse_arguments "$@"
