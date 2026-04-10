# Implementation Plan: NonSourceability QA Tests — Data Sourcing Integration & Rejected Path

**Branch**: `006-nonsource-qa-datasourcing` | **Date**: 2026-04-10 | **Spec**: [spec.md](spec.md)
**Input**: Feature specification from `/specs/006-nonsource-qa-datasourcing/spec.md`

## Summary

Extend `NonSourceabilityTest.kt` with Data Sourcing Service lifecycle assertions in the existing accepted-path test, and add a new rejected-path test. Both changes live in one existing file. Zero new dependencies — all required clients (`dataSourcingRequestControllerApi`, `dataSourcingControllerApi`) and utilities (`awaitUntilAsserted`, `GlobalAuth`) are already available.

## Technical Context

**Language/Version**: Kotlin on JVM 21  
**Primary Dependencies**: JUnit 5, Awaitility (`awaitUntilAsserted`), auto-generated OpenAPI clients (`dataSourcingService.openApiClient`, `datalandbackend.openApiClient`, `datalandqaservice.openApiClient`)  
**Storage**: N/A (test-only; reads/writes via REST against a running stack)  
**Testing**: `./gradlew dataland-e2etests:test --tests "org.dataland.e2etests.tests.NonSourceabilityTest.*"`  
**Target Platform**: Running local Dataland stack (`manageLocalStack.sh --start --simple`)  
**Project Type**: Integration/E2E test  
**Performance Goals**: N/A  
**Constraints**: All async DS state assertions must complete within `awaitUntilAsserted` default (2s, 500ms poll); `dataSourcingEntityId` from `patchRequestState` is nullable — non-null assert required  
**Scale/Scope**: 1 modified test method + 1 new test method in 1 existing file; +3 new private helper methods

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principle | Status | Notes |
|---|---|---|
| I. Contract-First Service Boundaries | ✅ PASS | Consumes existing contracts; no new contracts |
| II. Backward-Compatible Messaging | ✅ PASS | No new messages |
| III. Microservice Autonomy | ✅ PASS | E2E test requires full stack — expected for E2E by definition |
| IV. Mandatory Test Coverage | ✅ PASS | This IS the test; adds DS lifecycle coverage |
| V. Traceability & Operational Clarity | ✅ PASS | Polling tolerates async propagation |
| VI. Minimal Dependencies | ✅ PASS | Zero new dependencies |

**Post-design re-check**: All gates still pass. Single file change; no violations.

## Project Structure

### Documentation (this feature)

```text
specs/006-nonsource-qa-datasourcing/
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
- DS initialization flow: `createRequest` (via `asAdmin`) → `requestId`; `patchRequestState(Processing)` (via `asAdmin`) → `dataSourcingEntityId`.
- DS state fetch: `getDataSourcingById(dataSourcingEntityId).state`.
- DS state enum values confirmed: `DataSourcingState.NonSourceableVerification`, `DataSourcingState.NonSourceable`.
- `dataSourcingEntityId` is nullable on `StoredRequest`; non-null assert required.
- `createRequest` uses `asAdmin` — same as all other calls in this test file.
- DS state `NonSourceableVerification` is set asynchronously → polling required.
- DS state after `Rejected` is unchanged → no polling needed (assert synchronously).

### Phase 1: Design *(complete)*

→ See [data-model.md](data-model.md)

**contracts/**: Not applicable — no new API contracts; feature consumes existing DS service API.

**quickstart.md**: Not applicable — no new setup steps; running stack required (same as feature 005).

**Key design decisions**:

1. **Extend `Ctx`** with `dataSourcingId: String? = null`. Default `null` keeps the existing bypassQa=true test unchanged. New tests populate this field.

2. **DS initialization helper** `initializeDataSourcing(companyId: String, ctx: Ctx): String`:
   - Creates request as `TechnicalUser.PremiumUser`
   - Patches state as `TechnicalUser.Admin`
   - Returns `dataSourcingEntityId` (non-null asserted)

3. **Three new private helpers**:
   - `assertDsStateIsNonSourceableVerification(dataSourcingId: String)` — polled
   - `assertDsStateIsNonSourceable(dataSourcingId: String)` — polled
   - `assertDsStateIsUnchanged(dataSourcingId: String, expected: DataSourcingState)` — synchronous (pass in the state captured before the QA decision)

4. **Accepted-path test** — append to existing call sequence:
   ```
   (existing) val nonSourceabilityId = postNonSourceableAndAssertPending(ctx)
   (existing) assertBackendEntryIsPending(ctx)
   (existing) assertQaReviewRowAppears(ctx)
   (new)      assertDsStateIsNonSourceableVerification(dataSourcingId)
   (existing) postQaDecision(nonSourceabilityId)
   (existing) assertQaReviewIsAccepted(ctx)
   (existing) assertBackendEntryIsAcceptedAndActive(ctx)
   (new)      assertDsStateIsNonSourceable(dataSourcingId)
   ```

5. **Rejected-path test** — new `@Test` method, mirrors setup then diverges at QA decision:
   ```
   initializeDataSourcing(...)  → dataSourcingId
   postNonSourceableAndAssertPending(ctx)
   assertBackendEntryIsPending(ctx)
   assertQaReviewRowAppears(ctx)
   assertDsStateIsNonSourceableVerification(dataSourcingId)
   postQaDecisionRejected(nonSourceabilityId)   ← reuse existing postQaDecision with Rejected
   assertQaReviewIsRejected(ctx)
   assertBackendEntryIsRejectedAndInactive(ctx)
   assertDsStateIsUnchanged(dataSourcingId, DataSourcingState.NonSourceableVerification)
   ```

6. **Rejected-path helpers** reuse existing helpers where possible (`assertQaReviewRowAppears`, `assertBackendEntryIsPending`); add minimal new ones (`assertQaReviewIsRejected`, `assertBackendEntryIsRejectedAndInactive`).

### Phase 2: Tasks *(not created here — use /speckit.tasks)*

**Estimated implementation scope**:
- Extend `Ctx` data class (1 line change)
- Add 3 new imports (`DataSourcingState`, `RequestState`, `SingleRequest`)
- Add `initializeDataSourcing` helper (~12 lines)
- Add `assertDsStateIsNonSourceableVerification` helper (~7 lines)
- Add `assertDsStateIsNonSourceable` helper (~7 lines)
- Add `assertDsStateIsUnchanged` helper (~5 lines)
- Add `assertQaReviewIsRejected` helper (~8 lines)
- Add `assertBackendEntryIsRejectedAndInactive` helper (~10 lines)
- Modify accepted-path test body (~3 lines added)
- Add rejected-path test method (~15 lines)

**User stories to task-ify**:
- US1 (P1): Extend accepted-path test with DS assertions
- US2 (P2): Add rejected-path test with full assertion sequence
