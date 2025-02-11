#!/bin/bash

base_log_dir="${LOKI_VOLUME}/health-check-log"

# Check health status of each container
for container in $(docker ps --format '{{.Names}}'); do
#    echo "Container $container"
    # Retrieve the full LogPath
    full_log_path=$(docker inspect --format '{{.LogPath}}' $container 2>/dev/null)
    # Extract the directory path from LogPath
    log_dir=$(dirname "$full_log_path")

    if [ -z "$base_log_dir" ]; then
        container_log_dir="$log_dir"
    else
        container_log_dir="$base_log_dir/$container"
        mkdir -p "$container_log_dir"
    fi

    # Define your custom log file name
    custom_log_file="$container_log_dir/health-check.log"

    # Check health status and log it
    health_status=$(docker inspect --format '{{.State.Health.Status}}' "$container" 2>/dev/null)

    if [ -z "$health_status" ]; then
        echo "$(date): Health status for container $container is not available" >> "$custom_log_file"
    elif [ "$health_status" == "unhealthy" ]; then
        echo "$(date): Container $container is unhealthy" >> "$custom_log_file"
    elif [ "$health_status" == "healthy" ]; then
        echo "$(date): Container $container is healthy" >> "$custom_log_file"
    else
        echo "$(date): Container $container has unknown health status: $health_status" >> "$custom_log_file"
    fi
done