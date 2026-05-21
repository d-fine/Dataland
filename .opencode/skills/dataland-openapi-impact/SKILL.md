---
name: dataland-openapi-impact
description: Analyze Dataland OpenAPI and generated-client impact for a change
compatibility: opencode
---

## What I do

- Decide whether an API-related change requires regenerating OpenAPI specs or clients.
- Identify likely downstream consumers that need verification.
- Prevent hand-editing generated artifacts when regeneration is the correct workflow.

## When to use me

Use this when a change touches backend controllers, request or response models, generated clients, or any failing consumer that might be caused by contract drift.

## Workflow

1. Determine whether the change affects a producer contract:
   - controller endpoints
   - request and response DTOs
   - serialization shape
   - generated API client inputs or outputs
2. If yes, assume generated artifacts may be stale.
3. Regenerate in producer-to-consumer order:
   - producing service: `./gradlew :<service-module>:generateOpenApiDocs`
   - affected consumer modules: `./gradlew :<consumer-module>:generateClients`
4. Verify downstream consumers compile or pass their scoped checks.

## Dataland-specific impact hints

- `dataland-frontend` is a frequent consumer of generated clients.
- `dataland-e2etests` is another important consumer and often surfaces contract drift quickly.
- CI also verifies generated OpenAPI files and fake fixtures through:
  - `testing/verifyOpenApiFiles.sh`
  - `testing/verify_that_fake_fixtures_are_up_to_date.sh`

## Rules

- Do not hand-edit generated `*OpenApi.json` files when regeneration from source is possible.
- Do not hand-edit generated clients to work around stale specifications.
- If a frontend or E2E module breaks after backend API changes, regenerate the relevant producer and consumer artifacts before debugging deeper.
- If generated files change, include them in the verification scope and make sure the relevant consumer still builds or compiles.

## Typical verification

- Producer change only:
  - `./gradlew :<service-module>:generateOpenApiDocs`
  - relevant `:test` task
- Frontend consumer impact:
  - `./gradlew :dataland-frontend:generateClients`
  - frontend lint/typecheck/build as appropriate
- E2E consumer impact:
  - `./gradlew :dataland-e2etests:generateClients`
  - `./gradlew :dataland-e2etests:compileTestKotlin` or `:test`
- Repo-level generated-artifact verification:
  - `testing/verifyOpenApiFiles.sh`
  - `testing/verify_that_fake_fixtures_are_up_to_date.sh` when fixtures are involved

## Output style

- State whether regeneration is required, likely required, or probably not required.
- List the likely producer and consumer modules.
- List the minimal follow-up verification steps.
