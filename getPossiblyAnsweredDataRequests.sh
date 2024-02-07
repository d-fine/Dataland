#!/usr/bin/env bash
set -euo pipefail

# This script delivers the IDs of data requests that open but for which there is data

TARGET=$1
ADMIN_BEARER_TOKEN=$2

#curl -L -s -X GET -H "accept: application/json" -H "Authorization: Bearer $ADMIN_BEARER_TOKEN" "https://$TARGET/community/requests"
REQUESTS=$(curl -L -s -X GET -H "accept: application/json" -H "Authorization: Bearer $ADMIN_BEARER_TOKEN" "https://$TARGET/community/requests" | jq -c '.[]')
#echo $REQUESTS
#  jq -c '.[] | { requestId: .dataRequestId, companyId: .dataRequestCompanyIdentifierValue, dataType: .dataType, reportingPeriod: .reportingPeriod}')

OUTPUT_FILE=request_ids.txt
rm $OUTPUT_FILE || true
touch $OUTPUT_FILE
for REQUEST in $REQUESTS; do
  REQUEST_ID=$(jq -r '.dataRequestId' <<< "$REQUEST")
  COMPANY_IDENTIFIER_TYPE=$(jq -r '.dataRequestCompanyIdentifierType' <<< "$REQUEST")
  COMPANY_IDENTIFIER=$(jq -r '.dataRequestCompanyIdentifierValue' <<< "$REQUEST")
  if [ "$COMPANY_IDENTIFIER_TYPE" = "DatalandCompanyId" ]; then
    COMPANY_IDS=("$COMPANY_IDENTIFIER")
  else
    COMPANY_IDS=$(curl -L -s -X GET -H "accept: application/json" -H "Authorization: Bearer $ADMIN_BEARER_TOKEN" "https://$TARGET/api/companies/names?searchString=$COMPANY_IDENTIFIER" | jq -c -r ".[].companyId")
  fi
  DATA_TYPE=$(jq -r '.dataType' <<< "$REQUEST")
  REPORTING_PERIOD=$(jq -r '.reportingPeriod' <<< "$REQUEST")
  for COMPANY_ID in ${COMPANY_IDS[@]}; do
    echo "Checking request \"$REQUEST_ID\" for company \"$COMPANY_ID\", data type \"$DATA_TYPE\" and reporting period \"$REPORTING_PERIOD\""
    RESPONSE=$(curl -L -s -w '\n%{http_code}' -X GET -H "accept: application/json" -H "Authorization: Bearer $ADMIN_BEARER_TOKEN" "https://$TARGET/api/metadata?companyId=$COMPANY_ID&dataType=$DATA_TYPE&reportingPeriod=$REPORTING_PERIOD")
    RESPONSE_ARRAY=()
    while read LINE; do RESPONSE_ARRAY+=("$LINE"); done <<< "$RESPONSE"
    RESPONSE_BODY="${RESPONSE_ARRAY[0]}"
    HTTP_STATUS="${RESPONSE_ARRAY[1]}"
    if [ "$HTTP_STATUS" -eq 404 ]; then
      echo -e "\tCompany not found"
      continue
    elif [ "$HTTP_STATUS" -ne 200 ]; then
      echo -e "\tUnexpected error response for meta info retrieval. HTTP status \"HTTP_STATUS\""
      continue
    fi
    META_INFOS=$(jq -c ".[]" <<< "$RESPONSE_BODY")
    for META_INFO in $META_INFOS; do
      echo -e "\tRequest possibly fulfilled"
      echo "$REQUEST_ID" >> "$OUTPUT_FILE"
      break
    done
  done
done
echo "Finished processing requests"