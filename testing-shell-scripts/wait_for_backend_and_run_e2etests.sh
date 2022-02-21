#!/bin/sh

#This script begins compiling the Kotlin code of the e2etests-project, so the time that the backend-container needs to be up and responding is used efficiently.
#Then it continuously validates, whether the backend-container is running. Only then it executes the end-to-end tests.

./gradlew compileKotlin
timeout 240 sh -c "while ! wget http://backend:8080/actuator/health; do echo 'backend server not yet there - retrying in 1s'; sleep 1; done; echo 'backend server responded'" || exit
./gradlew :dataland-e2etests:test