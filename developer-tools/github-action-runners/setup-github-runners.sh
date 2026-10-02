#!/usr/bin/env bash
# Sets up multiple GitHub Actions self-hosted runner instances as systemd services.
#
# Usage:
#   ./setup-github-runners.sh --url <github-repo-or-org-url> --token <registration-token> [--count <n>] [--start <n>]
#
# Arguments:
#   --url     GitHub repository or organization URL (e.g. https://github.com/d-fine/dataland)
#   --token   Runner registration token (obtain from: Settings → Actions → Runners → New self-hosted runner)
#   --count   Number of runner instances to create (default: 4)
#   --start   Index to start numbering runners from (default: auto-detected as next available index)
#
# Each runner is installed in ~/actions-runner-<n> and registered as a systemd service
# running as the current user with the label "dala-ci-runner".
# Existing runner directories are skipped.

set -euo pipefail

LABEL="dala-ci-runner"
BASE_DIR="$HOME"
RUNNER_USER="$(whoami)"
COUNT=4
START=""
URL=""
TOKEN=""

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
    *)
      echo "Unknown argument: $1"
      echo "Usage: $0 --url <github-url> --token <registration-token> [--count <n>] [--start <n>]"
      exit 1
      ;;
  esac
done

if [[ -z "$URL" || -z "$TOKEN" ]]; then
  echo "Error: --url and --token are required."
  echo "Usage: $0 --url <github-url> --token <registration-token> [--count <n>] [--start <n>]"
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

# ---------------------------------------------------------------------------
# Auto-detect start index if not provided
# ---------------------------------------------------------------------------
if [[ -z "$START" ]]; then
  START=1
  while [[ -d "$BASE_DIR/actions-runner-$START" ]]; do
    START=$(( START + 1 ))
  done
  echo "Auto-detected start index: $START"
fi

END=$(( START + COUNT - 1 ))

# ---------------------------------------------------------------------------
# Detect latest runner version and download tarball
# ---------------------------------------------------------------------------
echo "Fetching latest GitHub Actions runner version..."
LATEST_TAG=$(curl -fsSL https://api.github.com/repos/actions/runner/releases/latest \
  | grep '"tag_name"' | head -1 | sed 's/.*"tag_name": "v\([^"]*\)".*/\1/')

if [[ -z "$LATEST_TAG" ]]; then
  echo "Error: Could not determine latest runner version from GitHub API."
  exit 1
fi

echo "Latest runner version: $LATEST_TAG"

TARBALL="actions-runner-linux-x64-${LATEST_TAG}.tar.gz"
TARBALL_PATH="/tmp/$TARBALL"
DOWNLOAD_URL="https://github.com/actions/runner/releases/download/v${LATEST_TAG}/${TARBALL}"

if [[ ! -f "$TARBALL_PATH" ]]; then
  echo "Downloading runner tarball to $TARBALL_PATH ..."
  curl -fsSL -o "$TARBALL_PATH" "$DOWNLOAD_URL"
else
  echo "Tarball already present at $TARBALL_PATH, skipping download."
fi

# ---------------------------------------------------------------------------
# Create and configure each runner instance
# ---------------------------------------------------------------------------
SKIPPED=()
CREATED=()

for n in $(seq "$START" "$END"); do
  RUNNER_DIR="$BASE_DIR/actions-runner-$n"

  if [[ -d "$RUNNER_DIR" ]]; then
    echo "Runner $n: directory $RUNNER_DIR already exists — skipping."
    SKIPPED+=("$n")
    continue
  fi

  echo ""
  echo "=== Setting up runner $n in $RUNNER_DIR ==="

  mkdir -p "$RUNNER_DIR"
  tar -xzf "$TARBALL_PATH" -C "$RUNNER_DIR"

  "$RUNNER_DIR/config.sh" \
    --unattended \
    --url "$URL" \
    --token "$TOKEN" \
    --name "${LABEL}-$n" \
    --labels "$LABEL"

  pushd "$RUNNER_DIR" > /dev/null
  sudo ./svc.sh install "$RUNNER_USER"
  sudo ./svc.sh start
  popd > /dev/null

  CREATED+=("$n")
  echo "Runner $n: installed and started."
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
  RUNNER_DIR="$BASE_DIR/actions-runner-$n"
  if [[ -d "$RUNNER_DIR" ]]; then
    SERVICE_NAME=$(cat "$RUNNER_DIR/.service" 2>/dev/null || echo "unknown")
    echo "  Runner $n ($SERVICE_NAME):"
    sudo systemctl status "$SERVICE_NAME" --no-pager -l 2>/dev/null | grep -E "Active:|Loaded:" | sed 's/^/    /' || echo "    (could not query status)"
  fi
done
