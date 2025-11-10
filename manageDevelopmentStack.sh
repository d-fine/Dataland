#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "$SCRIPT_DIR/localstack/docker_functions.sh"
source "$SCRIPT_DIR/localstack/env_functions.sh"
source "$SCRIPT_DIR/localstack/cert_functions.sh"

rebuild_gradle_dockerfile() {
  rm ./*github_env.log || true
  ./build-utils/base_rebuild_gradle_dockerfile.sh
}

build_gradle_artifacts() {
  echo "Building all Gradle artifacts..."
  ./gradlew assemble
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
  local dev_env="$2"
  local self_signed="$3"
  
  if [ "$dev_env" = true ]; then
    load_dev_environment
  fi
  
  set -x
  verify_environment_variables
  setup_certificates "$self_signed"
  build_gradle_artifacts
  rebuild_gradle_dockerfile
  source_github_env_log
  source_uncritical_environment
  rebuild_docker_images
  source_github_env_log
  source_uncritical_environment
  
  local compose_profiles
  read -ra compose_profiles <<< "$(determine_compose_profiles "$local_frontend")"
  
  stop_and_cleanup_containers
  start_docker_services "${compose_profiles[@]}"
  start_health_check
  wait_for_admin_proxy
  start_backend
  set +x
}

check_backend_not_running() {
  if curl -L https://local-dev.dataland.com/api/actuator/health/ping 2>/dev/null | grep -q UP; then
    echo "ERROR: The backend is currently running. This will prevent the new backend from starting."
    echo "Shut down the running process and restart the script."
    exit 1
  fi
}

write_eurodat_secret_files() {
  ./dataland-eurodat-client/write_secret_files.sh
}

assemble_all_projects() {
  ./gradlew clean dataland-frontend:npmInstall
  ./gradlew assemble --rerun-tasks
}

reset_development_stack() {
  local dev_env="$1"
  local self_signed="$2"
  
  if [ "$dev_env" = true ]; then
    load_dev_environment
  fi
  
  set -x
  verify_environment_variables
  check_backend_not_running
  write_eurodat_secret_files
  clear_docker_completely
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
  local do_login=false
  local self_signed=false
  
  if [ $# -eq 0 ]; then
    echo "Usage: $(basename "$0") [--start] [--stop] [--reset] [--login] [--local-frontend] [--dev-env] [--self-signed-certs] [--simple]"
    echo "  --start: Start the development stack"
    echo "  --stop: Stop the development stack"
    echo "  --reset: Reset and restart the development stack from scratch"
    echo "  --login: Login to Docker registry (ghcr.io)"
    echo "  --local-frontend: Run in local frontend mode (redirect traffic to localhost)"
    echo "  --dev-env: Load environments/.env.dev before starting/resetting"
    echo "  --self-signed-certs: Generate and use self-signed SSL certificates instead of retrieving them"
    echo "  --simple: Shortcut for --dev-env --self-signed-certs"
    echo ""
    echo "Multiple options can be combined in any order. Execution order is: login, stop, reset, start"
    exit 1
  fi

  while [ $# -gt 0 ]; do
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
      --login)
        do_login=true
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
      *)
        echo "Unknown option: $1"
        echo "Usage: $(basename "$0") [--start] [--stop] [--reset] [--login] [--local-frontend] [--dev-env] [--self-signed-certs] [--simple]"
        exit 1
        ;;
    esac
  done

  if [ "$do_login" = true ]; then
    login_to_docker_repository
  fi

  if [ "$do_stop" = true ]; then
    stop_development_stack
  fi

  if [ "$do_reset" = true ]; then
    reset_development_stack "$dev_env" "$self_signed"
  fi

  if [ "$do_start" = true ]; then
    start_development_stack "$local_frontend" "$dev_env" "$self_signed"
  fi
}

parse_arguments "$@"