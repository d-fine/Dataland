#!/bin/bash
set -euxo pipefail

if [[ "$(docker ps | awk '{print $2}' | grep -c _test:)" -eq 0 ]]; then
  exit 1
else
  exit 0
fi