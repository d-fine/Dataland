#!/bin/bash
LOG_FILE="/var/log/health-check.log"

echo "$(date): Health check script started" >> "$LOG_FILE"

# Check health status of each container
for container in $(docker ps --format '{{.Names}}'); do
    health_status=$(docker inspect --format '{{.State.Health.Status}}' "$container" 2>/dev/null)

    if [ -z "$health_status" ]; then
        echo "$(date): Health status for container $container is not available" >> "$LOG_FILE"
    elif [ "$health_status" == "unhealthy" ]; then
        echo "$(date): Container $container is unhealthy" >> "$LOG_FILE"
    elif [ "$health_status" == "healthy" ]; then
        echo "$(date): Container $container is healthy" >> "$LOG_FILE"
    else
        echo "$(date): Container $container has unknown health status: $health_status" >> "$LOG_FILE"
    fi
done
