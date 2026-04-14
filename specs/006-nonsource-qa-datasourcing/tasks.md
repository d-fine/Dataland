# Tasks: NonSourceability QA Tests — Data Sourcing Integration & Rejected Path

**Feature**: `006-nonsource-qa-datasourcing` | **Date**: 2026-04-10  
**Spec**: [spec.md](spec.md) | **Plan**: [plan.md](plan.md)  
**Target file**: `dataland-e2etests/src/test/kotlin/org/dataland/e2etests/tests/NonSourceabilityTest.kt`

---

## Phase 1 — Setup (Imports & Ctx)

- [X] T001 Add imports `DataSourcingState`, `RequestState`, `SingleRequest` from `dataSourcingService.openApiClient.model` in `NonSourceabilityTest.kt`
- [X] T002 Extend the `Ctx` data class with `dataSourcingId: String? = null` in `NonSourceabilityTest.kt`

---

## Phase 2 — Foundational Helpers (shared by both user stories)

- [X] T003 [P] Add `initializeDataSourcing(companyId: String): String` helper — calls `createRequest` then `patchRequestState(Processing)` both via `asAdmin`, returns `dataSourcingEntityId!!`; call site uses `ctx = ctx.copy(dataSourcingId = initializeDataSourcing(ctx.companyId))` in `NonSourceabilityTest.kt`
- [X] T004 [P] Add `assertDsStateIsNonSourceableVerification(ctx: Ctx)` helper — polls with `awaitUntilAsserted`, asserts `getDataSourcingById(ctx.dataSourcingId!!).state == DataSourcingState.NonSourceableVerification` in `NonSourceabilityTest.kt`
- [X] T005 [P] Add `assertDsStateIsNonSourceable(ctx: Ctx)` helper — polls with `awaitUntilAsserted`, asserts `getDataSourcingById(ctx.dataSourcingId!!).state == DataSourcingState.NonSourceable` in `NonSourceabilityTest.kt`
- [X] T005b [P] Refactor `postQaDecision(nonSourceabilityId: String)` to `postQaDecision(nonSourceabilityId: String, qaStatus: QaServiceQaStatus)` and update the existing accepted-path call site to pass `QaServiceQaStatus.Accepted` in `NonSourceabilityTest.kt`

---

## Phase 3 — User Story 1: Accepted path with DS assertions (P1)

**Story goal**: Extend the existing accepted-path test so it initialises a DS request before the QA lifecycle and asserts the DS state at two checkpoints.

**Independent test criteria**: Run `NonSourceabilityTest\`POST nonSourceable with bypassQa false triggers*\`` against a running stack — it should pass with the new DS state assertions.

- [X] T006 [US1] In the accepted-path `@Test` body, call `ctx = ctx.copy(dataSourcingId = initializeDataSourcing(ctx.companyId))` before `postNonSourceableAndAssertPending` in `NonSourceabilityTest.kt`
- [X] T007 [US1] Insert call to `assertDsStateIsNonSourceableVerification(ctx)` between `assertQaReviewRowAppears` and `postQaDecision` in the accepted-path `@Test` in `NonSourceabilityTest.kt`
- [X] T008 [US1] Append call to `assertDsStateIsNonSourceable(ctx)` at the end of the accepted-path `@Test` body in `NonSourceabilityTest.kt`

---

## Phase 4 — User Story 2: Rejected QA path test (P2)

**Story goal**: Add a new `@Test` method covering the rejection branch: QA rejects the entry, QA review shows `Rejected`, backend entry stays inactive, DS state unchanged.

**Independent test criteria**: Run the new rejected-path `@Test` against a running stack — all five acceptance scenarios from spec.md US2 should pass.

- [X] T009 [P] [US2] Add `assertQaReviewIsRejected(ctx: Ctx)` helper — GET QA reviews, assert `qaStatus == QaServiceQaStatus.Rejected` in `NonSourceabilityTest.kt`
- [X] T010 [P] [US2] Add `assertBackendEntryIsRejectedAndInactive(ctx: Ctx)` helper — polls with `awaitUntilAsserted`, asserts backend entry has `qaStatus == BackendQaStatus.Rejected` and `currentlyActive == false` in `NonSourceabilityTest.kt`
- [X] T011 [P] [US2] Add `assertDsStateIsUnchanged(ctx: Ctx, expected: DataSourcingState)` helper — synchronous GET on `ctx.dataSourcingId!!`, asserts state equals `expected` in `NonSourceabilityTest.kt`
- [X] T012 [US2] Add new `@Test` method for the rejected path: use `ctx = ctx.copy(dataSourcingId = initializeDataSourcing(ctx.companyId))`, call the full sequence ending with `postQaDecision(nonSourceabilityId, QaServiceQaStatus.Rejected)`, `assertQaReviewIsRejected(ctx)`, `assertBackendEntryIsRejectedAndInactive(ctx)`, `assertDsStateIsUnchanged(ctx, DataSourcingState.NonSourceableVerification)` in `NonSourceabilityTest.kt`

---

## Phase 5 — Polish

- [X] T013 Verify all new helpers follow the method naming and style conventions established in the file (no stray comments, consistent `asAdmin` wrapping, correct assertion messages)

---

## Dependencies

```
T001, T002 (setup) → T003, T004, T005, T005b (foundational helpers)
T003, T004, T005, T005b → T006, T007, T008 (US1 test body)
T003, T004, T005b → T009, T010, T011 (US2 helpers, can run parallel to US1)
T009, T010, T011 → T012 (US2 test body)
T006, T007, T008, T012 → T013 (polish)
```

## Parallel Execution

- T003, T004, T005, T005b can be written in parallel (independent helpers, different methods).
- T009, T010, T011 can be written in parallel with each other and with T006–T008.

## Implementation Strategy

**MVP = Phase 3 (US1)**: Extend the accepted-path test with DS assertions — delivers confidence in the full accepted-path state machine.  
**Phase 4 (US2)**: Add the rejected-path test — complements the happy path with the failure branch.
