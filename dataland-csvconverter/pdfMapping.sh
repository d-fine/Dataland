#!/bin/bash
set -euxo pipefail
# Utility script to generate a mapping between a folder of pdf reports and the ISINs in the real data set. 
# Expects two inputs: the csv file containing the real data and the folder with the pdfs for mapping
# The output is a folder "pdfs" containing all relevant pdfs and the mapping file
# In order to check which pdfs to not comply with the expected format run:
# ls *.pdf | grep -Pv '_[A-Z0-9]{12}_(Annual|Sustainability|Integrated)'
# inside the folder containing the pdfs

csv_file="$1"
pdf_folder="$2"

if [[ ! -f "$csv_file" ]]; then
  echo "Error: Expected file $csv_file does not exist."
  exit 1
fi

output_folder=$(dirname "$0")/pdfs
mkdir -p "$output_folder"
rm "$output_folder"/*.pdf
output_file=$output_folder/output.csv

echo "Annual Report,Sustainability Report,Integrated Report" > "$output_file"
for isin in $(grep -oP ';([A-Z0-9]{12});' $csv_file | cut -d';' -f2); do
  annual_report=$(find "$pdf_folder" -name "*${isin}_Annual*.pdf" -printf "%f\n")
  sustainability_report=$(find "$pdf_folder" -name "*${isin}_Sustainability*.pdf" -printf "%f\n")
  integrated_report=$(find "$pdf_folder" -name "*${isin}_Integrated*.pdf" -printf "%f\n")
  echo "$annual_report,$sustainability_report,$integrated_report" >> "$output_file"
  cp -p "$pdf_folder"/{"$annual_report","$sustainability_report","$integrated_report"} "$output_folder" 2>/dev/null || true
done

echo "Script finished. Output can be found here: $output_folder"
