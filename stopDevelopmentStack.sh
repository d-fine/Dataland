#!/usr/bin/env bash
set -euxo pipefail

docker compose --profile development --profile developmentContainerFrontend down
