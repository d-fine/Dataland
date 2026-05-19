#!/usr/bin/env bash
#
# logging_functions.sh — structured terminal output helpers
#
# Public ABI for scripts sourcing this file:
#   log_step <description>       Print a named phase.
#   log_info <message>           Print an informational message.
#   log_success <message>        Print a success message.
#   log_warn <message>           Print a warning message.
#   log_error <message>          Print an error message to stderr.
#
# Use log_step for coarse-grained script phases such as "Starting development stack".
# Use log_info for repeated progress updates inside loops such as
# "Waiting for Keycloak to finish initialization".

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

CHECK_MARK='OK'
if [[ -t 1 ]]; then
  CHECK_MARK='✓'
fi

# Print a named script phase.
log_step() {
  printf '%b...%b %s\n' "$COLOR_BLUE" "$COLOR_RESET" "$1"
}

# Print an informational message.
log_info() {
  printf '%b[i]%b %s\n' "$COLOR_CYAN" "$COLOR_RESET" "$1"
}

# Print a success message.
log_success() {
  printf '%b[%s]%b %s\n' "$COLOR_GREEN" "$CHECK_MARK" "$COLOR_RESET" "$1"
}

# Print a warning message.
log_warn() {
  printf '%b[W]%b %s\n' "$COLOR_YELLOW" "$COLOR_RESET" "$1"
}

# Print an error message to stderr.
log_error() {
  printf '%b[E]%b %s\n' "$COLOR_RED" "$COLOR_RESET" "$1" >&2
}
