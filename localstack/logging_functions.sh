#!/usr/bin/env bash

: "${SILENT:=false}"

# Only emit ANSI colors for interactive terminals unless color output is explicitly disabled.
if [[ -t 1 && "${NO_COLOR:-false}" != "true" ]]; then
  COLOR_BLUE='\033[1;34m'
  COLOR_CYAN='\033[0;36m'
  COLOR_GREEN='\033[0;32m'
  COLOR_YELLOW='\033[0;33m'
  COLOR_RED='\033[0;31m'
  COLOR_GRAY='\033[0;90m'
  COLOR_RESET='\033[0m'
else
  COLOR_BLUE=''
  COLOR_CYAN=''
  COLOR_GREEN=''
  COLOR_YELLOW=''
  COLOR_RED=''
  COLOR_GRAY=''
  COLOR_RESET=''
fi

STATUS_LINE_ACTIVE=false
STATUS_LINE_COUNT=0
CHECK_MARK='OK'
if [[ -t 1 ]]; then
  CHECK_MARK='✓'
fi

# Step and status output share terminal state so temporary progress lines can be replaced cleanly.
STEP_LINE_ACTIVE=false
STEP_LINE_DESCRIPTION=''

_step_line_commit_plain() {
  if [[ "$STEP_LINE_ACTIVE" == true ]]; then
    printf '\r\033[2K%b==>%b %s\n' "$COLOR_BLUE" "$COLOR_RESET" "$STEP_LINE_DESCRIPTION"
    STEP_LINE_ACTIVE=false
    STEP_LINE_DESCRIPTION=''
  fi
}

_status_line_clear() {
  if [[ "$STATUS_LINE_ACTIVE" == true ]]; then
    printf '\r\033[2K'

    local line_index
    for ((line_index = 1; line_index < STATUS_LINE_COUNT; line_index++)); do
      printf '\033[1A\r\033[2K'
    done

    STATUS_LINE_ACTIVE=false
    STATUS_LINE_COUNT=0
  fi
}

log_step() {
  _status_line_clear

  if [[ -t 1 && "$SILENT" == true ]]; then
    STEP_LINE_ACTIVE=true
    STEP_LINE_DESCRIPTION="$1"
    printf '\r\033[2K%b==>%b %s' "$COLOR_BLUE" "$COLOR_RESET" "$1"
    return
  fi

  printf '%b==>%b %s\n' "$COLOR_BLUE" "$COLOR_RESET" "$1"
}

log_info() {
  _step_line_commit_plain
  _status_line_clear
  printf '%b[i]%b %s\n' "$COLOR_CYAN" "$COLOR_RESET" "$1"
}

log_success() {
  _step_line_commit_plain
  _status_line_clear
  printf '%b[ok]%b %s\n' "$COLOR_GREEN" "$COLOR_RESET" "$1"
}

log_warn() {
  _step_line_commit_plain
  _status_line_clear
  printf '%b[warn]%b %s\n' "$COLOR_YELLOW" "$COLOR_RESET" "$1"
}

log_error() {
  _step_line_commit_plain
  _status_line_clear
  printf '%b[error]%b %s\n' "$COLOR_RED" "$COLOR_RESET" "$1" >&2
}

status_line_print() {
  _step_line_commit_plain

  if [[ ! -t 1 || "$SILENT" != true ]]; then
    log_info "$1"
    return
  fi

  _status_line_clear
  STATUS_LINE_ACTIVE=true

  local message="$1"
  local line_count=1
  local remaining_message="$message"
  # Track how many terminal lines were printed so the next update can erase them.
  while [[ "$remaining_message" == *$'\n'* ]]; do
    remaining_message="${remaining_message#*$'\n'}"
    ((line_count++))
  done

  STATUS_LINE_COUNT=$line_count
  printf '%b[i]%b %s' "$COLOR_GRAY" "$COLOR_RESET" "$message"
}

log_step_done() {
  _status_line_clear

  if [[ "$STEP_LINE_ACTIVE" == true && "$STEP_LINE_DESCRIPTION" == "$1" ]]; then
    printf '\r\033[2K%b==>%b %s %b[%s]%b\n' "$COLOR_BLUE" "$COLOR_RESET" "$1" "$COLOR_GREEN" "$CHECK_MARK" "$COLOR_RESET"
    STEP_LINE_ACTIVE=false
    STEP_LINE_DESCRIPTION=''
    return
  fi

  printf '%b==>%b %s %b[%s]%b\n' "$COLOR_BLUE" "$COLOR_RESET" "$1" "$COLOR_GREEN" "$CHECK_MARK" "$COLOR_RESET"
}
