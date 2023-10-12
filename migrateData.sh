#!/usr/bin/env bash
set -euo pipefail

# This script migrates all company with data and the data associated to them from the source to the target environment.
# It is intended to be used by developers testing data migration scripts. It requires python to be installed.
# Current issues with this script:
# 1. Documents are not migrated
# 2. Special characters like "ü" are not correctly transferred

function upload_data_set () {
  data_set_file="$framework_output/$data_id.json"
  response_file="$framework_output/$data_id.response"
  curl -X POST "https://$target/api/data/$framework?bypassQa=true" -H 'accept: application/json' -H "Authorization: Bearer $target_token" \
  -H 'Content-Type: application/json' -d @"$data_set_file" | python -m json.tool --sort-keys > "$framework_output/$data_id.response"
  new_data_id=$(grep "dataId" "$response_file" | cut -d'"' -f4)
  echo "$data_id,$new_data_id" >> "$data_mapping_file"
}

source=$1
source_token=$2
target=$3
target_token=$4
frameworks="eutaxonomy-financials eutaxonomy-non-financials lksg sfdr p2p sme"

if [[ $target == dataland.com ]]; then
  echo "The production environment cannot be the target for this script."
  exit 1
fi

timestamp=$(date +"%Y%m%d_%H%M")
output="./data_migration_$timestamp"

companies_folder=$output/companies
company_file=$companies_folder/company.txt
company_id_file=$companies_folder/company_ids.txt
company_mapping_file=$companies_folder/company_mapping.csv
mkdir -p "$companies_folder"

all_meta_file=$output/meta.txt
all_data_file=$output/dataIds.txt

echo "Writing output to $output"

#migrate all companies with data
curl -X 'GET' "https://$source/api/companies" -H 'accept: application/json' -H "Authorization: Bearer $source_token" | python -m json.tool --sort-keys > "$company_file"
grep "companyId" "$company_file" | cut -d'"' -f4 | sort -u > "$company_id_file"
echo "sourceId,targetId" > "$company_mapping_file"
while read -r old_company_id; do
  echo "Processing company $old_company_id"
  curl -X GET "https://$source/api/companies/$old_company_id" -H 'accept: application/json' -H "Authorization: Bearer $source_token" \
  | python -c 'import json,sys;print(json.dumps(json.load(sys.stdin)["companyInformation"]))' \
  | python -m json.tool --sort-keys > "$companies_folder/$old_company_id.data"
  response_file="$companies_folder/${old_company_id}.response"
  curl -X POST "https://$target/api/companies" -H 'accept: application/json' -H "Authorization: Bearer $target_token" \
  -H 'Content-Type: application/json' -d @"$companies_folder/$old_company_id.data" | python -m json.tool --sort-keys > "$response_file"
  new_company_id=$(grep "companyId" "$response_file" | cut -d'"' -f4)
  echo "$old_company_id,$new_company_id" >> "$company_mapping_file"
done < "$company_id_file"


#migrate all data for all frameworks
for framework in $frameworks; do
  echo "Processing framework $framework"
  framework_output=$output/$framework
  mkdir -p "$framework_output"
  data_mapping_file=$framework_output/data_mapping.csv
  echo "sourceId,targetId" > "$data_mapping_file"

  all_meta_file=$framework_output/meta.txt
  active_meta_file=$framework_output/activeMeta.txt
  all_data_file=$framework_output/dataIds.txt
  active_data_file=$framework_output/activeDataIds.txt
  curl -X GET "https://$source/api/metadata?dataType=$framework&showOnlyActive=false" -H 'accept: application/json' -H "Authorization: Bearer $source_token" |  python -m json.tool --sort-keys > "$all_meta_file"
  grep "dataId" "$all_meta_file" | cut -d'"' -f4 > "$all_data_file"
  curl -X GET "https://$source/api/metadata?dataType=$framework&showOnlyActive=true" -H 'accept: application/json' -H "Authorization: Bearer $source_token" |  python -m json.tool --sort-keys > "$active_meta_file"
  grep "dataId" "$active_meta_file" | cut -d'"' -f4 > "$active_data_file"

  while read -r data_id; do
    echo "Processing data set wit ID $data_id."
    data_set_file=$framework_output/$data_id.json
    curl -X GET "https://$source/api/data/$framework/$data_id" -H 'accept: application/json' -H "Authorization: Bearer $source_token" | python -m json.tool --sort-keys > "$data_set_file"
    old_company_id=$(grep "companyId" "$data_set_file" | cut -d'"' -f4)
    new_company_id=$(grep "$old_company_id" "$company_mapping_file" | cut -d',' -f2)
    sed -i "s/$old_company_id/$new_company_id/g" "$data_set_file"

    if grep -q "$data_id" "$active_data_file"; then
      echo "Skipping upload of active data set $data_id to upload last."
      continue
    fi
    upload_data_set
  done < "$all_data_file"

  while read -r data_id; do
    upload_data_set
  done < "$active_data_file"
done