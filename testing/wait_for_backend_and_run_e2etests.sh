#!/bin/sh
# This script continuously validates, whether the backend-container is running.
# Then it continuously validates, whether the skyminder-dummy-server-container is running.
# Only then it executes the end-to-end tests.

set -ex
timeout 240 sh -c "while ! curl http://proxy:80/api/actuator/health/ping 2>/dev/null | grep -q UP; do echo 'backend server not yet there - retrying in 1s'; sleep 1; done; echo 'backend server responded'"
timeout 240 sh -c "while ! curl http://proxy:80/api/actuator/health/skyminderDummyServer 2>/dev/null | grep -q UP; do echo 'skyminder dummy server not yet there - retrying in 1s'; sleep 1; done; echo 'skyminder dummy server responded'"
./gradlew :dataland-e2etests:test :dataland-frontend:npm_run_testpipeline --no-daemon