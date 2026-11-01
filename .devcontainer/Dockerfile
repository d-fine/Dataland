FROM mcr.microsoft.com/devcontainers/base:ubuntu-24.04@sha256:d94c97dd9cacf183d0a6fd12a8e87b526e9e928307674ae9c94139139c0c6eae

RUN apt-get update \
    && DEBIAN_FRONTEND=noninteractive apt-get install -y \
        libgtk-3-0 \
        libgbm-dev \
        libnotify-dev \
        libnss3 \
        libxss1 \
        libasound2t64 \
        libxtst6 \
        xauth \
        xvfb \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/*

RUN mkdir -p /home/vscode/.local/share/opencode \
             /home/vscode/.local/state/opencode \
             /home/vscode/.cache/Cypress \
    && chown -R vscode:vscode /home/vscode/.local /home/vscode/.cache
