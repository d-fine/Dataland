#!/bin/bash
# A Script that will copy persistent data from a data image <image_tag> to a local volume <target_volume>
# The data image should have been created with create_data_image_from_volume.sh
# Usage: create_volume_from_data_image.sh <image_tag> <target_volume>

docker volume rm $2
docker run --rm -v $2:/target_volume $1 sh -c "cp -r /data/* /target_volume"
