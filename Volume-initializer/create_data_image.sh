#!/bin/bash
# A Script that will create & push an image that persists the data of a local volume
# The create image will bear the contents of the <source_volume> in the folder /data
# the image's name/tag will be <image_tag>
# Usage: create_data_image <source_volume> <image_tag>

docker stop data_image_creator || true
docker rm data_image_creator || true
docker run --name data_image_creator -v $1:/source_volume alpine:latest sh -c "mkdir /data && cp -r /source_volume/* /data"
docker commit --change "ENTRYPOINT [\"/bin/sh\", \"-c\"]" --change "CMD [\"sh\"]" data_image_creator $2
docker stop data_image_creator || true
docker rm data_image_creator || true
docker push $2
