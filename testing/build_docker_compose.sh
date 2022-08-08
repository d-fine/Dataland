#!/bin/sh
./gradlew dataland-keycloak:dataland_theme:login:buildTheme --no-daemon --stacktrace
docker-compose --project-name dala-e2e-test --profile testing pull
docker-compose --project-name dala-e2e-test --profile testing build