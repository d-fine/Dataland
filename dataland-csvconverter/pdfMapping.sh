#!/bin/bash
set -euxo pipefail
# Utility script to generate a mapping between a folder of pdf reports and the ISINs in the real data set. 
# Expects two inputs: the csv file containing the real data and the folder with the pdfs for mapping

csv_file="$1"
pdf_folder="$2"
output_file=$pdf_folder/output.csv

if [[ ! -f "$csv_file" ]]; then
  echo "Error: Expected file $csv_file does not exist."
  exit 1
fi

echo "Annual Report,Sustainability Report,Integrated Report" > "$output_file"
for isin in $(grep -oP ';([A-Z0-9]{12});' $csv_file | cut -d';' -f2); do
  annual_report=$(find "$pdf_folder" -name "*${isin}_Annual*.pdf" -printf "%f\n")
  sustainability_report=$(find "$pdf_folder" -name "*${isin}_Sustainability*.pdf" -printf "%f\n")
  integrated_report=$(find "$pdf_folder" -name "*${isin}_Integrated*.pdf" -printf "%f\n")
  echo "$annual_report,$sustainability_report,$integrated_report" >> "$output_file"
done

echo "Script finished. Output file can be found here: $output_file"
