#!/usr/bin/env bash
set -euxo pipefail

function getFormattedOpenApiSha1Sum() {
  find * -name "*OpenApi.json.formatted.json" -type f | \
  sort -u | \
  xargs sha1sum
}

function getOpenApiSha1Sum() {
  find * -name "*OpenApi.json" -type f -exec bash -c 'jq -S . $1 > $1.formatted.json' shell {} \;

  getFormattedOpenApiSha1Sum | \
  awk '{print $1}' | \
  sha1sum | \
  awk '{print $1}'
}

sha1SumBeforeRegenerate=$(getOpenApiSha1Sum)
./gradlew generateOpenApiDocs --no-daemon --stacktrace
sha1SumAfterRegenerate=$(getOpenApiSha1Sum)
echo "sha1sum before regenerate: $sha1SumBeforeRegenerate"
getFormattedOpenApiSha1Sum > formatbefore.txt

echo "sha1sum after regenerate: $sha1SumAfterRegenerate"
getFormattedOpenApiSha1Sum > formatafter.txt

if [[ "$sha1SumBeforeRegenerate" == "$sha1SumAfterRegenerate" ]]; then
  echo "OpenApi Files OK!"
  exit 0
else
  echo "OpenApi Files not OK!"
  exit 1
fi
