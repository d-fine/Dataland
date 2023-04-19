#!/bin/bash
set -euo pipefail

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

addFieldSnippet () {
  local field_name=$1
  local field_label=$2
  local field_description=$3
  local field_component=$4
  local field_dependency=$5
cat >> "$json_file" <<FIELDSNIPPET
{
  name: "$field_name",
  label: "$field_label",
  description: "$field_description",
  component: "$field_component",
  dependency: "$field_dependency",
},
FIELDSNIPPET
}

startCategory () {
  local category_name=$1
  local category_label=$2
cat >> "$json_file" <<CATEGORYSTART
{
  name: "$category_name",
  label: "$category_label",
  color: "<COLOR>",
  categories: [
CATEGORYSTART
}

endCategory () {
  echo "]," >> "$json_file"
  echo "}," >> "$json_file"
}

startSubCategory () {
  local sub_category_name=$1
  local sub_category_label=$2
cat >> "$json_file" <<SUBCATEGORYSTART
{
name: "$sub_category_name",
label: "$sub_category_label",
fields: [
SUBCATEGORYSTART
}

endSubCategory () {
  echo "] as Field[]," >> "$json_file"
  echo "}," >> "$json_file"
}

resolveDependency () {
  local dependency=$1
  local count="${dependency//[^.]}"
  count="${#count}"
  local reference=""
  if [[ $count -ne 0 ]]; then
    reference=$(echo "$dependency" | cut -d'.' -f1-"$count")
    reference_field=$(grep ";$reference;" "$fields_file" | cut -d';' -f2)
    if [[ $reference_field == "" ]]; then
      echo "Unable to identify referenced field for $reference using dependency $dependency" >> $error_file
      exit 1
    fi
    echo "this.<VAR_NAME>.data?.${category_name}?.${sub_category_name}?.${reference_field}"
  fi
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


hierarchy_file=$output_dir/hierarchy.csv
categories_file=$output_dir/categories.csv
sub_categories_file=$output_dir/sub_categories_file.csv
json_file=$output_dir/model.json
fields_file=$output_dir/fields.csv
error_file=$output_dir/error.log

while read -r line; do
  category=$(echo "$line" | cut -d';' -f1)
  sub_category=$(echo "$line" | cut -d';' -f2)
  dependency=$(echo "$line" | cut -d';' -f3)
  name=$(echo "$line" | cut -d';' -f4)
  label=$(echo "$line" | cut -d';' -f5)
  component=$(echo "$line" | cut -d';' -f6)
  echo "Processing: $name"
  category_variable=$(convertToCamelCase "$category")
  sub_category_variable=$(convertToCamelCase "$sub_category")
  name_variable=$(convertToCamelCase "$name")
  echo "$category_variable","$sub_category_variable","$name_variable" >> $hierarchy_file
  echo "$sub_category_variable;$name_variable;$name;$component;$dependency;$label;$category_variable" >> $fields_file
  echo "$category;$sub_category" >> $sub_categories_file
  echo "$category" >> $categories_file
done < "$csv_file"

awk '!seen[$0]++' $categories_file > tmp.csv
mv tmp.csv $categories_file
awk '!seen[$0]++' $sub_categories_file > tmp.csv
mv tmp.csv $sub_categories_file

echo "export const <DATA_MODEL_NAME> = [" > "$json_file"

while read -r line; do
  category_label=$(echo "$line" | cut -d';' -f1)
  category_name=$(convertToCamelCase "$category_label")
  echo "Processing: $category_label"
  startCategory "$category_name" "$category_label"
  while read -r entry; do
    current=$(echo "$entry" | cut -d';' -f1)
    if [[ "$category_label" == "$current" ]]; then
      sub_category_label=$(echo "$entry" | cut -d';' -f2)
      sub_category_name=$(convertToCamelCase "$sub_category_label")
      echo "Adding Sub-Category $sub_category_label to category $category_label with current being $current"
      startSubCategory "$sub_category_name" "$sub_category_label"
      for field in $(grep ";$category_name$" $fields_file | grep "^$sub_category_name;" | cut -d';' -f2); do
        echo "Adding field: $field"
        config=$(grep ";$field;" $fields_file)
        field_label=$(echo "$config" | cut -d';' -f3)
        field_description=$(echo "$config" | cut -d';' -f6)
        field_component=$(echo "$config" | cut -d';' -f4)
        field_dependency=$(echo "$config" | cut -d';' -f5)
        dependency_mapped=$(resolveDependency "$field_dependency")
        addFieldSnippet "$field" "$field_label" "$field_description" "$field_component" "$dependency_mapped"
      done
      endSubCategory
    fi
  done < "$sub_categories_file"
  endCategory
done < "$categories_file"

echo "];" >> "$json_file"
