#!/bin/bash

delete_docker_volume_if_existent () {
  target_server_url=$1
  location=$2
  volume_filter=$3

  old_volume=$(ssh ubuntu@"$target_server_url" "cd $location && sudo docker volume ls -q | grep $volume_filter") || true
  if [[ -n $old_volume ]]; then
    echo "Removing old database volume with name $old_volume."
    ssh ubuntu@"$target_server_url" "cd $location && sudo docker volume rm $old_volume"
  fi
}
export -f is_edc_server_up_and_healthy