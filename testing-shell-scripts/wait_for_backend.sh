#!/bin/bash

#start compiling Kotlin
./gradlew compileKotlin
#Kotlin compiled, now checking if backend responds
timeout 240 bash -c "while ! curl http://backend:8080/actuator/health; do echo 'backend server not yet there - retrying in 1s'; sleep 1; done; echo 'backend server responded'" || exit
#backend responded, now starting end-to-end-tests
./gradlew :dataland-e2etests:test