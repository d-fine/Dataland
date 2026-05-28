#!/usr/bin/env bash
# Memory monitor for E2E test runs.
# Samples Docker container stats and Cypress/Electron host process memory every second.
# Usage:
#   ./testing/monitor-memory.sh start [logfile]   - start monitoring in background
#   ./testing/monitor-memory.sh stop              - stop monitoring

set -uo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
PID_FILE="/tmp/memory-monitor.pid"
DEFAULT_LOG_DIR="$REPO_ROOT/log"

print_header() {
  printf "%-26s | %-45s | %6s | %12s | %12s | %6s\n" \
    "TIMESTAMP" "NAME" "CPU%" "MEM_USED" "MEM_LIMIT" "MEM%"
  printf '%0.s-' {1..120}
  printf "\n"
}

# Convert memory strings like 1.2GiB, 512MiB, 3.4MB, 1.5GB to MB
to_mb() {
  echo "$1" | awk '{
    val = $1
    if (val ~ /GiB/) { sub(/GiB/, "", val); printf "%.1f", val * 1024 }
    else if (val ~ /MiB/) { sub(/MiB/, "", val); printf "%.1f", val }
    else if (val ~ /KiB/) { sub(/KiB/, "", val); printf "%.1f", val / 1024 }
    else if (val ~ /GB/)  { sub(/GB/,  "", val); printf "%.1f", val * 1000 }
    else if (val ~ /MB/)  { sub(/MB/,  "", val); printf "%.1f", val }
    else if (val ~ /kB/)  { sub(/kB/,  "", val); printf "%.1f", val / 1024 }
    else printf "%.1f", val
  }'
}

sample_once() {
  local ts tmp_rows
  ts=$(date '+%Y-%m-%d %H:%M:%S.%3N')
  tmp_rows=$(mktemp)

  # Docker container stats — collect raw cpu/mem into temp file
  if docker info > /dev/null 2>&1; then
    docker stats --no-stream --format \
      "{{.Name}}|{{.CPUPerc}}|{{.MemUsage}}|{{.MemPerc}}" 2>/dev/null \
    | while IFS='|' read -r name cpu mem_usage mem_pct; do
        local mem_used mem_limit
        mem_used=$(echo "$mem_usage" | awk '{print $1}')
        mem_limit=$(echo "$mem_usage" | awk '{print $3}')
        echo "ROW|${cpu}|$(to_mb "$mem_used")|$name|$mem_used|$mem_limit|$mem_pct"
      done >> "$tmp_rows"
  fi

  # Cypress / Electron host processes
  { ps aux 2>/dev/null \
    | grep -iE "Cypress|electron" \
    | grep -v grep \
    | awk '{
        rss_mb = $6 / 1024;
        cpu = $3;
        label = "";
        n = split($0, parts, " ");
        for (i = 11; i <= n && length(label) < 44; i++) {
          label = label (i == 11 ? "" : " ") parts[i];
        }
        sub(/.*Cypress\/Cypress/, "Cypress", label);
        sub(/.*node_modules\/\.bin\//, "", label);
        if (length(label) > 44) label = substr(label, 1, 41) "...";
        printf "ROW|%s%%|%.1f|[host] %s|%.1fMB|N/A|%s%%\n", cpu, rss_mb, label, rss_mb, $4;
      }' >> "$tmp_rows"; } || true

  # Processes inside the e2etests container (CI only — silently skipped if container not running)
  local e2e_ps
  e2e_ps=$(docker exec dataland-e2etests-1 ps aux --sort=-%mem 2>/dev/null) || true
  if [[ -n "$e2e_ps" ]]; then
    echo "$e2e_ps" | awk 'NR>1 {
        rss_mb = $6 / 1024;
        cpu = $3;
        if (rss_mb > 10) {
          label = "";
          n = split($0, parts, " ");
          for (i = 11; i <= n && length(label) < 44; i++) {
            label = label (i == 11 ? "" : " ") parts[i];
          }
          sub(/.*Cypress\/Cypress/, "Cypress", label);
          sub(/.*node_modules\/\.bin\//, "", label);
          if (length(label) > 44) label = substr(label, 1, 41) "...";
          printf "ROW|%s%%|%.1f|[e2e] %s|%.1fMB|N/A|%s%%\n", cpu, rss_mb, label, rss_mb, $4;
        }
      }' >> "$tmp_rows" || true
  fi

  # Print individual rows
  while IFS='|' read -r _ cpu mem_mb name mem_used mem_limit mem_pct; do
    printf "%-26s | %-45s | %6s | %12s | %12s | %6s\n" \
      "$ts" "$name" "$cpu" "$mem_used" "$mem_limit" "$mem_pct"
  done < "$tmp_rows"

  # Compute and print totals
  awk -F'|' -v ts="$ts" '
    { gsub(/%/, "", $2); cpu_sum += $2; mem_sum += $3 }
    END {
      printf "%-26s | %-45s | %5.1f%% | %9.1fMB | %12s | %6s\n",
        ts, ">>> TOTAL", cpu_sum, mem_sum, "", ""
    }
  ' "$tmp_rows"

  rm -f "$tmp_rows"

  printf '%0.s-' {1..120}
  printf "\n"
}

monitor_loop() {
  local logfile="$1"
  mkdir -p "$(dirname "$logfile")"
  {
    echo "Memory monitor started at $(date '+%Y-%m-%d %H:%M:%S')"
    echo "Log file: $logfile"
    echo ""
    print_header
  } >> "$logfile"

  while true; do
    sample_once >> "$logfile" 2>&1 || true
    sleep 2
  done
}

cmd_start() {
  local logfile="${1:-$DEFAULT_LOG_DIR/memory-monitor-$(date '+%Y%m%d_%H%M%S').log}"

  if [[ -f "$PID_FILE" ]] && kill -0 "$(cat "$PID_FILE")" 2>/dev/null; then
    echo "Monitor already running (PID $(cat "$PID_FILE")). Stop it first."
    exit 1
  fi

  mkdir -p "$(dirname "$logfile")"
  monitor_loop "$logfile" &
  local pid=$!
  echo "$pid" > "$PID_FILE"
  echo "$logfile" > /tmp/memory-monitor.logfile

  echo "Memory monitor started (PID $pid)"
  echo "Log file: $logfile"
}

cmd_stop() {
  if [[ ! -f "$PID_FILE" ]]; then
    echo "No PID file found — monitor may not be running."
    exit 0
  fi

  local pid
  pid=$(cat "$PID_FILE")

  if kill -0 "$pid" 2>/dev/null; then
    kill "$pid"
    echo "Memory monitor stopped (PID $pid)"
  else
    echo "Process $pid not found — already stopped."
  fi

  rm -f "$PID_FILE"

  if [[ -f /tmp/memory-monitor.logfile ]]; then
    echo "Log saved to: $(cat /tmp/memory-monitor.logfile)"
    rm -f /tmp/memory-monitor.logfile
  fi
}

case "${1:-}" in
  start) cmd_start "${2:-}" ;;
  stop)  cmd_stop ;;
  *)
    echo "Usage: $0 start [logfile] | stop"
    exit 1
    ;;
esac