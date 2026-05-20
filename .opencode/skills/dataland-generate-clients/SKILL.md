---
name: dataland-generate-clients
description: Generate Dataland OpenAPI clients for the correct consumer module and verify the result
compatibility: opencode
---

## What I do

- Choose the right consumer module for client generation.
- Run the correct `:module:generateClients` command from the repository root.
- Point to the smallest useful follow-up verification for that consumer.

## When to use me

Use this when the user asks to generate or refresh clients, or when a consumer module is failing because backend API contracts may be stale.

## Workflow

1. Identify the consumer module that needs regenerated clients.
2. If the change touched an API producer contract, regenerate the producer OpenAPI spec first.
3. Run `./gradlew :<consumer-module>:generateClients` from the repo root.
4. Run the smallest follow-up verification that confirms the consumer still compiles or builds.

## Common consumer modules

- `dataland-frontend`
- `dataland-e2etests`
- `dataland-backend`
- `dataland-qa-service`
- `dataland-user-service`
- `dataland-document-manager`
- `dataland-community-manager`
- `dataland-data-sourcing-service`
- `dataland-accounting-service`
- `dataland-batch-manager`
- `dataland-internal-storage`
- `dataland-external-storage`
- `dataland-keycloak-adapter`

## Dataland-specific rules

- Run client generation from the repository root.
- Do not hand-edit generated clients to work around stale specs.
- If the user did not name the consumer module and it is unclear, ask one short question.
- If the consumer is `dataland-frontend`, remember that this is a frequent downstream consumer and usually the first place contract drift surfaces.
- If the consumer is `dataland-e2etests`, remember it also depends on copied test data and often uses `compileTestKotlin` as a fast check.

## Typical commands

- Frontend clients:
  - `./gradlew :dataland-frontend:generateClients`
- E2E clients:
  - `./gradlew :dataland-e2etests:generateClients`
- Backend or service consumers:
  - `./gradlew :<module>:generateClients`

## Typical follow-up verification

- `dataland-frontend`:
  - `npm --prefix ./dataland-frontend run typecheck`
  - `npm --prefix ./dataland-frontend run build`
- `dataland-e2etests`:
  - `./gradlew :dataland-e2etests:compileTestKotlin`
  - `./gradlew :dataland-e2etests:test` when feasible
- Kotlin service consumers:
  - `./gradlew :<module>:test` or the smallest compile/test task that exercises the generated client usage

## Output style

- State which consumer module was selected.
- State whether producer spec regeneration is needed first.
- Give the exact generation command and the minimal follow-up verification.
