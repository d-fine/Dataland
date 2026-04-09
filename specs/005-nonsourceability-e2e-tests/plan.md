# Implementation Plan: NonSourceability QA Lifecycle E2E Test

**Branch**: `005-nonsourceability-e2e-tests` | **Date**: 2026-04-09 | **Spec**: [spec.md](spec.md)  
**Input**: Feature specification from `/specs/005-nonsourceability-e2e-tests/spec.md`

## Summary

Add a single JUnit E2E test to the existing `NonSourceabilityTest.kt` that exercises the full non-sourceability QA lifecycle: POST to the backend with `bypassQa=false` → verify pending/inactive state in backend → verify QA review row appears in QA service (async) → accept via QA service → verify QA row is accepted → verify backend entry is accepted and `currentlyActive=true` (async).

All required API clients (`metaDataControllerApi`, `nonSourceabilityQaControllerApi`) and auth utilities (`GlobalAuth`, `awaitUntilAsserted`) already exist. The only deliverable is filling in the test method in one existing file.

## Technical Context

**Language/Version**: Kotlin on JVM 21  
**Primary Dependencies**: JUnit 5 (`@Test`, `Assertions`), Awaitility (`awaitUntilAsserted`), auto-generated OpenAPI clients (`datalandbackend.openApiClient`, `datalandqaservice.openApiClient`)  
**Storage**: N/A (test-only; reads/writes via REST API against a running stack)  
**Testing**: `./gradlew dataland-e2etests:test --tests "org.dataland.e2etests.tests.NonSourceabilityTest.*"`  
**Target Platform**: Running local Dataland stack (`manageLocalStack.sh --start --simple`)  
**Project Type**: Integration/E2E test  
**Performance Goals**: N/A  
**Constraints**: All async assertions must complete within `awaitUntilAsserted` default (2 s, 500 ms poll)  
**Scale/Scope**: 1 new test method in 1 existing file

## Constitution Check

*Gate: Must pass before Phase 0 research. Re-checked after Phase 1 design.*

| Principle | Status | Notes |
|---|---|---|
| I. Contract-First Service Boundaries | ✅ PASS | Test consumes existing contracts; no new contracts defined |
| II. Backward-Compatible Messaging | ✅ PASS | No new messages introduced |
| III. Microservice Autonomy | ✅ PASS | E2E test requires full stack; this is expected for E2E tests by definition |
| IV. Mandatory Test Coverage | ✅ PASS | This IS the test; adds coverage for the backend→QA→backend lifecycle path |
| V. Traceability & Operational Clarity | ✅ PASS | Uses `awaitUntilAsserted` to tolerate async propagation |
| VI. Minimal Dependencies | ✅ PASS | Zero new dependencies; reuses existing clients and utilities |

**Constitution re-check post-design**: All gates still pass. Design adds one test method; no violations.

## Project Structure

### Documentation (this feature)

```text
specs/005-nonsourceability-e2e-tests/
├── plan.md         ← this file
├── research.md     ← Phase 0 output
├── data-model.md   ← Phase 1 output
└── tasks.md        ← Phase 2 output (/speckit.tasks — not created here)
```

### Source Code (one file changed)

```text
dataland-e2etests/src/test/kotlin/org/dataland/e2etests/tests/
└── NonSourceabilityTest.kt   ← add new @Test method here
```

**Structure Decision**: Single file change. No new files, no new modules, no new dependencies. All scaffolding (ApiAccessor, GlobalAuth, awaitUntilAsserted) already in place.

## Complexity Tracking

> No constitution violations. No complexity justification needed.

## Phase Plan

### Phase 0: Research *(complete)*

→ See [research.md](research.md)

All unknowns resolved:
- API client method signatures confirmed from generated client source.
- Auth pattern confirmed from existing tests.
- `awaitUntilAsserted` timeout/polling confirmed from source.
- `dataType` String vs. DataTypeEnum mismatch between backend and QA service clients identified and resolved (`DataTypeEnum.sfdr.value`).
- Two `QaStatus` enum types (backend vs. QA service) identified; must be imported unambiguously.
- `nonSourceabilityId` flows from POST response — no secondary lookup needed.

### Phase 1: Design *(complete)*

→ See [data-model.md](data-model.md)

**contracts/**: Not applicable — this feature adds no new API contracts or message schemas. It tests existing contracts defined in specs/004-unify-non-sourceability.

**quickstart.md**: Not applicable — running the test requires a live stack (described in AGENTS.md). No new setup steps.

**Key design decisions**:

1. **Extend existing class**: Add the new test method to `NonSourceabilityTest.kt`. The class already has the correct annotations, `apiAccessor`, and `testReportingPeriod`.

2. **Single test, linear flow**: All six assertions live in one `@Test` method. This matches the pattern in `DataRequestNonSourceableTest` and gives the clearest failure locality.

3. **Capture POST response for ID**: `val createdEntry = GlobalAuth.withTechnicalUser(Admin) { apiAccessor.metaDataControllerApi.postNonSourceabilityOfADataset(...) }` — `createdEntry.nonSourceabilityId` is used for the subsequent QA service call.

4. **Two `QaStatus` imports**: The `postNonSourceabilityDecision` call uses `org.dataland.datalandqaservice.openApiClient.model.QaStatus`, while backend GET assertions use `org.dataland.datalandbackend.openApiClient.model.QaStatus`. Alias one or fully qualify at call site.

5. **Two `awaitUntilAsserted` blocks**:
   - After POST: await QA service row with `qaStatus=Pending`.
   - After POST decision: await backend entry with `qaStatus=Accepted` and `currentlyActive=true`.

### Phase 2: Tasks *(not created here — use /speckit.tasks)*

**Estimated implementation scope**:
- 1 new `@Test` method (~50 lines) in `NonSourceabilityTest.kt`
- Updated imports in same file
- No other files changed

**User stories to task-ify**:
- US1 (P1): Full QA lifecycle test — the only story in scope

