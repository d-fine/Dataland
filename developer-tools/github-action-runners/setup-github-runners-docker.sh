#!/usr/bin/env bash
# Sets up multiple GitHub Actions self-hosted runners, each running in its own
# Docker container with a private Docker-in-Docker (DinD) daemon.
#
# This avoids the port-443 conflict that occurs when multiple runners run directly
# on the host: each runner's Docker containers (including the e2e-tests inbound
# proxy) live inside that runner's own isolated Docker daemon.
#
# Usage:
#   ./setup-github-runners-docker.sh --url <github-repo-or-org-url> --token <registration-token> [--count <n>] [--start <n>] [--rebuild]
#
# Arguments:
#   --url      GitHub repository or organization URL (e.g. https://github.com/d-fine/dataland)
#   --token    Runner registration token (obtain from: Settings → Actions → Runners → New self-hosted runner)
#   --count    Number of runner instances to create (default: 4)
#   --start    Index to start numbering runners from (default: auto-detected as next available index)
#   --rebuild  Force a rebuild of the dala-ci-runner Docker image even if it already exists
#
# Each runner is a container named "dala-ci-runner-<n>", registered as a systemd
# service of the same name, running with the label "dala-ci-runner". Existing
# containers are skipped.
#
# Requires: docker, systemd, sudo.

set -euo pipefail

LABEL="dala-ci-runner"
IMAGE="dala-ci-runner:latest"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
COUNT=4
START=""
URL=""
TOKEN=""
REBUILD=0

# ---------------------------------------------------------------------------
# Argument parsing
# ---------------------------------------------------------------------------
while [[ $# -gt 0 ]]; do
  case "$1" in
    --url)
      URL="$2"
      shift 2
      ;;
    --token)
      TOKEN="$2"
      shift 2
      ;;
    --count)
      COUNT="$2"
      shift 2
      ;;
    --start)
      START="$2"
      shift 2
      ;;
    --rebuild)
      REBUILD=1
      shift
      ;;
    *)
      echo "Unknown argument: $1"
      echo "Usage: $0 --url <github-url> --token <registration-token> [--count <n>] [--start <n>] [--rebuild]"
      exit 1
      ;;
  esac
done

if [[ -z "$URL" || -z "$TOKEN" ]]; then
  echo "Error: --url and --token are required."
  echo "Usage: $0 --url <github-url> --token <registration-token> [--count <n>] [--start <n>] [--rebuild]"
  exit 1
fi

if ! [[ "$COUNT" =~ ^[1-9][0-9]*$ ]]; then
  echo "Error: --count must be a positive integer."
  exit 1
fi

if [[ -n "$START" ]] && ! [[ "$START" =~ ^[1-9][0-9]*$ ]]; then
  echo "Error: --start must be a positive integer."
  exit 1
fi

if ! command -v docker > /dev/null 2>&1; then
  echo "Error: docker is required on the host but was not found."
  exit 1
fi

# ---------------------------------------------------------------------------
# Auto-detect start index if not provided
# ---------------------------------------------------------------------------
if [[ -z "$START" ]]; then
  START=1
  while docker ps -a --format '{{.Names}}' | grep -qx "${LABEL}-${START}"; do
    START=$(( START + 1 ))
  done
  echo "Auto-detected start index: $START"
fi

END=$(( START + COUNT - 1 ))

# ---------------------------------------------------------------------------
# Build the runner image (contains Docker/DinD + CI tooling; runner binary
# itself is downloaded by entrypoint.sh at container start).
# ---------------------------------------------------------------------------
if [[ "$REBUILD" -eq 1 ]] || ! docker image inspect "$IMAGE" > /dev/null 2>&1; then
  echo "Building Docker image $IMAGE from $SCRIPT_DIR ..."
  docker build -t "$IMAGE" "$SCRIPT_DIR"
else
  echo "Docker image $IMAGE already exists, skipping build (use --rebuild to force)."
fi

# ---------------------------------------------------------------------------
# Create and start each runner container + systemd unit
# ---------------------------------------------------------------------------
SKIPPED=()
CREATED=()

for n in $(seq "$START" "$END"); do
  CONTAINER_NAME="${LABEL}-${n}"
  SERVICE_NAME="${CONTAINER_NAME}.service"
  SERVICE_PATH="/etc/systemd/system/${SERVICE_NAME}"

  if docker ps -a --format '{{.Names}}' | grep -qx "$CONTAINER_NAME"; then
    echo "Runner $n: container $CONTAINER_NAME already exists — skipping."
    SKIPPED+=("$n")
    continue
  fi

  echo ""
  echo "=== Setting up runner $n as container $CONTAINER_NAME ==="

  docker run -d \
    --privileged \
    --name "$CONTAINER_NAME" \
    --restart unless-stopped \
    -e RUNNER_URL="$URL" \
    -e RUNNER_TOKEN="$TOKEN" \
    -e RUNNER_NAME="$CONTAINER_NAME" \
    -e RUNNER_LABELS="$LABEL" \
    "$IMAGE" > /dev/null

  echo "Writing systemd unit $SERVICE_PATH ..."
  sudo tee "$SERVICE_PATH" > /dev/null <<EOF
[Unit]
Description=GitHub Actions Runner ($CONTAINER_NAME)
After=docker.service
Requires=docker.service

[Service]
Restart=always
ExecStart=/usr/bin/docker start -a $CONTAINER_NAME
ExecStop=/usr/bin/docker stop $CONTAINER_NAME

[Install]
WantedBy=multi-user.target
EOF

  sudo systemctl daemon-reload
  sudo systemctl enable "$SERVICE_NAME"
  # The container is already running from `docker run` above; just make systemd aware of it.
  sudo systemctl start "$SERVICE_NAME" 2>/dev/null || true

  CREATED+=("$n")
  echo "Runner $n: container started and systemd service enabled."
done

# ---------------------------------------------------------------------------
# Summary
# ---------------------------------------------------------------------------
echo ""
echo "========================================"
echo "Setup complete"
echo "========================================"
echo "Created : ${CREATED[*]:-none}"
echo "Skipped : ${SKIPPED[*]:-none}"
echo ""
echo "Service status:"
for n in $(seq "$START" "$END"); do
  CONTAINER_NAME="${LABEL}-${n}"
  SERVICE_NAME="${CONTAINER_NAME}.service"
  if docker ps -a --format '{{.Names}}' | grep -qx "$CONTAINER_NAME"; then
    echo "  Runner $n ($SERVICE_NAME):"
    sudo systemctl status "$SERVICE_NAME" --no-pager -l 2>/dev/null | grep -E "Active:|Loaded:" | sed 's/^/    /' || echo "    (could not query status)"
  fi
done
