#!/usr/bin/env bash
set -euxo pipefail

rm -f testing/data/*

./gradlew :dataland-frontend:npm_run_fakefixtures --no-daemon --stacktrace

git add testing/data/*
if git diff --exit-code --stat HEAD -- testing/data/; then
  echo "Fake-Fixtures are OK!"
  exit 0
else
  echo "Fake-Fixtures changed during re-generation. Either you forgot to update them or the generation code is non-deterministic!"
  exit 1
fi
