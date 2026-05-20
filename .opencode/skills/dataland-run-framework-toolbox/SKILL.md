---
name: dataland-run-framework-toolbox
description: Run the correct Dataland framework-toolbox task and verify generator-side effects
compatibility: opencode
---

## What I do

- Choose the correct framework-toolbox task for the user's intent.
- Run toolbox tasks from the repository root.
- Remind the agent to check for unintended tracked-file changes after generator-style runs.

## When to use me

Use this when the user asks to run the framework toolbox, regenerate the framework list, execute toolbox consistency generation, or verify framework-toolbox behavior locally.

## Workflow

1. Determine which toolbox task matches the request.
2. Run the task from the repository root.
3. If the task generates outputs or transforms tracked files, verify that the resulting diff is intentional.

## Task selection

- Normal Kotlin code changes in the toolbox module:
  - `./gradlew :dataland-framework-toolbox:test`
- Integration behavior, framework loading, or input handling:
  - `./gradlew :dataland-framework-toolbox:integrationTest`
- Generate the framework list used by CI:
  - `./gradlew :dataland-framework-toolbox:runCreateFrameworkList`
- Run consistency generation for a specific framework:
  - `./gradlew :dataland-framework-toolbox:runCoverage --args='<framework>'`

## Dataland-specific rules

- These tasks assume the repository root as the working directory.
- Inputs under `dataland-framework-toolbox/inputs/` directly affect generation behavior.
- After `runCreateFrameworkList` or `runCoverage`, inspect tracked-file changes just like CI does.
- CI verifies generator-style runs with `testing/verify_that_no_git_tracked_files_changed.sh`.
- If the user asks to run consistency generation but does not specify a framework, ask one short question.

## CI parity hints

- CI first runs `runCreateFrameworkList` to build the framework matrix.
- CI then runs `runCoverage --args='<framework>'` per framework.
- CI follows generator-style runs with:
  - `./testing/verify_that_no_git_tracked_files_changed.sh`

## Practical notes

- If `runCoverage` fails because required environment variables are missing, mirror the local CI-style environment only as needed rather than changing code to bypass the failure.
- Prefer intentional output changes only. If the toolbox run changes tracked files unexpectedly, call that out clearly.

## Output style

- State which toolbox task was selected and why.
- Give the exact command.
- For generator-style runs, include the tracked-file verification step.
