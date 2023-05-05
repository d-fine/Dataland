#!/bin/bash
set -euxo pipefail

set -o allexport
source ./*github_env.log
set +o allexport

docker compose --profile development down
