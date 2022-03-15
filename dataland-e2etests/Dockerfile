FROM ghcr.io/d-fine/dataland/cypressbase:latest

COPY . /app/
# COPY <path to .gradle on host system, which is $HOME/.gradle> /root/
EXPOSE 8080
WORKDIR /app

ENTRYPOINT ["/bin/sh","-c"]
CMD ["/app/testing/wait_for_backend_and_run_e2etests.sh"]
