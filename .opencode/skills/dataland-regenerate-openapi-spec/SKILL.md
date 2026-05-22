---
name: dataland-regenerate-openapi-spec
description: Regenerate a Dataland service OpenAPI spec and identify the downstream follow-up work
compatibility: opencode
---

## What I do

- Identify the producer service whose OpenAPI spec should be regenerated.
- Run the correct `:module:generateOpenApiDocs` task.
- Distinguish targeted regeneration from repo-wide OpenAPI verification.
- Point to likely downstream consumer modules that may need `generateClients`.

## When to use me

Use this when the user asks to regenerate an OpenAPI spec, when a backend contract changed, or when generated JSON files or downstream clients may be stale.

## Workflow

1. Identify the producer service.
2. Run `./gradlew :<service-module>:generateOpenApiDocs` from the repository root.
3. Check whether downstream consumers now need client regeneration.
4. If the task is repo-wide verification rather than targeted regeneration, use `testing/verifyOpenApiFiles.sh`.

## Producer services with generated OpenAPI JSON

- `dataland-backend`
- `dataland-api-key-manager`
- `dataland-accounting-service`
- `dataland-community-manager`
- `dataland-data-sourcing-service`
- `dataland-document-manager`
- `dataland-email-service`
- `dataland-external-storage`
- `dataland-internal-storage`
- `dataland-qa-service`
- `dataland-specification-service`
- `dataland-user-service`

## Dataland-specific rules

- Do not hand-edit generated `*OpenApi.json` files when regeneration from source is possible.
- Regenerate producer specs before regenerating consumer clients.
- Common downstream consumers to keep in mind are `dataland-frontend` and `dataland-e2etests`.
- If the user asks whether OpenAPI files are up to date across the repo, use `testing/verifyOpenApiFiles.sh` instead of guessing based on one module.

## Typical commands

- Targeted spec regeneration:
  - `./gradlew :<service-module>:generateOpenApiDocs`
- Repo-wide verification:
  - `testing/verifyOpenApiFiles.sh`

## Typical downstream follow-up

- Frontend consumer impact:
  - `./gradlew :dataland-frontend:generateClients`
  - frontend typecheck/build as appropriate
- E2E consumer impact:
  - `./gradlew :dataland-e2etests:generateClients`
  - `./gradlew :dataland-e2etests:compileTestKotlin`
- Other Kotlin consumers:
  - `./gradlew :<consumer-module>:generateClients`
  - the smallest compile or test task that exercises the consumer

## Output style

- State which producer service was selected.
- Give the exact regeneration command.
- List the likely consumers that need follow-up and the smallest verification steps.
