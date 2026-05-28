FROM mcr.microsoft.com/devcontainers/base:ubuntu

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
