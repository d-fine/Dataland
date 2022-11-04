FROM ghcr.io/d-fine/dataland/cypressbase:latest

COPY . /app/
EXPOSE 8080
WORKDIR /app
RUN ./gradlew :dataland-e2etests:compileTestKotlin :dataland-frontend:generateAPIClientFrontend :dataland-frontend:npmInstall :dataland-frontend:npm_run_checkcypresscompilation --no-daemon --stacktrace

ENTRYPOINT ["/bin/sh","-c"]
CMD ["/app/testing/wait_for_backend_and_run_e2etests.sh"]
