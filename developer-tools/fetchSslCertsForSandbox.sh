#!/usr/bin/env bash
set -euo pipefail

sandbox_name="dataland"
repo_path_in_sandbox="${1:-$(pwd)}"

if ! command -v sbx >/dev/null 2>&1; then
  echo "sbx is required on the host machine to copy files into the sandbox." >&2
  exit 1
fi

cert_dir="./local/certs"
mkdir -p "$cert_dir"

echo "Fetching SSL certificate files from letsencrypt.dataland.com via scp..."
scp ubuntu@letsencrypt.dataland.com:/etc/letsencrypt/live/local-dev.dataland.com/* "$cert_dir"

echo "Copying certificate files into sandbox '$sandbox_name' at $repo_path_in_sandbox/local/certs..."
sbx cp "$cert_dir" "$sandbox_name:$repo_path_in_sandbox/local"

echo "Done. SSL certificates are now available inside the sandbox at $repo_path_in_sandbox/local/certs."
