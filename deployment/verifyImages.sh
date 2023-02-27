#!/bin/bash
set -euxo pipefail

if [[ "$(docker ps | awk '{print $2}' | grep -c _test:)" -eq 0 ]]; then
  exit 0
else
  exit 1
fi