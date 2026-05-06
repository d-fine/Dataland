---
name: dataland-change-checks
description: Choose the smallest correct Dataland verification steps for a change
compatibility: opencode
---

## What I do

- Map a change to the smallest correct local verification steps.
- Prefer scoped module or package checks before repo-wide checks.
- Escalate to broader checks only when the change is cross-cutting or the impact is unclear.

## When to use me

Use this when you need to decide which checks to run before finishing a change in the Dataland monorepo.

## Workflow

1. Identify which area changed:
   - backend Kotlin/Spring service or shared backend library
   - `dataland-frontend`
   - `dataland-website`
   - `dataland-sharedElements`
   - `dataland-e2etests`
   - `dataland-keycloak/dataland_theme/login`
   - `dataland-framework-toolbox`
   - automation or CI scripts under `.github/`, `testing/`, or `build-utils/`
2. Start with the nearest `AGENTS.md` guidance for the touched package.
3. Pick the smallest verification set that matches the change.
4. Expand the verification scope if any of these are true:
   - contracts or generated clients changed
   - shared package output changed
   - multiple modules changed
   - CI workflow or helper scripts changed
   - the local impact is uncertain

## Repo-specific rules

- Backend Kotlin changes:
  - Start with `./gradlew ktlintFormat` when formatting is needed.
  - Run `./gradlew :<affected-module>:test`.
  - Add `./gradlew detekt` when touching production Kotlin code, shared code, or multiple backend modules.
- Frontend changes in `dataland-frontend`:
  - Usually run `npm run lint`, `npm run typecheck`, and `npm run build`.
  - Prefer CI-style checks before finishing: `npm run lintci`, `npm run formatci`, `npm run checkdependencies`.
  - Add Cypress compilation or component tests when tests, fixtures, or user-facing behavior changed.
- Website changes:
  - Usually run `npm run lintci`, `npm run formatci`, `npm run typecheck`.
  - Add `npm run build` when rendered output, routes, or Vue components used by the site changed.
- Shared elements:
  - Always run `npm run build` in `dataland-sharedElements`.
  - Also verify affected consumers, usually `dataland-frontend` and/or `dataland-website`.
- E2E changes:
  - Use `./gradlew :dataland-e2etests:compileTestKotlin` for a scoped fast check.
  - Run `./gradlew :dataland-e2etests:test` when feasible.
- Framework toolbox changes:
  - Run `./gradlew :dataland-framework-toolbox:test` for normal Kotlin changes.
  - Add `integrationTest`, `runCreateFrameworkList`, or `runCoverage --args='<framework>'` when generation logic or framework inputs changed.

## Escalation rules

- Prefer `./runBasicChecks.sh short` when the change crosses module boundaries or the right scope is unclear.
- Use full `./runBasicChecks.sh` only when you intentionally want the broader local smoke workflow.
- Remember that `./runBasicChecks.sh` must run from repo root, and the full mode expects the backend not to already be running.

## Output style

- Return a concise verification plan.
- Separate required checks from optional or nice-to-have checks.
- Explain why the scope was chosen when the answer is not obvious.
