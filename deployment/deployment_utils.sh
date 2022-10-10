#!/bin/bash

setup_ssh () {
  mkdir -p ~/.ssh/
  echo "$TARGETSERVER_HOST_KEYS" >  ~/.ssh/known_hosts
  echo "$SSH_PRIVATE_KEY" > ~/.ssh/id_rsa
  chmod 600 ~/.ssh/id_rsa
}

wait_for_health () {
  timeout 240 bash -c "while ! curl -L $1 2>/dev/null | grep -q UP; do echo 'Waiting for $2 to finish boot process.'; sleep 5; done; echo '$2 available!'"
}

delete_docker_volume_if_existent () {
  volume_filter=$1

  old_volume=$(search_volume "$volume_filter")
  if [[ -n $old_volume ]]; then
    echo "Removing old database volume with name $old_volume."
    docker volume rm "$old_volume"
  fi
}

search_volume() {
  volume_filter=$1
  volume_found=$(docker volume ls -q | grep "$volume_filter") || true
  echo "$volume_found"
}