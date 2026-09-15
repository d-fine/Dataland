#!/usr/bin/env bash
set -euo pipefail

sandbox_name="dataland"
repo_path_in_sandbox="${1:-$(pwd)}"

if ! command -v sbx >/dev/null 2>&1; then
  echo "sbx is required on the host machine to copy files into the sandbox." >&2
  exit 1
fi

cert_dir="./local/certs"
sandbox_dir="$repo_path_in_sandbox/local"
mkdir -p "$cert_dir"

echo "Fetching SSL certificate files from letsencrypt.dataland.com via scp..."
scp ubuntu@letsencrypt.dataland.com:/etc/letsencrypt/live/local-dev.dataland.com/* "$cert_dir"

echo "Copying certificate files into sandbox '$sandbox_name' at $sandbox_dir..."
sbx exec -u agent $sandbox_name mkdir -p $cert_dir
sbx cp "$cert_dir" "$sandbox_name:$sandbox_dir"

for file in $cert_dir/*; do
  echo "Changing ownership for $(basename $file)"
  sbx exec $sandbox_name sudo chown agent:agent $cert_dir/$(basename $file)
done

echo "Done. SSL certificates are now available inside the sandbox at $repo_path_in_sandbox/local/certs."
