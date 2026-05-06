# Project Overview

- Dataland is a monorepo with many Kotlin/Spring Boot services, shared backend libraries, a Vue 3 frontend, an Astro website, and end-to-end test modules.
- Backend modules are built with Gradle Kotlin DSL and communicate via REST and RabbitMQ.
- The main user-facing frontend lives in `dataland-frontend/`. The static website lives in `dataland-website/`. Shared frontend code lives in `dataland-sharedElements/`.
- OpenAPI specifications and generated clients are part of normal development workflows. Changes in one service can require regenerating specs or clients in dependent modules.

# Working In This Monorepo

- Prefer scoped commands over repo-wide commands when your change is limited to one module.
- Use Gradle project paths for backend and mixed modules: `./gradlew :dataland-backend:test`.
- Frontend package commands should usually be run from the package directory or via `npm --prefix ./<package> run <script>`.
- Follow the nearest `AGENTS.md` file for package-specific instructions. Nested files override root guidance when they are more specific.
- Package-specific `AGENTS.md` files currently exist for `dataland-frontend/`, `dataland-website/`, `dataland-sharedElements/`, `dataland-e2etests/`, `dataland-keycloak/dataland_theme/login/`, and `dataland-framework-toolbox/`; consult them when working in those directories.

# Where To Look First

- CI behavior and command truth source: `.github/workflows/CI.yaml`
- Workflow helper sources: `testing/` and `build-utils/`
- Broad local smoke checks: `./runBasicChecks.sh` and `./runBasicChecks.sh short`
- OpenAPI verification script: `testing/verifyOpenApiFiles.sh`
- Fake fixture verification script: `testing/verify_that_fake_fixtures_are_up_to_date.sh`

# Agentic AI Workflow

- For ambiguous, cross-module, or architecture-heavy work, prefer a planning pass before editing.
- For read-heavy investigation or review, prefer subagents so the main implementation thread stays focused.

# Common Commands

- Repo-wide local fix commands:
  - `./gradlew ktlintFormat`
- Repo-wide CI-style verification commands:
  - `./gradlew ktlintCheck`
  - `./gradlew detekt`
- Targeted backend tests:
  - `./gradlew :<module>:test`
- Generate OpenAPI specs or clients for a specific module:
  - `./gradlew :<module>:generateOpenApiDocs`
  - `./gradlew :<module>:generateClients`
- Broad repo smoke checks:
  - `./runBasicChecks.sh`
  - `./runBasicChecks.sh short`

# CI Alignment

- Prefer local checks that mirror CI when you are finishing a change.
- CI explicitly verifies:
  - `./gradlew ktlintCheck detekt`
  - backend module tests for the Spring services and shared backend libraries
  - the package-specific frontend, website, E2E, and Keycloak theme checks described in their nested `AGENTS.md` files
  - `testing/verify_that_fake_fixtures_are_up_to_date.sh`
  - `testing/verifyOpenApiFiles.sh`
- When choosing between a local fix command and a CI-style verification command, use the fix command while editing and the CI-style command before finishing when practical.

# Verification By Change Type

- Backend Kotlin changes:
  - Run `./gradlew ktlintFormat`.
  - Run `./gradlew :<affected-module>:test`.
  - Run `./gradlew detekt` when touching production Kotlin code, shared code, or multiple backend modules.
- Cross-cutting or uncertain changes:
  - Prefer `./runBasicChecks.sh short` before finishing.
- `./runBasicChecks.sh` must be run from the repository root.
- `./runBasicChecks.sh short` skips setup steps and is useful when generated clients and local state are already in place.
- Full `./runBasicChecks.sh` assumes the backend is not already running locally.

# Never Do These By Default

- Never hand-edit generated OpenAPI clients when they can be regenerated from source.
- Never hand-edit generated `*OpenApi.json` files unless there is a documented repo workflow that requires it.
- Never introduce a new frontend UI library when PrimeVue already fits the need.
- Never introduce new `@ts-nocheck` directives in modified frontend files.
- Never autowire a fresh `ObjectMapper` where `defaultObjectMapper` from `dataland-backend-utils` should be used.
- Never jump straight to repo-wide checks when a scoped module or package check is sufficient, unless the change is clearly cross-cutting.

# Backend Guidelines

- Use Kotlin and Spring Boot for backend services.
- Use Gradle as the build tool.
- Implement endpoints in a RESTful style.
- Use `dataland-backend-utils` for shared functionality when an existing utility already fits.
- Use `BaseIntegrationTest` for integration tests.
- Use `org.mockito.kotlin` for mocking in JUnit tests; do not use plain Mockito APIs unless already required by surrounding code.
- Use the `defaultObjectMapper` from `dataland-backend-utils`; do not autowire a new `ObjectMapper`.
- If you add a database entity in a service that already uses Flyway, add the corresponding migration.

# Frontend Guidelines

- Use Vue 3 and TypeScript; never introduce plain JavaScript or Vue 2 patterns.
- Always use the Composition API, not the Options API.
- Always use `<script setup lang="ts">` in Vue single-file components.
- Use PrimeVue and PrimeIcons for UI components. Do not add alternative UI libraries unless explicitly required.
- Use PrimeVue default styling where possible. Add scoped styling only when necessary.
- If you touch frontend files, do not introduce new `@ts-nocheck` usage in the modified files.

# Testing Expectations

- There are three test levels in the repo:
  - Unit tests inside backend services or frontend components.
  - Integration tests for service-plus-database flows or whole frontend pages.
  - End-to-end tests for important user journeys across services and frontend.
- Backend tests use JUnit.
- Frontend tests use Cypress:
  - Component tests for unit/integration-level frontend coverage.
  - E2E tests for full user journeys.
- Add or update tests when behavior changes, especially for user-facing features, API changes, and bug fixes.
- When changing APIs, data models, persistence behavior, or generated clients, verify downstream consumers still compile and relevant tests still pass.

# Generated OpenAPI And Clients

- Several services publish OpenAPI specs that are used to generate clients for other modules.
- If you change controller contracts, request or response models, or generated API client inputs, expect local generated artifacts to become stale.
- Regenerate the producing service spec first, then regenerate clients in affected consumer modules.
- Common downstream consumers to keep in mind are `dataland-frontend` and `dataland-e2etests`, both of which have dedicated regeneration workflows.
- Do not manually patch generated clients to work around stale specifications. Regenerate first.
- Do not manually patch generated OpenAPI JSON as a substitute for updating the producing service code.
- Common commands:
  - `./gradlew :<service-module>:generateOpenApiDocs`
  - `./gradlew :<consumer-module>:generateClients`
- Verification scripts:
  - `testing/verifyOpenApiFiles.sh` checks that generated OpenAPI files are up to date.
  - `testing/verify_that_fake_fixtures_are_up_to_date.sh` checks that fake fixtures are up to date after regeneration.
- If generated files change, include them in the verification scope and make sure dependent builds still pass.

# Troubleshooting

- If a frontend or test module fails after backend API changes, regenerate the relevant OpenAPI specs and clients before debugging further.
- `dataland-website` output is consumed by the frontend build, so website changes can require frontend verification as well.
- If you are unsure which checks approximate CI best, use `./runBasicChecks.sh short` for a faster pass or `./runBasicChecks.sh` for the fuller workflow.
- When editing only one area of the monorepo, avoid unnecessary repo-wide runs unless the change affects shared contracts, generated code, or multiple modules.
