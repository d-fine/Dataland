#!/usr/bin/env bash
set -euxo pipefail

./gradlew :dataland-frontend:generateClients :dataland-frontend:npm_run_testcomponent --no-daemon --stacktrace 2>&1 | tee cypress-component-test-logs.log
