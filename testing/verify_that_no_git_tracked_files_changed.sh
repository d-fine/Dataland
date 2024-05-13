#!/usr/bin/env bash
set -euxo pipefail

git add --all
if git diff --exit-code --stat HEAD --; then
  echo "Okay! There are no changed files."
  exit 0
else
  echo "Error! Some files have changed."
  exit 1
fi
