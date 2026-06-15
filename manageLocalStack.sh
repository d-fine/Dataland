#!/usr/bin/env bash
exec 3>&1 4>&2

set -euo pipefail

project_root="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
VERBOSE=false
export VERBOSE
source "$project_root/localstack/docker_functions.sh"
source "$project_root/localstack/env_functions.sh"
source "$project_root/localstack/cert_functions.sh"

print_usage() {
  echo "Usage: $(basename "$0") [--start] [--stop] [--reset] [--retrieve-certs] [--no-container-backend] [--verbose]" >&3
  echo "  --start: Start the development stack" >&3
  echo "  --stop: Stop the development stack" >&3
  echo "  --reset: Reset and restart the development stack from scratch" >&3
  echo "  --retrieve-certs: Retrieve the SSL certificates instead of using self-signed ones" >&3
  echo "  --no-container-backend: Run backend without containers" >&3
  echo "  --verbose: Print subcommand output while also writing it to the local stack log" >&3
  echo "" >&3
  echo "Multiple options can be combined in any order. Execution order is: stop, reset, start" >&3
}

rebuild_gradle_dockerfile() {
  rm -f ./*github_env.log
  ./build-utils/base_rebuild_gradle_dockerfile.sh
}

start_health_check() {
  mkdir -p "${LOKI_VOLUME}/health-check-log"
  ./health-check/healthCheck.sh &
}

prepare_loki_bind_mounts() {
  # Docker creates missing bind-mount source directories as root before the container starts.
  # Create both Loki bind-mount paths as the local user first so files written into the
  # project directory stay accessible to local tooling.
  mkdir -p "${LOKI_VOLUME}/health-check-log"
}

load_environment_step() {
  local description="$1"
  shift

  log_step "$description"
  "$@"
}

start_backend() {
  ./gradlew dataland-backend:bootRun --args='--spring.profiles.active=development' --no-daemon --stacktrace
}

start_development_stack() {
  local self_signed="$1"
  local container_backend="$2"

  run_step "Verifying environment variables" ./verifyEnvironmentVariables.sh
  run_step "Setting up SSL certificates" setup_certificates "$self_signed"
  run_step "Assembling projects" assemble_all_projects
  run_step "Rebuilding Gradle base image" rebuild_gradle_dockerfile
  load_environment_step "Loading generated GitHub environment" source_github_env_log
  load_environment_step "Loading uncritical environment" source_uncritical_environment
  run_step "Building Docker images" rebuild_docker_images
  load_environment_step "Reloading generated GitHub environment" source_github_env_log
  load_environment_step "Reloading uncritical environment" source_uncritical_environment

  local compose_profiles
  read -ra compose_profiles <<< "$(determine_compose_profiles "$container_backend")"

  if [[ "$container_backend" = true ]]; then
    export INTERNAL_BACKEND_URL="http://backend:8080/api"
    export BACKEND_URL="http://backend:8080/api/"
  else
    export INTERNAL_BACKEND_URL="http://host.docker.internal:8080/api"
    export BACKEND_URL="http://host.docker.internal:8080/api/"
  fi

  log_step "Cleaning up existing containers"
  stop_and_cleanup_containers
  run_step "Preparing Loki bind mounts" prepare_loki_bind_mounts
  log_step "Starting Docker services"
  start_docker_services "$container_backend" "${compose_profiles[@]}"
  start_health_check
  log_step "Waiting for admin-proxy"
  wait_for_admin_proxy "${compose_profiles[@]}"

  if [[ "$container_backend" = false ]]; then
    log_step "Starting backend locally"
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
  ./gradlew assemble dataland-frontend:npmInstall dataland-website:npmBuild
}

reset_development_stack() {
  local self_signed="$1"

  run_step "Verifying environment variables" ./verifyEnvironmentVariables.sh
  check_backend_not_running
  log_step "Clearing Docker state"
  clear_docker_completely
  run_step "Cleaning Gradle outputs" ./gradlew clean
  run_step "Assembling projects" assemble_all_projects
  run_step "Rebuilding Gradle base image" rebuild_gradle_dockerfile
  load_environment_step "Loading generated GitHub environment" source_github_env_log
  load_environment_step "Loading uncritical environment" source_uncritical_environment
  run_step "Rebuilding Postgres image" rebuild_postgres_image
  run_step "Rebuilding Keycloak image" rebuild_keycloak_image
  log_step "Initializing Keycloak"
  initialize_keycloak
}

parse_arguments() {
  local do_stop=false
  local do_reset=false
  local do_start=false
  
  local self_signed=true
  local container_backend=true

  load_dev_environment

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
      --retrieve-certs)
        self_signed=false
        shift
        ;;
      --no-container-backend)
        container_backend=false
        shift
        ;;
      --verbose)
        VERBOSE=true
        shift
        ;;
      *)
        log_error "Unknown option: $1"
        print_usage
        exit 1
        ;;
    esac
  done

  if [[ "$do_stop" = true ]]; then
    log_step "Stopping development stack"
    stop_development_stack
  fi

  if [[ "$do_reset" = true ]]; then
    reset_development_stack "$self_signed"
  fi

  if [[ "$do_start" = true ]]; then
    start_development_stack "$self_signed" "$container_backend"
  fi
}

initialize_local_stack_log "$@"
log_info "Local stack command logs: $MANAGE_LOCAL_STACK_LOG_FILE"
trap finalize_local_stack_log EXIT
parse_arguments "$@"
