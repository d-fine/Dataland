#!/bin/bash

health_check_log_dir="${LOKI_VOLUME}/health-check-log"
mkdir -p "$health_check_log_dir"


# Check health status of each container
for container in $(docker ps --format '{{.Names}}'); do

    # Define your custom log file name
    custom_log_file="$health_check_log_dir/health-check.log"
    echo "LOKI_VOLUME is set to: $LOKI_VOLUME" >> "$custom_log_file"

    # Check health status and log it
    health_status=$(docker inspect --format '{{.State.Health.Status}}' "$container" 2>/dev/null)

    if [ -z "$health_status" ]; then
        echo "$(date) level=warn container=$container Health status is not available" >> "$custom_log_file"
    elif [ "$health_status" == "unhealthy" ]; then
        echo "$(date) level=error container=$container Container is unhealthy" >> "$custom_log_file"
    elif [ "$health_status" == "healthy" ]; then
        echo "$(date) level=info container=$container Container is healthy" >> "$custom_log_file"
    else
        echo "$(date) level=warn container=$container Container has unknown health status: $health_status" >> "$custom_log_file"
    fi
done