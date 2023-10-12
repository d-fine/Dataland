#!/usr/bin/env bash
set -euo pipefail

# This script is intended for supporting developers testing data migration scripts. It downloads all available data from
# one environment and places one json file per data set into a time stamped folder. Requires python to be installed.
# Possible frameworks are currently: eutaxonomy-financials, eutaxonomy-non-financials, lksg, sfdr, p2p, sme

server=$1
framework=$2
token=$3

timestamp=$(date +"%Y%m%d_%H%M")
output="./data_validation/${framework}_$timestamp"
meta_file=$output/meta.txt
data_file=$output/dataIds.txt
mkdir -p "$output"

curl -X 'GET' "https://$server/api/metadata?dataType=$framework&showOnlyActive=false" -H 'accept: application/json' -H "Authorization: Bearer $token" |  python -m json.tool --sort-keys > "$meta_file"
grep "dataId" "$meta_file" | cut -d'"' -f4 > "$data_file"

while read -r dataId;
do
  echo "$dataId"
  curl -X GET "https://$server/api/data/$framework/$dataId" -H 'accept: application/json' -H "Authorization: Bearer $token" | python -m json.tool --sort-keys > "$output/$dataId.data"
done < "$data_file"
