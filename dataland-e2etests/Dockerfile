FROM ghcr.io/d-fine/dataland/cypressbase:latest

COPY . /app/
EXPOSE 8080
WORKDIR /app

ENTRYPOINT ["/bin/sh","-c"]
CMD ["/app/testing-shell-scripts/wait_for_backend_and_run_e2etests.sh"]
