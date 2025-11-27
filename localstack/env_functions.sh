#!/usr/bin/env bash

load_dev_environment() {
  set -o allexport
  source ./environments/.env.dev
  set +o allexport
}

source_github_env_log() {
  set -o allexport
  source ./*github_env.log
  set +o allexport
}

source_uncritical_environment() {
  set -o allexport
  source ./environments/.env.uncritical
  set +o allexport
}
