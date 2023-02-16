#!/bin/bash
set -euxo pipefail

function getOpenApiSha1Sum() {
  ls
  find * -name "*OpenApi.json" -type f -exec bash -c 'jq -S . $1 > $1' shell {} \;
  ls
  find * -name "*OpenApi.jsonFormatted.json" -type f | \
  sort -u | \
  xargs sha1sum | \
  awk '{print $1}' | \
  sha1sum | \
  awk '{print $1}'
}

sha1SumBeforeRegenerate=$(getOpenApiSha1Sum)
./gradlew generateOpenApiDocs --no-daemon --stacktrace
sha1SumAfterRegenerate=$(getOpenApiSha1Sum)
echo "sha1sum before regenerate: $sha1SumBeforeRegenerate"
echo "sha1sum after regenerate: $sha1SumAfterRegenerate"

if [[ "$sha1SumBeforeRegenerate" == "$sha1SumAfterRegenerate" ]]; then
  echo "OpenApi Files OK!"
  exit 0
else
  echo "OpenApi Files not OK!"
  exit 1
fi
