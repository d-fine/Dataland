#!/usr/bin/env bash
# Entrypoint for the dala-ci-runner Docker image.
#
# Starts a private Docker-in-Docker (DinD) daemon inside the container, downloads and
# registers the latest GitHub Actions self-hosted runner, and runs it in the foreground.
# On SIGTERM/SIGINT (i.e. `docker stop`), the runner is cleanly unregistered from GitHub
# before the container exits.
#
# Required environment variables:
#   RUNNER_URL     GitHub repository or organization URL
#   RUNNER_TOKEN   Runner registration token
#   RUNNER_NAME    Name to register this runner under (e.g. dala-ci-runner-1)
#   RUNNER_LABELS  Comma-separated labels (e.g. dala-ci-runner)

set -euo pipefail

RUNNER_DIR="$HOME/actions-runner"
DOCKERD_LOG="$HOME/dockerd.log"
DOCKERD_PID=""
RUNNER_CONFIGURED=0

for var in RUNNER_URL RUNNER_TOKEN RUNNER_NAME RUNNER_LABELS; do
  if [[ -z "${!var:-}" ]]; then
    echo "Error: environment variable $var is required." >&2
    exit 1
  fi
done

# ---------------------------------------------------------------------------
# Clean shutdown: unregister from GitHub and stop the inner Docker daemon.
# ---------------------------------------------------------------------------
cleanup() {
  echo ""
  echo "Received termination signal, shutting down..."

  if [[ "$RUNNER_CONFIGURED" -eq 1 && -f "$RUNNER_DIR/config.sh" ]]; then
    echo "Unregistering runner from GitHub..."
    "$RUNNER_DIR/config.sh" remove --token "$RUNNER_TOKEN" \
      || echo "Warning: runner unregistration failed (may already be removed)."
  fi

  if [[ -n "$DOCKERD_PID" ]] && kill -0 "$DOCKERD_PID" 2>/dev/null; then
    echo "Stopping inner Docker daemon (pid $DOCKERD_PID)..."
    sudo kill "$DOCKERD_PID" 2>/dev/null || true
    wait "$DOCKERD_PID" 2>/dev/null || true
  fi

  exit 0
}
trap cleanup SIGTERM SIGINT

# ---------------------------------------------------------------------------
# Start the inner Docker daemon (Docker-in-Docker).
# ---------------------------------------------------------------------------
echo "Starting inner Docker daemon..."
sudo -- sh -c "dockerd > '$DOCKERD_LOG' 2>&1 &"

READY=0
for _ in $(seq 1 30); do
  if docker info > /dev/null 2>&1; then
    READY=1
    break
  fi
  sleep 1
done

if [[ "$READY" -ne 1 ]]; then
  echo "Error: inner Docker daemon did not become ready in time. Log output:" >&2
  cat "$DOCKERD_LOG" >&2 || true
  exit 1
fi

DOCKERD_PID=$(pgrep -x dockerd | head -1 || true)
echo "Inner Docker daemon is ready."

# ---------------------------------------------------------------------------
# Download the latest GitHub Actions runner release (unless already present,
# e.g. after a container restart where the home directory was preserved).
# ---------------------------------------------------------------------------
if [[ ! -f "$RUNNER_DIR/run.sh" ]]; then
  echo "Fetching latest GitHub Actions runner version..."
  LATEST_TAG=$(curl -fsSL https://api.github.com/repos/actions/runner/releases/latest \
    | grep '"tag_name"' | head -1 | sed 's/.*"tag_name": "v\([^"]*\)".*/\1/')

  if [[ -z "$LATEST_TAG" ]]; then
    echo "Error: Could not determine latest runner version from GitHub API." >&2
    exit 1
  fi
  echo "Latest runner version: $LATEST_TAG"

  TARBALL="actions-runner-linux-x64-${LATEST_TAG}.tar.gz"
  TARBALL_PATH="/tmp/$TARBALL"
  DOWNLOAD_URL="https://github.com/actions/runner/releases/download/v${LATEST_TAG}/${TARBALL}"

  echo "Downloading runner tarball..."
  curl -fsSL -o "$TARBALL_PATH" "$DOWNLOAD_URL"

  mkdir -p "$RUNNER_DIR"
  tar -xzf "$TARBALL_PATH" -C "$RUNNER_DIR"
  rm -f "$TARBALL_PATH"
else
  echo "Runner binary already present at $RUNNER_DIR, skipping download."
fi

# ---------------------------------------------------------------------------
# Register the runner with GitHub (unless already configured, e.g. restart).
# ---------------------------------------------------------------------------
if [[ ! -f "$RUNNER_DIR/.runner" ]]; then
  echo "Registering runner '$RUNNER_NAME' with labels '$RUNNER_LABELS'..."
  "$RUNNER_DIR/config.sh" \
    --unattended \
    --url "$RUNNER_URL" \
    --token "$RUNNER_TOKEN" \
    --name "$RUNNER_NAME" \
    --labels "$RUNNER_LABELS" \
    --replace
else
  echo "Runner already registered, skipping config.sh."
fi
RUNNER_CONFIGURED=1

# ---------------------------------------------------------------------------
# Run the runner in the foreground, in the background so the trap can fire.
# ---------------------------------------------------------------------------
echo "Starting runner..."
"$RUNNER_DIR/run.sh" &
RUNNER_PID=$!
wait "$RUNNER_PID"
