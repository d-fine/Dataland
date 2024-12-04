#!/usr/bin/env bash
set -euxo pipefail

# This script is used to verify that there are no uncommitted changes in the OpenApi files.

find . -name "*OpenApi.json" -type f -exec bash -c 'jq -S . $1 > $1.formatted-before.json' shell {} \;
./gradlew generateOpenApiDocs --no-daemon --stacktrace
find . -name "*OpenApi.json" -type f -exec bash -c 'jq -S . $1 > $1.formatted-after.json' shell {} \;

error=0
broken_files=""
while IFS= read -r -d '' file
do
  hash_before=$(sha1sum "$file" | awk '{ print $1 }' )
  hash_after=$(sha1sum "${file/.formatted-before.json/.formatted-after.json}" | awk '{ print $1 }' )
  if [[ "$hash_before" != "$hash_after" ]]; then
    echo "OpenApi Files do not match for $file!"
    error=$((error+1))
    broken_files+="$file "
  fi
done < <(find . -name "*OpenApi.json.formatted-before.json" -type f -print0)

if [[ $error -ne 0 ]]; then
  echo "There are $error OpenApi files that do not match: $broken_files."

  exit 1
fi