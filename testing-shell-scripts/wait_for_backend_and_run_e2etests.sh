#!/bin/sh
# This script begins compiling the Kotlin code of the e2etests-project, so the time that the backend-container
# and the skyminder-dummy-server-container need to be up and responding is used efficiently.
# Then it continuously validates, whether the backend-container is running.
# Then it continuously validates, whether the skyminder-dummy-server-container is running.
# Only then it executes the end-to-end tests.

set -ex
./gradlew compileKotlin
timeout 240 sh -c "while ! curl http://proxy:80/api/actuator/health/ping 2>/dev/null | grep -q UP; do echo 'backend server not yet there - retrying in 1s'; sleep 1; done; echo 'backend server responded'"
timeout 1500 sh -c "while ! curl http://proxy:80/api/actuator/health/skyminderDummyServer 2>/dev/null | grep -q UP; do echo 'skyminder dummy server not yet there - retrying in 1s'; sleep 1; done; echo 'skyminder dummy server responded'"
./gradlew :dataland-e2etests:test :dataland-frontend:npm_run_testpipeline