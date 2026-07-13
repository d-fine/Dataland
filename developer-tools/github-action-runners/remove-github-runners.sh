#!/usr/bin/env bash
# Removes all GitHub Actions self-hosted runner instances whose registered label
# matches the given tag. Each runner is gracefully unregistered from GitHub,
# its systemd service is stopped and uninstalled, and the runner directory is deleted.
#
# Usage:
#   ./remove-github-runners.sh --token <removal-token> [--label <label>] [--base-dir <dir>]
#
# Arguments:
#   --token     Runner removal token (obtain from: Settings → Actions → Runners → select runner → Remove)
#               A single token is valid for all removals within its expiry window.
#   --label     Label used to identify runner directories to remove (default: dala-ci-runner)
#               Matches directories named <label>-<n> inside base-dir, as created by setup-github-runners.sh.
#   --base-dir  Directory containing the runner folders (default: $HOME)
#
# The script identifies runner directories by scanning base-dir for folders whose
# registered runner name (stored in .runner) matches the given label prefix.

set -euo pipefail

LABEL="dala-ci-runner"
BASE_DIR="$HOME"
TOKEN=""

# ---------------------------------------------------------------------------
# Argument parsing
# ---------------------------------------------------------------------------
while [[ $# -gt 0 ]]; do
  case "$1" in
    --token)
      TOKEN="$2"
      shift 2
      ;;
    --label)
      LABEL="$2"
      shift 2
      ;;
    --base-dir)
      BASE_DIR="$2"
      shift 2
      ;;
    *)
      echo "Unknown argument: $1"
      echo "Usage: $0 --token <removal-token> [--label <label>] [--base-dir <dir>]"
      exit 1
      ;;
  esac
done

if [[ -z "$TOKEN" ]]; then
  echo "Error: --token is required."
  echo "Usage: $0 --token <removal-token> [--label <label>] [--base-dir <dir>]"
  exit 1
fi

# ---------------------------------------------------------------------------
# Discover matching runner directories
# ---------------------------------------------------------------------------
MATCHING_DIRS=()

for dir in "$BASE_DIR"/*/; do
  [[ -d "$dir" ]] || continue
  RUNNER_JSON="$dir/.runner"
  if [[ -f "$RUNNER_JSON" ]]; then
    RUNNER_NAME=$(grep -o '"agentName": "[^"]*"' "$RUNNER_JSON" 2>/dev/null | sed 's/.*": "//' | tr -d '"' || true)
    if [[ "$RUNNER_NAME" == ${LABEL}* ]]; then
      MATCHING_DIRS+=("$dir")
    fi
  fi
done

if [[ ${#MATCHING_DIRS[@]} -eq 0 ]]; then
  echo "No runner directories found in $BASE_DIR matching label prefix '$LABEL'."
  exit 0
fi

echo "Found ${#MATCHING_DIRS[@]} runner(s) matching label '$LABEL':"
for dir in "${MATCHING_DIRS[@]}"; do
  RUNNER_NAME=$(grep -o '"agentName": "[^"]*"' "$dir/.runner" 2>/dev/null | sed 's/.*": "//' | tr -d '"' || echo "unknown")
  echo "  $dir  ($RUNNER_NAME)"
done

echo ""
read -r -p "Proceed with removal of all listed runners? [y/N] " CONFIRM
if [[ "$CONFIRM" != "y" && "$CONFIRM" != "Y" ]]; then
  echo "Aborted."
  exit 0
fi

# ---------------------------------------------------------------------------
# Remove each runner
# ---------------------------------------------------------------------------
REMOVED=()
FAILED=()

for dir in "${MATCHING_DIRS[@]}"; do
  RUNNER_NAME=$(grep -o '"agentName": "[^"]*"' "$dir/.runner" 2>/dev/null | sed 's/.*": "//' | tr -d '"' || echo "unknown")
  echo ""
  echo "=== Removing runner: $RUNNER_NAME ($dir) ==="

  pushd "$dir" > /dev/null

  # Stop and uninstall the systemd service if present
  if [[ -f ".service" ]]; then
    SERVICE_NAME=$(cat .service)
    echo "  Stopping and uninstalling service: $SERVICE_NAME"
    sudo systemctl stop "$SERVICE_NAME" 2>/dev/null || echo "  Warning: could not stop service (may already be stopped)"
    sudo ./svc.sh uninstall || echo "  Warning: svc.sh uninstall failed, continuing"
  else
    echo "  No .service file found, skipping service removal."
  fi

  # Unregister from GitHub
  echo "  Unregistering runner from GitHub..."
  if ./config.sh remove --token "$TOKEN"; then
    echo "  Unregistered successfully."
  else
    echo "  Warning: GitHub unregistration failed (runner may already be removed from GitHub)."
  fi

  popd > /dev/null

  # Delete the runner directory
  echo "  Deleting directory: $dir"
  rm -rf "$dir"

  REMOVED+=("$RUNNER_NAME")
  echo "  Done."
done

# ---------------------------------------------------------------------------
# Summary
# ---------------------------------------------------------------------------
echo ""
echo "========================================"
echo "Removal complete"
echo "========================================"
echo "Removed : ${REMOVED[*]:-none}"
echo "Failed  : ${FAILED[*]:-none}"
