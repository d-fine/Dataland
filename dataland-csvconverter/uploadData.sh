#!/bin/bash
set -e
# Utility script to update the data container image. Requires the csv file to be in the dataland-csvconverter
# directory.

csv_file="$1"
workdir=$(dirname $0)

cd $workdir

if [[ ! -f "$csv_file" ]]; then
  echo "Error: Expected file $workdir/$csv_file not found."
  exit 1
fi

./../gradlew :dataland-csvconverter:run --args="$csv_file"

docker login ghcr.io -u "$GITHUB_USER" -p "$GITHUB_TOKEN"
docker build -t ghcr.io/d-fine/dataland/datacontainer:latest .
docker image push ghcr.io/d-fine/dataland/datacontainer:latest
