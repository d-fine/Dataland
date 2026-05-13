# Frontend Package

- This package contains the main Vue 3 application for Dataland.
- Use Vue 3 with TypeScript and the Composition API.
- Always use `<script setup lang="ts">` in Vue single-file components.
- Use PrimeVue and PrimeIcons for UI work. Reuse existing patterns before introducing new abstractions.

# Commands

- Install dependencies: `npm install`
- Start local dev server: `npm run dev`
- Format files: `npm run format`
- Check formatting without changes: `npm run formatci`
- Lint files: `npm run lint`
- Lint in CI mode: `npm run lintci`
- Type-check: `npm run typecheck`
- Check dependencies: `npm run checkdependencies`
- Run component tests: `npm run testcomponent`
- Check Cypress E2E TypeScript compilation: `npm run checkcypresscompilation`
- Check fake fixture compilation: `npm run checkfakefixturecompilation`
- Generate fake fixtures: `npm run fakefixtures`

# Verification

- For most code changes, run:
  - `npm run lint`
  - `npm run typecheck`
- Before finishing, prefer CI-style verification with:
  - `npm run lintci`
  - `npm run formatci`
  - `npm run checkdependencies`
  - `npm run checkcypresscompilation` when Cypress TypeScript inputs changed
- When changing Cypress tests, support code, or fixture generation, also run the relevant Cypress compilation checks.
- When changing user-facing behavior, run the most relevant Cypress component or E2E tests.
- If the change depends on backend API contracts, regenerate clients with `./gradlew :dataland-frontend:generateClients` from the repo root.

# Package-Specific Rules

- Do not introduce new `@ts-nocheck` directives in modified files.
- Prefer existing shared components, composables, stores, and framework configuration patterns before adding new ones.
- Keep styling aligned with PrimeVue defaults unless there is a clear need for custom scoped styles.
- This package depends on `dataland-sharedElements`, `dataland-website` and on generated OpenAPI clients. Changes in either may require rebuilding this package.
