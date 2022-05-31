#!/bin/bash

docker stop data_image_creator || true
docker rm data_image_creator || true
docker run --name data_image_creator -v $1:/source_volume alpine:latest sh -c "mkdir /data && cp -r /source_volume/* /data"
docker commit --change "ENTRYPOINT [\"/bin/sh\", \"-c\"]" --change "CMD [\"sh\"]" data_image_creator $2
docker stop data_image_creator || true
docker rm data_image_creator || true
#docker push $2
