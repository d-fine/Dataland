#!/usr/bin/env bash
set -euxo pipefail

loki_volume="$1"

# Move health check scripts and services to their destinations
sudo mv /tmp/health-check/healthCheck.sh /usr/local/bin/healthCheck.sh
sudo mv /tmp/health-check/health-check.service /etc/systemd/system/health-check.service
sudo mv /tmp/health-check/logrotate.service /etc/systemd/system/logrotate.service
sudo mv /tmp/health-check/logrotate.timer /etc/systemd/system/logrotate.timer

# Configure environment file and logrotate settings
echo "LOKI_VOLUME=$loki_volume" | sudo tee "/etc/default/health-check" > /dev/null
sudo sed "s|\${LOKI_VOLUME}|$loki_volume|g" /tmp/health-check/health-check | sudo tee /etc/logrotate.d/health-check > /dev/null
sudo chown root:root /etc/logrotate.d/health-check
sudo chmod +x /usr/local/bin/healthCheck.sh

# Reload systemd manager configuration and enable services
sudo systemctl daemon-reload
sudo systemctl enable health-check.service
sudo systemctl enable logrotate.timer

# Ensure the health check log directory exists
if [ ! -d "$loki_volume/health-check-log" ]; then
  echo "Creating $loki_volume/health-check-log dir as volume for docker container health check logs"
  sudo mkdir -p "$loki_volume/health-check-log"
  sudo chmod a+w "$loki_volume/health-check-log"
fi

echo "(Re-)Start Health Check for Docker Containers"
sudo systemctl restart health-check.service
sudo systemctl restart logrotate.timer