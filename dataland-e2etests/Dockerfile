ARG DATALAND_E2ETESTS_CORE_VERSION
FROM ghcr.io/d-fine/dataland/dataland_e2etests_core:${DATALAND_E2ETESTS_CORE_VERSION}
ARG GITHUB_USER
ARG GITHUB_TOKEN

COPY ./gradle/ /app/gradle/
COPY ./build.gradle.kts ./gradle.properties ./settings.gradle.kts ./gradlew /app/
COPY ./dataland-backend/ /app/dataland-backend
COPY ./.git/ /app/.git
COPY ./dataland-frontend/tests /app/dataland-frontend/tests
COPY ./dataland-frontend/src /app/dataland-frontend/src
COPY ./dataland-frontend/* /app/dataland-frontend/
COPY ./dataland-e2etests/ /app/dataland-e2etests
COPY ./testing/ /app/testing

WORKDIR /app
RUN GITHUB_USER=$GITHUB_USER GITHUB_TOKEN=$GITHUB_TOKEN ./gradlew :dataland-e2etests:compileTestKotlin :dataland-frontend:generateAPIClientFrontend :dataland-frontend:npmInstall :dataland-frontend:npm_run_checkcypresscompilation --no-daemon --stacktrace && rm -rf /root/.npm /root/.gradle

ENTRYPOINT ["/bin/sh","-c"]
CMD ["/app/testing/wait_for_backend_and_run_e2etests.sh"]
