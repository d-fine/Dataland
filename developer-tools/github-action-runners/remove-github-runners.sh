#!/usr/bin/env bash
# Removes selected GitHub Actions self-hosted runner instances interactively.
# Use arrow keys to navigate, space to toggle selection, enter to confirm.
#
# Usage:
#   ./remove-github-runners.sh --token <removal-token> [--label <label>] [--base-dir <dir>]
#
# Arguments:
#   --token     Runner removal token (obtain from: Settings → Actions → Runners → select runner → Remove)
#               A single token is valid for all removals within its expiry window.
#   --label     Label used to identify runner directories to remove (default: dala-ci-runner)
#               Matches directories whose registered runner name starts with this prefix.
#   --base-dir  Directory containing the runner folders (default: $HOME)

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
RUNNER_DIRS=()
RUNNER_NAMES=()

for dir in "$BASE_DIR"/*/; do
  [[ -d "$dir" ]] || continue
  RUNNER_JSON="$dir/.runner"
  if [[ -f "$RUNNER_JSON" ]]; then
    RUNNER_NAME=$(grep -o '"agentName": "[^"]*"' "$RUNNER_JSON" 2>/dev/null \
      | sed 's/.*": "//' | tr -d '"' || true)
    if [[ "$RUNNER_NAME" == ${LABEL}* ]]; then
      RUNNER_DIRS+=("$dir")
      RUNNER_NAMES+=("$RUNNER_NAME")
    fi
  fi
done

if [[ ${#RUNNER_DIRS[@]} -eq 0 ]]; then
  echo "No runner directories found in $BASE_DIR matching label prefix '$LABEL'."
  exit 0
fi

# ---------------------------------------------------------------------------
# Interactive checkbox selection
# ---------------------------------------------------------------------------
# CHECKED[i]=1 means runner i is selected for removal (all selected by default)
COUNT=${#RUNNER_DIRS[@]}
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
    local line="  $checkbox  ${RUNNER_NAMES[$i]}  (${RUNNER_DIRS[$i]})"
    if [[ "$i" -eq "$CURSOR" ]]; then
      tput bold 2>/dev/null || true
      printf "\r\033[K> %s\n" "${line:2}"
      tput sgr0 2>/dev/null || true
    else
      printf "\r\033[K%s\n" "$line"
    fi
  done
}

echo "Found $COUNT runner(s) matching label '$LABEL'."
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
SELECTED_DIRS=()
SELECTED_NAMES=()

for (( i=0; i<COUNT; i++ )); do
  if [[ "${CHECKED[$i]}" -eq 1 ]]; then
    SELECTED_DIRS+=( "${RUNNER_DIRS[$i]}" )
    SELECTED_NAMES+=( "${RUNNER_NAMES[$i]}" )
  fi
done

if [[ ${#SELECTED_DIRS[@]} -eq 0 ]]; then
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

for (( i=0; i<${#SELECTED_DIRS[@]}; i++ )); do
  dir="${SELECTED_DIRS[$i]}"
  RUNNER_NAME="${SELECTED_NAMES[$i]}"

  echo ""
  echo "=== Removing runner: $RUNNER_NAME ($dir) ==="

  pushd "$dir" > /dev/null

  # Stop and uninstall the systemd service if present
  if [[ -f ".service" ]]; then
    SERVICE_NAME=$(cat .service)
    echo "  Stopping and uninstalling service: $SERVICE_NAME"
    sudo systemctl stop "$SERVICE_NAME" 2>/dev/null \
      || echo "  Warning: could not stop service (may already be stopped)"
    sudo ./svc.sh uninstall \
      || echo "  Warning: svc.sh uninstall failed, continuing"
  else
    echo "  No .service file found, skipping service removal."
  fi

  # Unregister from GitHub
  echo "  Unregistering runner from GitHub..."
  if ./config.sh remove --token "$TOKEN"; then
    echo "  Unregistered successfully."
    REMOVED+=("$RUNNER_NAME")
  else
    echo "  Warning: GitHub unregistration failed (runner may already be removed from GitHub)."
    FAILED+=("$RUNNER_NAME")
  fi

  popd > /dev/null

  # Delete the runner directory
  echo "  Deleting directory: $dir"
  rm -rf "$dir"
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
