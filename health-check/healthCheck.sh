#!/bin/bash

set -euo pipefail

# Check if LOKI_VOLUME is set
if [[ -z "$LOKI_VOLUME" ]]; then
    echo "LOKI_VOLUME environment variable is not set."
    exit 1
fi

# Infinite loop
while true; do
  # Check health status of each container
  for container in $(docker ps --format '{{.Names}}'); do

      # Define your custom log file name
      custom_log_file="${LOKI_VOLUME}/health-check-log/health-check.log"

      # Check health status and log it
      health_status=$(docker inspect --format '{{.State.Health.Status}}' "$container" 2>/dev/null || echo "not_available")

      if [ "$health_status" == "not_available" ]; then
          echo "$(date) level=warn container=$container Health status is not available" >> "$custom_log_file"
      elif [ "$health_status" == "unhealthy" ]; then
          echo "$(date) level=error container=$container Container is unhealthy" >> "$custom_log_file"
      elif [ "$health_status" == "healthy" ]; then
          echo "$(date) level=info container=$container Container is healthy" >> "$custom_log_file"
      else
          echo "$(date) level=warn container=$container Container has unknown health status: $health_status" >> "$custom_log_file"
      fi
  done
  sleep 30
done