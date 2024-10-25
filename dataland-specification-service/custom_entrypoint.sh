#!/usr/bin/env bash
set -euxo pipefail

while IFS= read -r -d '' file;
do
  envsubst < "$file" > "$file.tmp" && mv "$file.tmp" "$file"
done < <(find "/usr/share/nginx/html" -name "*.json" -print0)

nginx -g "daemon off;"
