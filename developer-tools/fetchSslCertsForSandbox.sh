#!/usr/bin/env bash
set -euo pipefail

SANDBOX_NAME="dataland"
REPO_PATH_IN_SANDBOX="${1:-$(pwd)}"

if ! command -v sbx >/dev/null 2>&1; then
  echo "sbx is required on the host machine to copy files into the sandbox." >&2
  exit 1
fi

CERT_DIR="./local/certs"
mkdir -p "$CERT_DIR"

echo "Fetching SSL certificate files from letsencrypt.dataland.com via scp..."
scp ubuntu@letsencrypt.dataland.com:/etc/letsencrypt/live/local-dev.dataland.com/* "$CERT_DIR"

echo "Copying certificate files into sandbox '$SANDBOX_NAME' at $REPO_PATH_IN_SANDBOX/local/certs..."
sbx cp "$CERT_DIR" "$SANDBOX_NAME:$REPO_PATH_IN_SANDBOX/local"

echo "Done. SSL certificates are now available inside the sandbox at $REPO_PATH_IN_SANDBOX/local/certs."
