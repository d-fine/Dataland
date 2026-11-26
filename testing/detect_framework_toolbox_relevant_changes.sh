#!/usr/bin/env bash
set -euxo pipefail
# Decides whether the framework-toolbox consistency tests have to run.
# They only verify that checked-in generated code matches what the toolbox generates, so they are only
# relevant if the generator, its inputs or its generated outputs changed.
# Always runs on main (safety net). If anything is unclear, the tests are run.
# Writes runConsistencyTests=true|false to $GITHUB_OUTPUT.

relevant_paths=(
  "dataland-framework-toolbox/"
  "dataland-specification-lib/"
  "dataland-specification-service/src/main/resources/specifications/"
  "dataland-backend/src/main/kotlin/org/dataland/datalandbackend/frameworks/"
  "dataland-qa-service/src/main/kotlin/org/dataland/datalandqaservice/frameworks/"
  "dataland-frontend/src/frameworks/"
  "dataland-frontend/tests/e2e/fixtures/frameworks/"
  "testing/verify_that_no_git_tracked_files_changed.sh"
)

output_file="${GITHUB_OUTPUT:-/dev/null}"

set_result() {
  echo "runConsistencyTests=$1" >> "$output_file"
  echo "runConsistencyTests=$1"
  exit 0
}

branch="${GITHUB_REF_NAME:-$(git rev-parse --abbrev-ref HEAD)}"
if [[ "$branch" == "main" ]]; then
  echo "On main: always run the consistency tests."
  set_result true
fi

base=$(git merge-base origin/main HEAD 2>/dev/null || true)
if [[ -z "$base" ]]; then
  echo "Could not determine merge-base with origin/main: running the tests to be safe."
  set_result true
fi

if ! changed=$(git diff --name-only "$base" HEAD -- "${relevant_paths[@]}"); then
  echo "git diff failed: running the tests to be safe."
  set_result true
fi

if [[ -n "$changed" ]]; then
  echo "Relevant files changed:"
  echo "$changed"
  set_result true
fi

echo "No relevant changes compared to origin/main: skipping the consistency tests."
set_result false
