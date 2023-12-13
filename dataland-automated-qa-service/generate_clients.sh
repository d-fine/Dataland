#!/usr/bin/env bash

set -euxo pipefail

PROJECT_ROOT="$(dirname "$0")"/..
SUBPROJECT_ROOT="$PROJECT_ROOT"/dataland-automated-qa-service

echo Building clients
openapi-python-client generate --path "$PROJECT_ROOT"/dataland-internal-storage/internalStorageOpenApi.json
openapi-python-client generate --path "$PROJECT_ROOT"/dataland-backend/backendOpenApi.json

echo Reconstructing build directory
rm -r "$SUBPROJECT_ROOT"/build
mkdir -p "$SUBPROJECT_ROOT"/build/clients
touch "$SUBPROJECT_ROOT"/build/clients/__init__.py
echo Moving clients into build directory
mv "$SUBPROJECT_ROOT"/dataland-backend-api-documentation-client/dataland_backend_api_documentation_client "$SUBPROJECT_ROOT"/build/clients
mv "$SUBPROJECT_ROOT"/dataland-internal-storage-api-documentation-client/dataland_internal_storage_api_documentation_client "$SUBPROJECT_ROOT"/build/clients
rm -r "$SUBPROJECT_ROOT"/dataland-backend-api-documentation-client
rm -r "$SUBPROJECT_ROOT"/dataland-internal-storage-api-documentation-client
