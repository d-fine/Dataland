ARG DATALAND_E2ETESTS_CORE_VERSION
FROM ghcr.io/d-fine/dataland/dataland_e2etests_core:${DATALAND_E2ETESTS_CORE_VERSION}

WORKDIR /app
COPY ./dataland-backend/ ./dataland-backend
COPY ./dataland-frontend/ ./dataland-frontend
COPY ./dataland-e2etests/ ./dataland-e2etests
COPY ./testing/ ./testing
COPY ./.git/ ./.git

RUN --mount=type=secret,id=DOCKER_SECRET,target=/root/.gradle/gradle.properties,required=true \
     ./gradlew :dataland-e2etests:compileTestKotlin :dataland-e2etests:testClasses :dataland-frontend:generateAPIClientFrontend :dataland-frontend:npmInstall :dataland-frontend:npm_run_checkcypresscompilation --no-daemon --stacktrace && \
     rm -rf /root/.npm

ENTRYPOINT ["/bin/sh","-c"]
CMD ["/app/testing/wait_for_backend_and_run_e2etests.sh"]
