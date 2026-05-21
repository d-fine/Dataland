---
name: dataland-frontend-impact
description: Evaluate frontend, website, and shared-elements verification impact in Dataland
compatibility: opencode
---

## What I do

- Decide how a change in `dataland-frontend`, `dataland-website`, or `dataland-sharedElements` should be verified.
- Call out consumer impact, generated client impact, and fixture or Cypress implications.

## When to use me

Use this when a change touches the main Vue frontend, the Astro website, or the shared frontend package.

## Workflow

1. Identify which frontend area changed.
2. Start with the package-specific `AGENTS.md` guidance.
3. Check whether the change also affects another consumer package.
4. Add verification for generated clients, fake fixtures, or Cypress compilation when the change reaches those workflows.

## Area-specific guidance

### `dataland-frontend`

- Standard verification:
  - `npm run lint`
  - `npm run typecheck`
  - `npm run build`
- CI-style verification before finishing:
  - `npm run lintci`
  - `npm run formatci`
  - `npm run checkdependencies`
- Add when relevant:
  - `npm run checkcypresscompilation` for Cypress TypeScript inputs
  - `npm run checkfakefixturecompilation` or `npm run fakefixtures` for fake fixture generation changes
  - `npm run testcomponent` for user-facing component behavior
  - `./gradlew :dataland-frontend:generateClients` when backend API contracts changed

### `dataland-website`

- Standard verification:
  - `npm run lintci`
  - `npm run formatci`
  - `npm run typecheck`
- Add `npm run build` when rendered pages, route structure, markdown/content pipelines, or shared Vue components changed.
- Remember that website output is consumed by the frontend build, so some website changes also require frontend verification.

### `dataland-sharedElements`

- Always run `npm run build`.
- Then verify affected consumers:
  - `dataland-frontend`
  - `dataland-website`
- Be careful with exported paths and public interfaces because consumer breakage can be indirect.

## Cross-package rules

- A `dataland-sharedElements` change usually requires checking at least one consumer, often both.
- A `dataland-website` change can require `dataland-frontend` verification because the frontend build consumes website output.
- A backend API change that surfaces in the frontend usually requires client regeneration before deeper debugging.
- Do not introduce new `@ts-nocheck` directives in modified frontend files.

## Output style

- Identify the touched frontend area.
- List required checks first.
- List any additional consumer or generated-client checks separately with a short reason.
