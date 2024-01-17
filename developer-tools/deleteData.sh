#!/usr/bin/env bash
set -euo pipefail

key=$1
#key="MTM2YTkzOTQtNDg3My00YTYxLWEyNWItNjViMWU4ZTdjYzJm_e74cf78e106fe605299138e432512b791485367753c03a60d9088ce421f0141189f945fe428c8a7f_2733262788"
isin_file=isin_non_financial.txt
company_file=company.txt
data_file=data.txt

#TODO create backup before deleting the actual data
#TODO make sure it works for the intended data
#TODO check that the companies are gone from the company search page

#loop over non financials and financials
for framework in eutaxonomy-non-financials eutaxonomy-financials; do
  isin_file=isin_$framework.txt
while read -r isin; do
  echo "Processing ISIN $isin"
  echo "https://local-dev.dataland.com/api/companies?searchString=$isin"
  curl -X 'GET' "https://local-dev.dataland.com/api/companies?searchString=$isin" -H 'accept: application/json' -H "Authorization: Bearer $key" | python -m json.tool --sort-keys > "$company_file"
  company_id=$(grep "companyId" "$company_file" | cut -d'"' -f4 | sort -u)
  echo "Found company ID $company_id for ISIN $isin"
  curl -X 'GET' "https://local-dev.dataland.com/api/data/$framework/companies/$company_id?showOnlyActive=true" -H 'accept: application/json' -H "Authorization: Bearer $key" | python -m json.tool --sort-keys > "$data_file"
  data_id=$(grep "dataId" "$data_file" | cut -d'"' -f4 | sort -u)
  echo "Found data ID $data_id for ISIN $isin"
  curl -X 'DELETE' "https://local-dev.dataland.com/api/data/$data_id" -H 'accept: */*' -H "Authorization: Bearer $key"

  #TODO add command for deletion
done < $isin_file
done