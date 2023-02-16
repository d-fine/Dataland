#!/bin/bash
set -euxo pipefail

mkdir formatted
find * -name "*OpenApi.json" -type f -exec bash -c 'mv -t formatted $1' shell {} \;
./gradlew generateOpenApiDocs --no-daemon --stacktrace
find * -name "*OpenApi.json" -type f -exec bash -c 'jd -set $1 formatted/$1' shell {} \;
