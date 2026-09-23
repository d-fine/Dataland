# Docker image for a single GitHub Actions self-hosted runner, running its own
# Docker-in-Docker (DinD) daemon. Each container is a fully isolated runner so that
# per-runner services (e.g. the e2e-tests inbound proxy binding to port 443) never
# collide with the same service running in a sibling runner container.
#
# The runner binary itself is NOT baked into the image: entrypoint.sh downloads the
# latest release at container start, so the image only needs to be rebuilt when the
# pre-installed tooling below changes.
#
# Built and run by setup-github-runners-docker.sh.

FROM ubuntu:22.04

ENV DEBIAN_FRONTEND=noninteractive

# ---------------------------------------------------------------------------
# Base tooling required by the e2e-tests job (see .github/workflows/CI.yaml):
#   - Docker Engine + Compose v2 plugin (also used as the DinD daemon)
#   - postgresql-client for pg_isready
#   - curl, git, jq, ca-certificates, openssh-client for general scripting
#   - sudo (configured passwordless below) for steps like `sudo rm -rf`
# ---------------------------------------------------------------------------
RUN apt-get update \
    && apt-get install -y --no-install-recommends \
       ca-certificates \
       curl \
       git \
       jq \
       openssh-client \
       postgresql-client \
       procps \
       sudo \
       tar \
       gnupg \
       lsb-release \
       # Runtime dependencies of the .NET-based GitHub Actions runner binary
       libicu70 \
       liblttng-ust1 \
       libkrb5-3 \
       libssl3 \
       zlib1g \
    && install -m 0755 -d /etc/apt/keyrings \
    && curl -fsSL https://download.docker.com/linux/ubuntu/gpg -o /etc/apt/keyrings/docker.asc \
    && chmod a+r /etc/apt/keyrings/docker.asc \
    && echo \
       "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.asc] https://download.docker.com/linux/ubuntu \
       $(lsb_release -cs) stable" | tee /etc/apt/sources.list.d/docker.list > /dev/null \
    && apt-get update \
    && apt-get install -y --no-install-recommends \
       docker-ce \
       docker-ce-cli \
       containerd.io \
       docker-buildx-plugin \
       docker-compose-plugin \
    && rm -rf /var/lib/apt/lists/*

# ---------------------------------------------------------------------------
# Create an unprivileged runner user with passwordless sudo (the e2e-tests job
# runs `sudo rm -rf ./dataland-loki/data`), and add it to the docker group.
# ---------------------------------------------------------------------------
RUN useradd --create-home --shell /bin/bash runner \
    && usermod -aG docker runner \
    && echo "runner ALL=(ALL) NOPASSWD:ALL" > /etc/sudoers.d/runner \
    && chmod 0440 /etc/sudoers.d/runner

COPY --chmod=0755 entrypoint.sh /entrypoint.sh

USER runner
WORKDIR /home/runner

ENTRYPOINT ["/entrypoint.sh"]
