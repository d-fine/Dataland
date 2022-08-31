#!/bin/bash
set -eux

target_server_url=$1
location=$2

old_volume=$(ssh ubuntu@"$target_server_url" "cd $location && sudo docker volume ls -q | grep backend_data") || true
if [[ -n $old_volume ]]; then
  echo "Removing old database volume with name $old_volume."
  ssh ubuntu@"$target_server_url" "cd $location && sudo docker volume rm $old_volume"
fi
