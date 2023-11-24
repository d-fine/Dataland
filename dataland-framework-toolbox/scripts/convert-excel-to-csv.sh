#!/usr/bin/env bash
set -euxo pipefail

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

"/c/Program Files/LibreOffice/program"/scalc.exe --headless \
  --convert-to csv:"Text - txt - csv (StarCalc)":44,34,UTF8,1,,1033,false,true,false,false,false,-1 \
  --outdir "$2" \
  "$1"