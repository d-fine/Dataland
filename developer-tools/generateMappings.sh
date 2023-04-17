#!/bin/bash
set -euxo pipefail

convertToCamelCase () {
  local string=$(echo "$1" | tr -cd '[:alnum:] ')
  local result=""
  local lower=""
  for part in $string; do
    lower=${part,,}
    result+=${lower^}
  done
  echo "${result,}"
}

csv_file="$1"
if [[ ! -f "$csv_file" ]]; then
  echo "Error: Expected file $csv_file does not exist."
  exit 1
fi

work_dir=$(dirname "$0")
cd "$work_dir"

output_dir=mappings
rm -r ./$output_dir 2>/dev/null || true
mkdir -p ./$output_dir

description_mapping=$output_dir/description.csv
name_mapping=$output_dir/name.csv
variable_file=$output_dir/variables.csv
type_mapping=$output_dir/types.csv
hierarchy_file=$output_dir/hierarchy.csv
group_file=$output_dir/group.csv
category_mapping=$output_dir/category.csv
sub_category_mapping=$output_dir/sub_category.csv

while read -r line; do
  category=$(echo "$line" | cut -d';' -f1)
  sub_category=$(echo "$line" | cut -d';' -f2)
  name=$(echo "$line" | cut -d';' -f3)
  description=$(echo "$line" | cut -d';' -f4-)
  category_variable=$(convertToCamelCase "$category")
  sub_category_variable=$(convertToCamelCase "$sub_category")
  name_variable=$(convertToCamelCase "$name")
  echo "$name_variable: \"$description\"," >> ./$description_mapping
  echo "$name_variable: \"$name\"," >> ./$name_mapping
  echo "$name_variable" >> ./$variable_file
  echo "$name_variable: \"YesNoComponent\"," >> ./$type_mapping
  echo "$category_variable","$sub_category_variable","$name_variable" >> ./$hierarchy_file
  echo "$category_variable: \"$category\"," >> ./$category_mapping
  echo "$sub_category_variable: \"$sub_category\"," >> ./$sub_category_mapping
done < "$csv_file"

awk '!seen[$0]++' ./$category_mapping > ./tmp.csv
mv ./tmp.csv ./$category_mapping
awk '!seen[$0]++' ./$sub_category_mapping > ./tmp.csv
mv ./tmp.csv ./$sub_category_mapping

for sub_category in $(cat ./$hierarchy_file | cut -d',' -f2 | sort -u); do
  all_fields=""
  for field in $(grep ",$sub_category," ./$hierarchy_file | cut -d',' -f3); do
    all_fields+="\"$field\","
  done
  echo "\"$sub_category\" : [${all_fields::-1}]," >> ./$group_file
done




