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
rm "$output_folder"/*.pdf || true
output_file=$output_folder/output.csv

echo "Annual Report File,Sustainability Report File,Integrated Report File,Annual Report,Sustainability Report,Integrated Report" > "$output_file"
for isin in $(grep -oP ';([A-Z0-9]{12});' $csv_file | cut -d';' -f2); do
  annual_report_file=$(find "$pdf_folder" -name "*${isin}_Annual*.pdf" -printf "%f\n")
  sustainability_report_file=$(find "$pdf_folder" -name "*${isin}_Sustainability*.pdf" -printf "%f\n")
  integrated_report_file=$(find "$pdf_folder" -name "*${isin}_Integrated*.pdf" -printf "%f\n")
  annual_report_hash=$(sha256sum "$pdf_folder/$annual_report_file" 2>/dev/null | awk '{print $1}') || true
  sustainability_report_hash=$(sha256sum "$pdf_folder/$sustainability_report_file" 2>/dev/null | awk '{print $1}') || true
  integrated_report_hash=$(sha256sum "$pdf_folder/$integrated_report_file" 2>/dev/null | awk '{print $1}') || true
  echo "$annual_report_file,$sustainability_report_file,$integrated_report_file,$annual_report_hash,$sustainability_report_hash,$integrated_report_hash" >> "$output_file"
  cp -p "$pdf_folder"/{"$annual_report_file","$sustainability_report_file","$integrated_report_file"} "$output_folder" 2>/dev/null || true
done

echo "Script finished. Output can be found here: $output_folder"
