#!/bin/bash
set -euxo pipefail
# Utility script to update the data container image. Expects two inputs: the csv file to parse and the version number
# for the data container

csv_file="$1"
version="$2"
workdir=$(dirname "$0")
work_file=data.csv

cd "$workdir"

if [[ ! -f "$csv_file" ]]; then
  echo "Error: Expected file $csv_file does not exist."
  exit 1
fi

iconv -f iso-8859-1 -t UTF-8 "$csv_file" > $work_file

./../gradlew :dataland-csvconverter:run --args="$work_file" --stacktrace

docker login ghcr.io -u "$GITHUB_USER" -p "$GITHUB_TOKEN"
docker build -t ghcr.io/d-fine/dataland/datacontainer:"$version" .
docker image push ghcr.io/d-fine/dataland/datacontainer:"$version"

rm -f "$work_file"
