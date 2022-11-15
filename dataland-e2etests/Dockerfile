ARG DATALAND_E2ETESTS_CORE_VERSION
FROM ghcr.io/d-fine/dataland/dataland_e2etests_core:${DATALAND_E2ETESTS_CORE_VERSION}
ARG GITHUB_USER
ARG GITHUB_TOKEN

WORKDIR /app
COPY ./dataland-backend/ ./dataland-backend
COPY ./dataland-frontend/ ./dataland-frontend
COPY ./dataland-e2etests/ ./dataland-e2etests
COPY ./testing/ ./testing
COPY ./.git/ ./.git

RUN GITHUB_USER=$GITHUB_USER GITHUB_TOKEN=$GITHUB_TOKEN ./gradlew :dataland-e2etests:compileTestKotlin :dataland-frontend:generateAPIClientFrontend :dataland-frontend:npmInstall :dataland-frontend:npm_run_checkcypresscompilation --no-daemon --stacktrace && rm -rf /root/.npm /root/.gradle

ENTRYPOINT ["/bin/sh","-c"]
CMD ["/app/testing/wait_for_backend_and_run_e2etests.sh"]
