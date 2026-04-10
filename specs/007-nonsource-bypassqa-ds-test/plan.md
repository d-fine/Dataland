# Implementation Plan: bypassQa=true Test with Data Sourcing Integration

**Branch**: `007-nonsource-bypassqa-ds-test` | **Date**: 2026-04-10 | **Spec**: [spec.md](spec.md)
**Input**: Feature specification from `/specs/007-nonsource-bypassqa-ds-test/spec.md`

## Summary

Add a single new `@Test` method to `NonSourceabilityTest.kt` covering the `bypassQa=true` path with Data Sourcing integration. The test initialises a DS entity, posts non-sourceability with bypass, asserts QA service has no review rows, asserts the backend entry is immediately active, and asserts the DS entity reaches `NonSourceable`. Two new small helpers are required; all other helpers are reused from features 005/006. Zero new dependencies.

## Technical Context

**Language/Version**: Kotlin on JVM 21  
**Primary Dependencies**: JUnit 5, Awaitility (`awaitUntilAsserted`), auto-generated OpenAPI clients (`dataSourcingService.openApiClient`, `datalandbackend.openApiClient`, `datalandqaservice.openApiClient`)  
**Storage**: N/A (test-only; reads/writes via REST against a running stack)  
**Testing**: `./gradlew dataland-e2etests:test --tests "org.dataland.e2etests.tests.NonSourceabilityTest.*"`  
**Target Platform**: Running local Dataland stack (`manageLocalStack.sh --start --simple`)  
**Project Type**: Integration/E2E test  
**Performance Goals**: N/A  
**Constraints**: Async DS state assertion must complete within `awaitUntilAsserted` default (2s, 500ms poll); QA absence assertion is synchronous (no polling)  
**Scale/Scope**: 1 new test method + 2 new private helpers in 1 existing file

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principle | Status | Notes |
|---|---|---|
| I. Contract-First Service Boundaries | ✅ PASS | Consumes existing contracts; no new contracts |
| II. Backward-Compatible Messaging | ✅ PASS | No new messages |
| III. Microservice Autonomy | ✅ PASS | E2E test requires full stack — expected for E2E by definition |
| IV. Mandatory Test Coverage | ✅ PASS | This IS the test; adds bypassQa coverage with DS lifecycle |
| V. Traceability & Operational Clarity | ✅ PASS | Polling tolerates async DS propagation |
| VI. Minimal Dependencies | ✅ PASS | Zero new dependencies |

**Post-design re-check**: All gates still pass. Single file change; no violations.

## Project Structure

### Documentation (this feature)

```text
specs/007-nonsource-bypassqa-ds-test/
├── plan.md         ← this file
├── research.md     ← Phase 0 output
├── data-model.md   ← Phase 1 output
└── tasks.md        ← Phase 2 output (/speckit.tasks)
```

### Source Code (one file changed)

```text
dataland-e2etests/src/test/kotlin/org/dataland/e2etests/tests/
└── NonSourceabilityTest.kt   ← only file changed
```

**Structure Decision**: Single file. No new files, no new modules, no new dependencies.

## Complexity Tracking

> No constitution violations. No complexity justification needed.

## Phase Plan

### Phase 0: Research *(complete)*

→ See [research.md](research.md)

All unknowns resolved:
- `bypassQa=true` POST response contains `qaStatus=Accepted` and `currentlyActive=true` synchronously.
- QA service receives no event on bypass path → review list is empty; synchronous assertion sufficient.
- DS state after bypass acceptance: `NonSourceable` (same terminal state as QA-accepted path, same event).
- `assertDsStateIsNonSourceableVerification` intermediate check included for consistency with other tests.
- All required helpers (`initializeDataSourcing`, `assertDsStateIsNonSourceableVerification`, `assertBackendEntryIsAcceptedAndActive`, `assertDsStateIsNonSourceable`) already exist in the file.

### Phase 1: Design *(complete)*

→ See [data-model.md](data-model.md)

New helpers:

| Helper | Signature | Notes |
|---|---|---|
| `postNonSourceableWithBypassQa` | `(ctx: Ctx): Unit` | Posts with `bypassQa=true`; asserts response `qaStatus=Accepted`, `currentlyActive=true` |
| `assertNoQaReviewRowExists` | `(ctx: Ctx): Unit` | Synchronous GET; asserts empty list |

Test method call sequence:
```
initializeDataSourcing → assertDsStateIsNonSourceableVerification
→ postNonSourceableWithBypassQa → assertNoQaReviewRowExists
→ assertBackendEntryIsAcceptedAndActive → assertDsStateIsNonSourceable
```
