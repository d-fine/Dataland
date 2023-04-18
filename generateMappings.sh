#!/bin/bash
set -euxo pipefail

convertToCamelCase () {
  local string="$1"
  local result=""
  local lower=""
  for part in $string; do
    lower=${part,,}
    result+=${lower^}
  done
  echo "${result,}"
}

csv_file="$1"
workdir=$(dirname "$0")
cd "$workdir"

description_mapping=description.csv
name_mapping=name.csv
variable_file=variables.csv
type_mapping=types.csv

rm "$description_mapping" "$name_mapping" "$variable_file" 2>/dev/null || true

if [[ ! -f "$csv_file" ]]; then
  echo "Error: Expected file $csv_file does not exist."
  exit 1
fi

while read -r line; do
  name=$(echo "$line" | cut -d';' -f1 | tr -cd '[:alnum:] ')
  description=$(echo "$line" | cut -d';' -f2-)
  variable=$(convertToCamelCase "$name")
  echo "$variable: \"$description\"," >> ./$description_mapping
  echo "$variable: \"$name\"," >> ./$name_mapping
  echo "$variable" >> ./$variable_file
  echo "$variable: \"YesNoComponent\"," >> ./$type_mapping
done < "$csv_file"


