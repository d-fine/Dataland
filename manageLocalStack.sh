#!/usr/bin/env bash
set -euo pipefail

project_root="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "$project_root/localstack/docker_functions.sh"
source "$project_root/localstack/env_functions.sh"
source "$project_root/localstack/cert_functions.sh"

print_usage() {
  echo "Usage: $(basename "$0") [--start] [--stop] [--reset] [--local-frontend] [--dev-env] [--self-signed-certs] [--simple] [--container-backend]"
  echo "  --start: Start the development stack"
  echo "  --stop: Stop the development stack"
  echo "  --reset: Reset and restart the development stack from scratch"
  echo "  --local-frontend: Run in local frontend mode (redirect traffic to localhost)"
  echo "  --dev-env: Load environments/.env.dev before starting/resetting"
  echo "  --self-signed-certs: Generate and use self-signed SSL certificates instead of retrieving them"
  echo "  --simple: Shortcut for --dev-env --self-signed-certs --container-backend"
  echo "  --container-backend: Run backend in Docker container instead of via Gradle bootRun"
  echo ""
  echo "Multiple options can be combined in any order. Execution order is: stop, reset, start"
}

rebuild_gradle_dockerfile() {
  rm ./*github_env.log || true
  ./build-utils/base_rebuild_gradle_dockerfile.sh
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

  set -x
  ./verifyEnvironmentVariables.sh
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
  start_docker_services "$container_backend" "${compose_profiles[@]}"
  start_health_check
  wait_for_admin_proxy

  if [[ "$container_backend" = false ]]; then
    start_backend
  fi
  set +x
}

check_backend_not_running() {
  if curl -L https://local-dev.dataland.com/api/actuator/health/ping 2>/dev/null | grep -q UP; then
    echo "ERROR: The backend is currently running. This will prevent the new backend from starting."
    echo "Shut down the running process and restart the script."
    exit 1
  fi
}

assemble_all_projects() {
  ./gradlew assemble dataland-frontend:npmInstall
}

reset_development_stack() {
  local self_signed="$1"

  set -x
  ./verifyEnvironmentVariables.sh
  check_backend_not_running
  clear_docker_completely
  ./gradlew clean
  assemble_all_projects
  rebuild_gradle_dockerfile
  source_github_env_log
  source_uncritical_environment
  rebuild_postgres_image
  rebuild_keycloak_image
  initialize_keycloak
  set +x
}

parse_arguments() {
  local local_frontend=false
  local dev_env=false
  local do_stop=false
  local do_reset=false
  local do_start=false
  
  local self_signed=false
  local container_backend=false

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
        do_reset=true
        shift
        ;;
      --start)
        do_start=true
        shift
        ;;
      --local-frontend)
        echo "Launching in local frontend mode."
        local_frontend=true
        export FRONTEND_LOCATION_CONFIG="Localhost"
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
        container_backend=true
        shift
        ;;
      --container-backend)
        container_backend=true
        shift
        ;;
      *)
        echo "Unknown option: $1"
        print_usage
        exit 1
        ;;
    esac
  done

  if [[ "$dev_env" = true ]]; then
    load_dev_environment
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
