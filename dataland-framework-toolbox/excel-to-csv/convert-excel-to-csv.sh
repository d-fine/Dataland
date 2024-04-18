#!/usr/bin/env bash
set -euxo pipefail

## Note: This script only works when LibreOffice is available in the PATH. This especially does not hold on
# windows machines. If you still want to use this script you have to locally replace "libreoffice"
# by the path to the libreoffice executable on your machine (e.g. "/c/Program Files/LibreOffice/program"/scalc.exe).

## PARAMETER REFERENCE (https://help.libreoffice.org/latest/en-GB/text/shared/guide/csv_params.html)
# 44 - Use ',' to seperate fields
# 34 - Use '"' to format text
# UTF-8 - Use UTF-8 output formatting
# 1 - Start at the first line
# BLANK - No special column formatters
# 1033 - Use "English - United States" Formatting
# false - Quoted field as text --> NO (default)
# true - Detect special numbers (default)
# false - Save cell contents as shown --> No
# false - Export cell formulas --> No
# false - Remove spaces --> No
# -1 - Export all sheets

temporary_directory="$(mktemp -d)"

echo "Starting CSV conversion to $temporary_directory" >&2
libreoffice --headless \
  --convert-to csv:"Text - txt - csv (StarCalc)":44,34,UTF8,1,,1033,false,true,false,false,false,-1 \
  --outdir "$temporary_directory" \
  "$1" >&2

echo "Retrieving converted CSV" >&2
cat "$temporary_directory/$2"
