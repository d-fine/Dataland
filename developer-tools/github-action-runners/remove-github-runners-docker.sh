#!/usr/bin/env bash
# Removes selected Docker-based GitHub Actions self-hosted runner containers interactively.
# Use arrow keys to navigate, space to toggle selection, enter to confirm.
#
# Usage:
#   ./remove-github-runners-docker.sh --token <removal-token> [--label <label>]
#
# Arguments:
#   --token   Runner removal token (obtain from: Settings → Actions → Runners → select runner → Remove)
#             A single token is valid for all removals within its expiry window.
#   --label   Label/name-prefix used to identify runner containers to remove
#             (default: dala-ci-runner). Matches container names starting with this prefix.
#
# Each selected runner is unregistered from GitHub explicitly (via `docker exec` running
# config.sh remove --token <removal-token> inside the container). The container is only
# stopped and removed if the unregistration was verified successful.
#
# Requires: docker, systemd, sudo.

set -euo pipefail

LABEL="dala-ci-runner"
TOKEN=""
RUNNER_DIR="/home/runner/actions-runner"

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
    *)
      echo "Unknown argument: $1"
      echo "Usage: $0 --token <removal-token> [--label <label>]"
      exit 1
      ;;
  esac
done

if [[ -z "$TOKEN" ]]; then
  echo "Error: --token is required."
  echo "Usage: $0 --token <removal-token> [--label <label>]"
  exit 1
fi

if ! command -v docker > /dev/null 2>&1; then
  echo "Error: docker is required on the host but was not found."
  exit 1
fi

# ---------------------------------------------------------------------------
# Discover matching runner containers
# ---------------------------------------------------------------------------
RUNNER_NAMES=()
RUNNER_STATUSES=()

while IFS=$'\t' read -r name status; do
  [[ -z "$name" ]] && continue
  RUNNER_NAMES+=("$name")
  RUNNER_STATUSES+=("$status")
done < <(docker ps -a --filter "name=^${LABEL}-" --format '{{.Names}}\t{{.Status}}')

if [[ ${#RUNNER_NAMES[@]} -eq 0 ]]; then
  echo "No runner containers found matching label prefix '$LABEL'."
  exit 0
fi

# ---------------------------------------------------------------------------
# Interactive checkbox selection
# ---------------------------------------------------------------------------
# CHECKED[i]=1 means runner i is selected for removal (all selected by default)
COUNT=${#RUNNER_NAMES[@]}
CHECKED=()
for (( i=0; i<COUNT; i++ )); do
  CHECKED+=( 1 )
done

CURSOR=0

draw_menu() {
  # Move cursor to top of menu area (COUNT lines)
  tput cuu "$COUNT" 2>/dev/null || true
  for (( i=0; i<COUNT; i++ )); do
    local checkbox
    if [[ "${CHECKED[$i]}" -eq 1 ]]; then
      checkbox="[x]"
    else
      checkbox="[ ]"
    fi
    local line="  $checkbox  ${RUNNER_NAMES[$i]}  (${RUNNER_STATUSES[$i]})"
    if [[ "$i" -eq "$CURSOR" ]]; then
      tput bold 2>/dev/null || true
      printf "\r\033[K> %s\n" "${line:2}"
      tput sgr0 2>/dev/null || true
    else
      printf "\r\033[K%s\n" "$line"
    fi
  done
}

echo "Found $COUNT runner container(s) matching label '$LABEL'."
echo "Use UP/DOWN to navigate, SPACE to toggle, ENTER to confirm, q to quit."
echo ""

# Print initial blank lines that draw_menu will overwrite
for (( i=0; i<COUNT; i++ )); do printf "\n"; done

draw_menu

# Read keys without echo, one char at a time
old_tty=$(stty -g)
stty -echo -icanon min 1 time 0

while true; do
  key=$(dd bs=1 count=1 2>/dev/null | od -An -tx1 | tr -d ' \n')

  case "$key" in
    # Enter key (0d or 0a)
    0d|0a)
      break
      ;;
    # Space: toggle current
    20)
      if [[ "${CHECKED[$CURSOR]}" -eq 1 ]]; then
        CHECKED[$CURSOR]=0
      else
        CHECKED[$CURSOR]=1
      fi
      draw_menu
      ;;
    # Arrow keys come as 3-byte escape sequences: ESC [ A/B
    # Read the remaining 2 bytes when we see ESC (1b)
    1b)
      seq1=$(dd bs=1 count=1 2>/dev/null | od -An -tx1 | tr -d ' \n')
      seq2=$(dd bs=1 count=1 2>/dev/null | od -An -tx1 | tr -d ' \n')
      if [[ "$seq1" == "5b" ]]; then
        case "$seq2" in
          # Up arrow
          41)
            (( CURSOR > 0 )) && CURSOR=$(( CURSOR - 1 ))
            draw_menu
            ;;
          # Down arrow
          42)
            (( CURSOR < COUNT - 1 )) && CURSOR=$(( CURSOR + 1 ))
            draw_menu
            ;;
        esac
      fi
      ;;
    # q: quit
    71)
      stty "$old_tty"
      echo ""
      echo "Aborted."
      exit 0
      ;;
  esac
done

stty "$old_tty"
echo ""

# ---------------------------------------------------------------------------
# Collect selected runners
# ---------------------------------------------------------------------------
SELECTED_NAMES=()

for (( i=0; i<COUNT; i++ )); do
  if [[ "${CHECKED[$i]}" -eq 1 ]]; then
    SELECTED_NAMES+=( "${RUNNER_NAMES[$i]}" )
  fi
done

if [[ ${#SELECTED_NAMES[@]} -eq 0 ]]; then
  echo "No runners selected. Nothing to do."
  exit 0
fi

echo "Selected for removal:"
for name in "${SELECTED_NAMES[@]}"; do
  echo "  - $name"
done
echo ""
read -r -p "Confirm removal? [y/N] " CONFIRM
if [[ "$CONFIRM" != "y" && "$CONFIRM" != "Y" ]]; then
  echo "Aborted."
  exit 0
fi

# ---------------------------------------------------------------------------
# Remove each selected runner
# ---------------------------------------------------------------------------
REMOVED=()
FAILED=()

for RUNNER_NAME in "${SELECTED_NAMES[@]}"; do
  SERVICE_NAME="${RUNNER_NAME}.service"
  SERVICE_PATH="/etc/systemd/system/${SERVICE_NAME}"

  echo ""
  echo "=== Removing runner: $RUNNER_NAME ==="

  # ---------------------------------------------------------------------
  # Unregister from GitHub explicitly, using the supplied removal token,
  # before touching the container/service. This is more reliable than
  # relying on entrypoint.sh's SIGTERM trap, whose stored RUNNER_TOKEN
  # (the original registration token) may have already expired.
  # ---------------------------------------------------------------------
  UNREGISTERED=0
  IS_RUNNING=$(docker inspect -f '{{.State.Running}}' "$RUNNER_NAME" 2>/dev/null || echo "false")

  if [[ "$IS_RUNNING" == "true" ]]; then
    echo "  Unregistering runner from GitHub..."
    if docker exec -u runner "$RUNNER_NAME" "$RUNNER_DIR/config.sh" remove --token "$TOKEN"; then
      echo "  Unregistered successfully."
      UNREGISTERED=1
    else
      echo "  Error: GitHub unregistration failed for $RUNNER_NAME. Skipping removal."
      FAILED+=("$RUNNER_NAME")
      continue
    fi
  else
    echo "  Container is not running; assuming already unregistered from GitHub."
    UNREGISTERED=1
  fi

  if [[ "$UNREGISTERED" -ne 1 ]]; then
    echo "  Error: could not verify unregistration for $RUNNER_NAME. Skipping removal."
    FAILED+=("$RUNNER_NAME")
    continue
  fi

  # Stop via systemd so that `docker stop` sends SIGTERM to the container.
  if [[ -f "$SERVICE_PATH" ]]; then
    echo "  Stopping and disabling service: $SERVICE_NAME"
    sudo systemctl stop "$SERVICE_NAME" 2>/dev/null \
      || echo "  Warning: could not stop service (may already be stopped)"
    sudo systemctl disable "$SERVICE_NAME" 2>/dev/null \
      || echo "  Warning: could not disable service"
    sudo rm -f "$SERVICE_PATH"
    sudo systemctl daemon-reload
  else
    echo "  No systemd unit found at $SERVICE_PATH, stopping container directly."
    docker stop "$RUNNER_NAME" 2>/dev/null \
      || echo "  Warning: could not stop container (may already be stopped)"
  fi

  echo "  Removing container: $RUNNER_NAME"
  if docker rm -f "$RUNNER_NAME" > /dev/null 2>&1; then
    echo "  Removed successfully."
    REMOVED+=("$RUNNER_NAME")
  else
    echo "  Warning: failed to remove container (may already be removed)."
    FAILED+=("$RUNNER_NAME")
  fi
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
