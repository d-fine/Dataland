FROM cypress/included:9.5.1
RUN apt-get update && \
    apt-get install -y wget apt-transport-https gnupg && \
    wget -O - https://packages.adoptium.net/artifactory/api/gpg/key/public | apt-key add - && echo "deb https://packages.adoptium.net/artifactory/deb $(awk -F= '/^VERSION_CODENAME/{print$2}' /etc/os-release) main" | tee /etc/apt/sources.list.d/adoptium.list && \
    apt-get update && \
    apt-get install -y temurin-17-jdk

COPY . /app/
EXPOSE 8080
WORKDIR /app

ENTRYPOINT ["/bin/sh","-c"]
CMD ["/app/testing-shell-scripts/wait_for_backend_and_run_e2etests.sh"]
