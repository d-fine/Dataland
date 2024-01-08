#!/usr/bin/env bash

set -euxo pipefail

if find .coverage | grep -q . ; then
  mv .coverage .coverage.old
fi
coverage run -m pytest -o log_cli=true -o log_cli_level=INFO ./src/test/measure_coverage.py