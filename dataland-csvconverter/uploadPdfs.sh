#!/bin/bash
set -euxo pipefail
# Utility script to upload all PDFs in the pdfs folder via the dataland API

url="$1"
token="$2"
workdir=$(dirname "$0")
cd "$workdir"

for file in ./pdfs/*.pdf; do
  id=$(sha256sum "$file" 2>/dev/null | awk '{print $1}') || true
  response=$(curl -X GET "https://$url/documents/$id/exists" -H 'accept: application/json' -H "Authorization: Bearer $token")
  echo "$response"
  if [[ $response == '{"documentExists":true}' ]]; then
    echo "File $file already uploaded. Skipping."
    continue
  fi
  echo "Uploading $file"
  curl -X POST "https://$url/documents/" -H 'accept: application/json' -H "Authorization: Bearer $token" \
  -H 'Content-Type: multipart/form-data' -F "pdfDocument=@$file;type=application/pdf"
  exit 0
done

echo Finished uploading PDFs