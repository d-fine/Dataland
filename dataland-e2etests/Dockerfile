ARG DATALAND_E2ETESTS_BASE_VERSION
FROM ghcr.io/d-fine/dataland/dataland_e2etests_base:${DATALAND_E2ETESTS_BASE_VERSION}

WORKDIR /app
COPY ./dataland-backend/backendOpenApi.json ./dataland-backend/backendOpenApi.json
COPY ./dataland-community-manager/communityManagerOpenApi.json ./dataland-community-manager/communityManagerOpenApi.json
COPY ./dataland-api-key-manager/apiKeyManagerOpenApi.json ./dataland-api-key-manager/apiKeyManagerOpenApi.json
COPY ./dataland-document-manager/documentManagerOpenApi.json ./dataland-document-manager/documentManagerOpenApi.json
COPY ./dataland-qa-service/qaServiceOpenApi.json ./dataland-qa-service/qaServiceOpenApi.json
COPY ./dataland-email-service/emailServiceOpenApi.json ./dataland-email-service/emailServiceOpenApi.json
COPY ./dataland-user-service/userServiceOpenApi.json ./dataland-user-service/userServiceOpenApi.json
COPY ./dataland-frontend/ ./dataland-frontend
COPY ./dataland-backend-utils/ ./dataland-backend-utils/
COPY ./dataland-e2etests/ ./dataland-e2etests
COPY ./testing/ ./testing
COPY ./.git/ ./.git

RUN --mount=type=secret,id=DOCKER_SECRET,target=/root/.gradle/gradle.properties,required=true \
     ./gradlew :dataland-e2etests:compileTestKotlin :dataland-e2etests:testClasses :dataland-frontend:generateClients :dataland-frontend:npmInstall --no-daemon --stacktrace && \
     rm -rf /root/.npm

ENTRYPOINT ["/bin/sh","-c"]
CMD ["/app/testing/wait_for_backend_and_run_e2etests.sh"]
