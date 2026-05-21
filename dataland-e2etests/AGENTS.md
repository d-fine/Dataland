# E2E Package

- This module contains Kotlin-based end-to-end tests and generated API clients used for higher-level system verification.
- Treat it as a consumer of multiple backend service contracts. Backend API changes often require client regeneration here.
- Test data is copied from `testing/data` during the Gradle workflow.

# Commands

- Run tests: `./gradlew :dataland-e2etests:test`
- Regenerate all required API clients: `./gradlew :dataland-e2etests:generateClients`
- Compile tests and generated clients: `./gradlew :dataland-e2etests:compileTestKotlin`
- Refresh copied test resources: `./gradlew :dataland-e2etests:getTestData`

# Verification

- If you change E2E test code, run `./gradlew :dataland-e2etests:test` when feasible.
- If you change backend API contracts consumed here, run `./gradlew :dataland-e2etests:generateClients` before debugging compilation issues.
- For quicker verification after API or fixture changes, `./gradlew :dataland-e2etests:compileTestKotlin` is a useful scoped check.
- If your change touches shared test data under `testing/data`, make sure `getTestData` still succeeds and relevant tests still pass.

# Package-Specific Rules

- Do not hand-edit generated API clients under this module.
- Prefer regenerating clients and recompiling before changing production or test code to work around contract drift.
- Keep end-to-end coverage focused on important user journeys and cross-service flows.
