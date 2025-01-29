#!/bin/bash
LOG_FILE="/var/log/health-check.log"

echo "$(date): Health check script started" >> "$LOG_FILE"

# Überprüfe den Gesundheitsstatus jedes Containers
for container in $(docker ps --format '{{.Names}}'); do
    health_status=$(docker inspect --format '{{.State.Health.Status}}' "$container" 2>/dev/null)

    # Überprüfe, ob der Gesundheitsstatus vorhanden ist
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

###!/bin/bash
##LOG_FILE="/var/log/health-check.log"
##
##while true; do
##    for container in $(docker ps --format '{{.Names}}'); do
##        health_status=$(docker inspect --format '{{.State.Health.Status}}' "$container" 2>/dev/null)
##
##        if [ "$health_status" == "unhealthy" ]; then
##            echo "$(date): $container is unhealthy" >> "$LOG_FILE"
##            # Hier kannst du auch die Slack-Benachrichtigung senden
##            curl -X POST -H 'Content-type: application/json' --data '{"text": "'"$container is unhealthy"'}' https://hooks.slack.com/services/YOUR/SLACK/WEBHOOK
##        elif [ "$health_status" == "healthy" ]; then
##            echo "$(date): $container is healthy" >> "$LOG_FILE"
##        fi
##    done
##    sleep 15
##done
##!/bin/bash
#LOG_FILE="/var/log/health-check.log"
#
## Überprüfe den Gesundheitsstatus des Containers
#health_status=$(curl -f http://localhost/health)  # Passe die URL an, je nach deiner Anwendung
#
#if [ $? -ne 0 ]; then
#    echo "$(date): Container is unhealthy" >> "$LOG_FILE"
#    exit 1  # Gibt einen Fehlercode zurück, um anzuzeigen, dass der Healthcheck fehlgeschlagen ist
#else
#    echo "$(date): Container is healthy" >> "$LOG_FILE"
#    exit 0  # Gibt einen Erfolgscode zurück
#fi