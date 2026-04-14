# Tasks: bypassQa=true Test with Data Sourcing Integration

**Feature**: `007-nonsource-bypassqa-ds-test` | **Date**: 2026-04-10  
**Spec**: [spec.md](spec.md) | **Plan**: [plan.md](plan.md)  
**Target file**: `dataland-e2etests/src/test/kotlin/org/dataland/e2etests/tests/NonSourceabilityTest.kt`

---

## Phase 1 — Foundational Helpers

**Purpose**: Add the two new private helpers required by the test body. All other helpers are already present from features 005/006.

- [X] T001 [P] Add `postNonSourceableWithBypassQa(ctx: Ctx)` helper — posts with `bypassQa=true` via `asAdmin`, asserts response `qaStatus == BackendQaStatus.Accepted` and `currentlyActive == true` in `NonSourceabilityTest.kt`
- [X] T002 [P] Add `assertNoQaReviewRowExists(ctx: Ctx)` helper — synchronous `asAdmin` GET of `getNonSourceableReviews`, asserts the result list is empty in `NonSourceabilityTest.kt`

---

## Phase 2 — User Story 1: bypassQa=true test with DS (P1)

**Story goal**: Add a `@Test` method that initialises a DS entity, posts non-sourceability with `bypassQa=true`, and asserts all four acceptance scenarios from spec.md US1.

**Independent test criteria**: Run `NonSourceabilityTest\`POST nonSourceable with bypassQa true*\`` against a running stack — it should pass and verify the DS terminal state, the QA-absence, and the backend active state.

- [X] T003 [US1] Add `@Test fun \`POST nonSourceable with bypassQa true immediately accepts entry and transitions DS to NonSourceable\`` — initialise DS with `ctx = ctx.copy(dataSourcingId = initializeDataSourcing(ctx.companyId))`, then call `assertDsStateIsNonSourceableVerification(ctx)`, `postNonSourceableWithBypassQa(ctx)`, `assertNoQaReviewRowExists(ctx)`, `assertBackendEntryIsAcceptedAndActive(ctx)`, `assertDsStateIsNonSourceable(ctx)` in `NonSourceabilityTest.kt`

---

## Phase 3 — Polish

- [X] T004 Verify the two new helpers follow naming and style conventions (consistent `asAdmin` wrapping, assertion messages match the pattern of existing helpers, no stray comments) in `NonSourceabilityTest.kt`

---

## Dependencies

```
T001, T002 (helpers) → T003 (test body) → T004 (polish)
```

T001 and T002 can be written in parallel.

## Implementation Strategy

MVP = T001 + T002 + T003. T004 is a style-only review step with no functional impact.
