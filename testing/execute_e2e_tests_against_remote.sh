#!/usr/bin/env bash
set -euxo pipefail

# Runs the Cypress E2E tests from the dataland_e2etests image against an already deployed Dataland server.
# Uses the host network so that the admin SSH tunnel (see deployment/setup_admin_ssh_tunnel.sh) is reachable.

e2etests_image="ghcr.io/d-fine/dataland/dataland_e2etests:${DATALAND_E2ETESTS_VERSION}"

forwarded_env_vars=(
  ELECTRON_EXTRA_LAUNCH_ARGS
  KEYCLOAK_READER_PASSWORD
  KEYCLOAK_UPLOADER_PASSWORD
  KEYCLOAK_REVIEWER_PASSWORD
  KEYCLOAK_JUDGE_PASSWORD
  KEYCLOAK_PREMIUM_USER_PASSWORD
  KEYCLOAK_DATALAND_ADMIN_PASSWORD
  KC_BOOTSTRAP_ADMIN_USERNAME
  KC_BOOTSTRAP_ADMIN_PASSWORD
  PGADMIN_PASSWORD
  CYPRESS_RESET_DATABASE
  CYPRESS_EXECUTION_ENVIRONMENT
  CYPRESS_TEST_GROUP
  CYPRESS_SINGLE_POPULATE
  CYPRESS_RUN_PREPOPULATION
  RABBITMQ_USER
  RABBITMQ_PASS
  RABBITMQ_PASS_HASH
  LOGGING_OPTIONS_MAX_SIZE
  LOGGING_OPTIONS_MAX_FILE
  GRAFANA_PASSWORD
  LOKI_VOLUME
  SLACK_ALERT_URL
  SLACK_CRITICAL_ALERT_URL
)

env_args=()
for env_var in "${forwarded_env_vars[@]}"; do
  env_args+=(-e "$env_var")
done

mkdir -p ./dataland-frontend/cypress

docker run --rm --network host \
  -v "$(pwd)/.git:/app/.git" \
  -v "$(pwd)/dataland-frontend/cypress:/app/dataland-frontend/cypress" \
  "${env_args[@]}" \
  --workdir /app/dataland-frontend \
  --entrypoint npm \
  "$e2etests_image" \
  run testgithubenvironment --url="https://${TARGETSERVER_URL}"
