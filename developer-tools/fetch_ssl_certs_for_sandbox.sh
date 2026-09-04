#!/usr/bin/env bash
# Run this script OUTSIDE the docker sandbox (i.e. on your own machine, where your
# SSH agent/private key that is authorized on letsencrypt.dataland.com is available).
#
# It fetches the local-dev SSL certificate/key files from the letsencrypt server via scp
# (the same way localstack/cert_functions.sh::retrieve_ssl_certificates used to do it),
# and then copies the retrieved files into the running "dataland" sandbox via `sbx cp`,
# so the sandbox does not need any SSH key of its own.
#
# Usage:
#   ./developer-tools/fetch_ssl_certs_for_sandbox.sh [path-to-repo-in-sandbox]
#
# Example:
#   ./developer-tools/fetch_ssl_certs_for_sandbox.sh
#   ./developer-tools/fetch_ssl_certs_for_sandbox.sh /home/user/Dataland-sandbox

set -euo pipefail

SANDBOX_NAME="dataland"
REPO_PATH_IN_SANDBOX="${1:-$(pwd)}"

if ! command -v sbx >/dev/null 2>&1; then
  echo "sbx is required on the host machine to copy files into the sandbox." >&2
  exit 1
fi

TMP_BASE_DIR="$(mktemp -d)"
trap 'rm -rf "$TMP_BASE_DIR"' EXIT
TMP_CERT_DIR="$TMP_BASE_DIR/certs"
mkdir -p "$TMP_CERT_DIR"

echo "Fetching SSL certificate files from letsencrypt.dataland.com via scp..."
scp ubuntu@letsencrypt.dataland.com:/etc/letsencrypt/live/local-dev.dataland.com/* "$TMP_CERT_DIR"

DEST_PARENT_DIR="$REPO_PATH_IN_SANDBOX/local"

echo "Copying certificate files into sandbox '$SANDBOX_NAME' at $DEST_PARENT_DIR/certs..."
sbx cp "$TMP_CERT_DIR" "$SANDBOX_NAME:$DEST_PARENT_DIR"

echo "Done. SSL certificates are now available inside the sandbox at $DEST_PARENT_DIR/certs."
