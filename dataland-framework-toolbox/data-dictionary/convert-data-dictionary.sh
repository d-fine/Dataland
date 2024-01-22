#!/usr/bin/env bash
set -euxo pipefail

if [ ! -f "dataDictionary.xlsx" ]; then
  echo "Cannot find dataDictionary.xlsx. Please download the most recent version from teams."
  exit 1
fi

if [ -e "./csv" ]; then
  echo "Deleting"
  rm -rf ./csv
fi
mkdir -p ./csv

docker compose up --build

echo "All done!"