#!/usr/bin/env bash
set -euo pipefail

#The script is intended to provide a fast way for the developer to delete datasets using the company isin
#Currently this script is written for eutaxonomy-non-financials and eutaxnomy-financials
#Inputs necessary for this script are the server environment (e.g. dev1.dataland.com) and the api key of the data_admin
#on this environment

server=$1
key=$2
isin_file=isin_non_financial.txt
company_file=company.txt
data_file=data.txt

#loop over non financials and financials
for framework in eutaxonomy-non-financials eutaxonomy-financials; do
  isin_file=isin_$framework.txt
while read -r isin; do
  echo "Processing ISIN $isin"
  curl -X GET "https://$server/api/companies?searchString=$isin" -H 'accept: application/json' -H "Authorization: Bearer $key" | python -m json.tool --sort-keys > "$company_file"
  company_id=$(grep "companyId" "$company_file" | cut -d'"' -f4 | sort -u)
  echo "Found company ID $company_id for ISIN $isin"
  curl -X 'GET' "https://$server/api/data/$framework/companies/$company_id?showOnlyActive=true" -H 'accept: application/json' -H "Authorization: Bearer $key" | python -m json.tool --sort-keys > "$data_file"
  data_id=$(grep "dataId" "$data_file" | cut -d'"' -f4 | sort -u)
  echo "Found data ID $data_id for ISIN $isin"
  curl -X 'DELETE' "https://$server/api/data/$data_id" -H 'accept: */*' -H "Authorization: Bearer $key"

done < $isin_file
done