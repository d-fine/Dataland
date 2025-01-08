#!/usr/bin/env bash
set -euxo pipefail

environment=$1
source ./deployment/deployment_utils.sh

profile=production

echo "Setting $environment server as deployment target"
target_server_url="$TARGETSERVER_URL"

setup_ssh

timeout 300 bash -c "while ! ssh -o ConnectTimeout=5 ubuntu@$target_server_url exit; do echo '$environment server not yet there - retrying in 5s'; sleep 5; done" || exit

location=/home/ubuntu/dataland
keycloak_backup_dir=/home/ubuntu/keycloak_backup
persistent_keycloak_backup_dir=/home/ubuntu/persistent_keycloak_backup
keycloak_user_dir=$location/dataland-keycloak/users
#btrfs_device=/dev/sdb1
#btrfs_mount_point=/mnt/mybtrfs
#loki_dir=dataland-loki-data
#loki_size_limit=40G

# shut down currently running dataland application and purge files on server
ssh ubuntu@"$target_server_url" "(cd \"$location\" && sudo docker compose --profile production down && sudo docker compose --profile init down && sudo docker compose down --remove-orphans) || true"
# make sure no remnants remain when docker-compose file changes
ssh ubuntu@"$target_server_url" "sudo docker kill $(docker ps -q -a); docker rm $(docker ps -q -a); docker system prune --force; docker info"
# delete pgadmin_config volume
delete_docker_volume_if_existent_remotely "pgadmin_config" "$target_server_url" "$location"

echo "Exporting users and shutting down keycloak."
ssh ubuntu@"$target_server_url" "mkdir -p $location/dataland-keycloak"
scp ./deployment/migrate_keycloak_users.sh ubuntu@"$target_server_url":"$location"/dataland-keycloak
ssh ubuntu@"$target_server_url" "chmod +x \"$location/dataland-keycloak/migrate_keycloak_users.sh\""
ssh ubuntu@"$target_server_url" "\"$location/dataland-keycloak/migrate_keycloak_users.sh\" \"$location\" \"$keycloak_user_dir\" \"$keycloak_backup_dir\" \"$persistent_keycloak_backup_dir\""

ssh ubuntu@"$target_server_url" "sudo rm -rf \"$location\""

construction_dir=./dataland
build_directories "$construction_dir"
scp -r "$construction_dir" ubuntu@"$target_server_url":"$location"

ssh ubuntu@"$target_server_url" "mv \"$keycloak_backup_dir\"/*-users-*.json \"$keycloak_user_dir\" || true"

echo "Set up Keycloak from scratch."
ssh ubuntu@"$target_server_url" "set -o allexport; source \"$location\"/.env; set +o allexport;
                                 \"$location\"/dataland-keycloak/initialize_keycloak.sh $location $keycloak_user_dir" || exit 1

echo "Cleaning up exported user files."
ssh ubuntu@"$target_server_url" "(cp $keycloak_user_dir/*-users-*.json $persistent_keycloak_backup_dir; rm $keycloak_user_dir/*.json) || true"

if [[ $RESET_STACK_AND_REPOPULATE == true ]]; then
  echo "Deleting relevant Volumes"
  delete_docker_volume_if_existent_remotely "backend_data" "$target_server_url" "$location"
  delete_docker_volume_if_existent_remotely "document_manager_data" "$target_server_url" "$location"
  delete_docker_volume_if_existent_remotely "internal_storage_data" "$target_server_url" "$location"
  delete_docker_volume_if_existent_remotely "qa_service_data" "$target_server_url" "$location"
  delete_docker_volume_if_existent_remotely "rabbitmq_data" "$target_server_url" "$location"
  delete_docker_volume_if_existent_remotely "community_manager_data" "$target_server_url" "$location"
  delete_docker_volume_if_existent_remotely "batch_manager_data" "$target_server_url" "$location"
  delete_docker_volume_if_existent_remotely "email_service_data" "$target_server_url" "$location"
  delete_docker_volume_if_existent_remotely "loki_data" "$target_server_url" "$location"
fi

docker volume rm dataland_loki_data
docker volume create --driver local --opt type=ext4 --opt device=ext4 --opt o=size=100k,uid=1000 dataland_loki_data

#echo "Create limited volume in btrfs filesystem for Loki data (all logs)"
## Check if btrfs-filesystem is already mounted
#if ! mount | grep "on $btrfs_mount_point type btrfs" > /dev/null; then
#    echo "Mounting btrfs filesystem..."
#    # Create btrfs filesystem, if it does not exist already
#    if [ ! -b "$btrfs_device" ]; then
#        echo "Device $btrfs_device does not exist. Creating device..."
#        sudo mkfs.btrfs "$btrfs_device"
#    fi
#    # Mount btrfs filesystem
#    sudo mount "$btrfs_device" "$btrfs_mount_point"
#    # Add automatic mounting at boot
#    echo "$btrfs_device $btrfs_mount_point btrfs defaults 0 2" | sudo tee -a /etc/fstab
##    # Enable quota to restrict size of subvolumes
##    sudo btrfs quota enable "$btrfs_mount_point"
#fi
## Create loki subvolume, if it does not exist already
#if [ ! -d "$btrfs_mount_point/$loki_dir" ]; then
#    echo "Creating Btrfs subvolume for Loki."
#    sudo btrfs subvolume create "$btrfs_mount_point/$loki_dir"
#    echo "Restrict size for Loki subvolume."
##    sudo btrfs qgroup limit $loki_size_limit "$btrfs_mount_point/$loki_dir"
#    sudo btrfs subvolume set-size $loki_size_limit "$btrfs_mount_point/$loki_dir"
#else
#    echo "Subvolume for Loki already exists."
#fi
#echo "Setup completed: Limited volume for Loki data (all logs) exists."

if [[ $LOAD_GLEIF_GOLDEN_COPY == true ]]; then
  echo "Setting flag indicating that the full GLEIF Golden Copy File should be imported"
  ssh ubuntu@"$target_server_url" "mkdir -p $location/dataland-batch-manager/config; touch $location/dataland-batch-manager/config/perform_gleif_full_golden_copy_download_flag"
fi

if [[ $LOAD_NORTHDATA_GOLDEN_COPY == true ]]; then
  echo "Setting flag indicating that the full NorthData Golden Copy File should be imported"
  ssh ubuntu@"$target_server_url" "mkdir -p $location/dataland-batch-manager/config; touch $location/dataland-batch-manager/config/perform_northdata_full_golden_copy_download_flag"
fi


# Write all the files necessary for the EuroDaT-client to work
ssh ubuntu@"$target_server_url" "mkdir -p $location/dataland-eurodat-client/secret_files"
scp -r ./dataland-eurodat-client/secret_files_templates ubuntu@"$target_server_url":"$location"/dataland-eurodat-client/secret_files_templates

ssh ubuntu@"$target_server_url" "echo "${EURODAT_CLIENT_KEYSTORE_INT_BASE64}" | base64 -d > $location/dataland-eurodat-client/secret_files/keystore.jks"

scp ./dataland-eurodat-client/write_secret_files.sh ubuntu@"$target_server_url":"$location"/dataland-eurodat-client
ssh ubuntu@"$target_server_url" "$location/dataland-eurodat-client/write_secret_files.sh"

#ssh ubuntu@"$target_server_url" "cd $location; sudo docker volume create --driver local --opt type=ext4 --opt device=/dev/vda1 --opt o=size=10k,uid=1000 test_grafana_data"
#ssh ubuntu@"$target_server_url" "cd $location; sudo docker volume create --driver local --opt type=ext4 --opt device=/dev/vda1 --opt o=size=10k,uid=1000 dataland_loki_data"


echo "Starting docker compose stack."
ssh ubuntu@"$target_server_url" "cd $location; sudo docker compose pull; sudo docker compose --profile $profile up -d --build"

# Wait for all docker containers to become healthy
wait_for_docker_containers_healthy_remote $target_server_url $location $profile

# Wait for backend to finish boot process
wait_for_health "https://$target_server_url/api/actuator/health/ping" "backend"
