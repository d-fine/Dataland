FROM cypress/base:18.12.0
ARG GITHUB_USER
ARG GITHUB_TOKEN
RUN apt-get update && \
    apt-get install -y wget curl apt-transport-https gnupg && \
    wget -O - https://packages.adoptium.net/artifactory/api/gpg/key/public | apt-key add - && echo "deb https://packages.adoptium.net/artifactory/deb $(awk -F= '/^VERSION_CODENAME/{print$2}' /etc/os-release) main" | tee /etc/apt/sources.list.d/adoptium.list && \
    apt-get remove -y wget curl apt-transport-https gnupg && \
    apt-get update && \
    apt-get install -y temurin-17-jdk && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

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
RUN GITHUB_USER=$GITHUB_USER GITHUB_TOKEN=$GITHUB_TOKEN ./gradlew :dataland-e2etests:compileTestKotlin :dataland-frontend:generateAPIClientFrontend :dataland-frontend:npmInstall :dataland-frontend:npm_run_checkcypresscompilation --no-daemon --stacktrace && rm -rf /root/.npm /root/.gradle /usr/local/lib/node_modules

ENTRYPOINT ["/bin/sh","-c"]
CMD ["/app/testing/wait_for_backend_and_run_e2etests.sh"]
