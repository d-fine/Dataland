#!/bin/bash
echo i am the custom entrypoint
echo $1
# json edit magic
#
docker-entrypoint.sh $1