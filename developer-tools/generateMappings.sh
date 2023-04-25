#!/bin/bash
set -euxo pipefail

convertToCamelCase () {
  local string=$(echo "$1" | tr -cs '[:alnum:] ' ' ')
  local result=""
  local lower=""
  for part in $string; do
    lower=${part,,}
    result+=${lower^}
  done
  echo "${result,}"
}
if [ "$#" -ne 1 ]
then
  echo "Usage: ./generateMappings.sh <fileName>.csv"
  exit 1
fi

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

description_mapping=$output_dir/description.txt
name_mapping=$output_dir/name.txt
variable_file=$output_dir/variables.txt
type_mapping=$output_dir/types.txt

while read -r line; do
  name=$(echo "$line" | cut -d';' -f1)
  description=$(echo "$line" | cut -d';' -f2)
  component=$(echo "$line" | cut -d';' -f3)
  variable=$(convertToCamelCase "$name")
  echo "$variable: \"$description\"," >> ./$description_mapping
  echo "$variable: \"$name\"," >> ./$name_mapping
  echo "$variable" >> ./$variable_file
  echo "$variable: \"$component\"," >> ./$type_mapping
done < "$csv_file"


