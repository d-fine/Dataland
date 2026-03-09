# Dataland — Project Structure & Test Instructions

This file summarizes the repo layout and common commands to run tests locally.

## Project structure (high level)

- Top-level scripts and build files:
  - `gradlew`, `build.gradle.kts`, `settings.gradle.kts` (Gradle build)
  - `docker-compose.yml`, `manageLocalStack.sh` (local dev stack)
  - `runBasicChecks.sh` (convenience script to run linters/tests)
- Per-service modules: `dataland-<service>/` (e.g. dataland-backend, dataland-frontend, dataland-user-service, dataland-document-manager, dataland-email-service, dataland-accounting-service, etc.)
- Infrastructure & tooling:
  - `dataland-keycloak/`, `dataland-postgres-v1/`, `dataland-rabbitmq/`, `dataland-grafana/`, `dataland-loki/`, `dataland-pgadmin/`
  - `build-utils/`, `base-dockerfiles/`, `localstack/`, `local/`
- Tests & QA helpers: `dataland-e2etests/`, `dataland-qa-service/`, frontend Cypress config inside `dataland-frontend/`.

## Prerequisites

- Java 21+ (set `JAVA_HOME`)
- Node.js 24+
- Docker running (for integration/e2e/local stack)
- Recommended: add hosts entries for `local-dev.dataland.com` (see README)

## Quick test commands

Start local stack (required for many integration/e2e tests):

```bash
./manageLocalStack.sh --reset --start --simple
```

Run the convenience pipeline (linters, frontend checks, some tests):

```bash
./runBasicChecks.sh        # full mode
./runBasicChecks.sh short  # skip setup steps
```

Run backend unit tests (single module):

```bash
./gradlew dataland-backend:test
```

Run all Gradle checks / build (runs configured tests):

```bash
./gradlew check
./gradlew build
```

Run backend e2e tests (requires local stack running):

```bash
./gradlew dataland-e2etests:test
```

Frontend test commands (inside repo root):

```bash
npm --prefix ./dataland-frontend run testcomponent        # component/unit tests
npm --prefix ./dataland-frontend run cypress             # open Cypress UI
npm --prefix ./dataland-frontend run testpipeline -- --env EXECUTION_ENVIRONMENT=""  # headless pipeline
```

Run a single service's Gradle tests (example service name):

```bash
./gradlew :dataland-user-service:test
```

## Logs and failure traces

- `runBasicChecks.sh` writes logs to `./log/` (one file per check).
- Gradle prints task output to the terminal; use `--stacktrace` or `--info` / `--debug` flags for more detail.

## Notes / tips

- If tests depend on the running stack, start `manageLocalStack.sh` first.
- Some frontend Cypress tests may require manual interaction when run in headed mode; prefer headless `testpipeline` for CI-like runs.
- CI pipelines may run a subset of these commands; consult the repository CI config if unsure.

## Next steps you might find useful

- Create per-service README snippets with exact test commands and ports.
- Generate a Mermaid architecture diagram for quick onboarding.
