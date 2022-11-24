#!/bin/bash

function getOpenApiSha1Sum() {
  find * -name "*OpenApi.json" -type f | \
  sort -u | \
  xargs sha1sum | \
  awk '{print $1}' | \
  sha1sum | \
  awk '{print $1}'
}

sha1SumBeforeRegenerate=$(getOpenApiSha1Sum)
./gradlew generateOpenApiDocs
sha1SumAfterRegenerate=$(getOpenApiSha1Sum)

if [[ "$sha1SumBeforeRegenerate" == "$sha1SumAfterRegenerate" ]]; then
  echo "apiKey Files OK!"
  exit 0
else
  echo "apiKey Files not OK!"
  exit 1
fi