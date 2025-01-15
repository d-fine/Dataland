#!/usr/bin/env bash
# This script generates 'n' number of testing PDFs using LibreOffice. Therefore, you need libreoffice installed on your machine.
set -euxo pipefail

n_files=5

for i in $(seq 1 $n_files); do
  echo "This is fake-fixture PDF number $i" > ./fake-fixture-pdf-"$i".txt
  soffice --headless --convert-to pdf ./fake-fixture-pdf-"$i".txt
  rm ./fake-fixture-pdf-"$i".txt
done
