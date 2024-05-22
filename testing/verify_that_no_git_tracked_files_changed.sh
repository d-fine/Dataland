#!/usr/bin/env bash
set -euxo pipefail

git add --all

# Ignore changes in OpenApi files as they are verified in a separate GitHub action
if git diff --exit-code --stat HEAD -- :!*OpenApi.json; then
  echo "Okay! There are no changed files."
  exit 0
else
  echo "Error! Some files have changed."
  exit 1
fi
