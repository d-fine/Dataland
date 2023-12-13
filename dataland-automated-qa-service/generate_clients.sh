#!/usr/bin/env bash

set -euxo pipefail

PROJECT_ROOT="$(dirname "$0")"/..
SUBPROJECT_ROOT="$PROJECT_ROOT"/dataland-automated-qa-service

echo Building clients
openapi-python-client generate --path "$PROJECT_ROOT"/dataland-internal-storage/internalStorageOpenApi.json
openapi-python-client generate --path "$PROJECT_ROOT"/dataland-backend/backendOpenApi.json

echo Reconstructing build directory
rm -r "$SUBPROJECT_ROOT"/build || true
mkdir -p "$SUBPROJECT_ROOT"/build/clients
echo Moving clients into build directory
mv dataland-backend-api-documentation-client "$SUBPROJECT_ROOT"/build/clients
mv dataland-internal-storage-api-documentation-client "$SUBPROJECT_ROOT"/build/clients
