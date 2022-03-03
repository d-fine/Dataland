#!/bin/sh
# This script begins compiling the Kotlin code of the e2etests-project, so the time that the backend-container
# and the skyminder-dummy-server-container need to be up and responding is used efficiently.
# Then it continuously validates, whether the backend-container is running.
# Then it continuously validates, whether the skyminder-dummy-server-container is running.
# Only then it executes the end-to-end tests.

set -ex
./gradlew compileKotlin
timeout 240 sh -c "while ! wget http://proxy:80/api/actuator/health; do echo 'backend server not yet there - retrying in 1s'; sleep 1; done; rm health; echo 'backend server responded'"
timeout 120 sh -c "while ! wget http://proxy:80/skyminder-dummy-server/actuator/health; do echo 'skyminder dummy server not yet there - retrying in 1s'; sleep 1; done; rm health; echo 'skyminder dummy server responded'"
./gradlew :dataland-e2etests:test --no-daemon
./gradlew :dataland-frontend:npm_run_testproductive --no-daemon