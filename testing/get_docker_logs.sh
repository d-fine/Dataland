#!/bin/bash
# Write the logs of the docker container for later upload and analysis
mkdir -p ./dockerLogs
for docker_service in $(sudo docker ps --all --format "{{.Names}}");
do
  sudo docker logs "$docker_service" > ./dockerLogs/"$docker_service".log 2>&1
done