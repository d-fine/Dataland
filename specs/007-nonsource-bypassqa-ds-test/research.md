# Research: bypassQa=true Test with Data Sourcing Integration

**Feature**: `007-nonsource-bypassqa-ds-test` | **Date**: 2026-04-10

All findings are derived from feature 006 research (already verified against the
codebase) and the existing `NonSourceabilityTest.kt` implementation. No new unknowns.

---

## Finding 1 — bypassQa=true response shape

**Decision**: Use `createdEntry.qaStatus` and `createdEntry.currentlyActive` directly from the POST response.
**Rationale**: The existing `POST metadata nonSourceable followed by GET` test already validates that `bypassQa=true` yields an immediately accepted, active entry in the same POST response. No GET is needed to observe acceptance.
**Alternatives considered**: GET after POST — redundant; FR-004 covers a dedicated `assertBackendEntryIsAcceptedAndActive` poll anyway.

---

## Finding 2 — QA service: no review row created for bypassQa=true

**Decision**: Assert `getNonSourceableReviews(...)` returns an empty list. Use a synchronous (non-polled) assertion.
**Rationale**: The backend only emits the non-sourceability-created event to the QA service when `bypassQa=false`. With `bypassQa=true` the entry is accepted synchronously in the backend without publishing to the QA topic; the QA service therefore never creates a review row.
**Alternatives considered**: Poll to confirm no row appears — unnecessary; absence is guaranteed by the backend's synchronous bypass path. Polling would only add latency without robustness benefit.

---

## Finding 3 — DS state after bypassQa=true acceptance

**Decision**: Assert `DataSourcingState.NonSourceable` using the existing `assertDsStateIsNonSourceable(ctx)` helper (polls with `awaitUntilAsserted`).
**Rationale**: The backend emits the same "non-sourceability accepted" domain event regardless of whether acceptance came from a QA decision or a bypass. The DS service listener transitions the entity from `NonSourceableVerification` to `NonSourceable` on that event. This is identical to the terminal state in the QA-accepted path.
**Alternatives considered**: Synchronous GET — too fragile; the DS transition is event-driven and may lag the POST response by up to the `awaitUntilAsserted` timeout.

---

## Finding 4 — `postNonSourceableWithBypassQa` helper design

**Decision**: New private helper, `void` return type. Posts with `bypassQa=true` and immediately asserts `qaStatus=Accepted` and `currentlyActive=true` on the response.
**Rationale**: No QA decision is ever posted, so `nonSourceabilityId` is not needed. The helper mirrors the structure of `postNonSourceableAndAssertPending` for readability and consistency.
**Alternatives considered**: Inline the POST in the `@Test` body — duplicates assertion logic, reduces readability.

---

## Finding 5 — `assertNoQaReviewRowExists` helper design

**Decision**: Synchronous GET of `getNonSourceableReviews`, assert the list is empty.
**Rationale**: No event is sent to the QA service on the bypass path, so no eventual consistency gap exists. A synchronous assertion is robust — a non-empty result unambiguously indicates a backend regression.
**Alternatives considered**: Poll to confirm absence — adds latency without benefit; the baseline for this assertion is zero events.

---

## Finding 6 — Intermediate DS state assertion before bypass POST

**Decision**: Call `assertDsStateIsNonSourceableVerification(ctx)` before the bypass POST, mirroring the structure of the accepted- and rejected-path tests.
**Rationale**: The spec does not explicitly require this, but the pattern is established in feature 006 tests. It confirms the DS entity reached `NonSourceableVerification` before the non-sourceability event arrives, making the test read as a coherent state-machine progression and catching DS initialization regressions.
**Alternatives considered**: Skip the intermediate assertion — simpler, but breaks the symmetry with the other two tests and removes a useful checkpoint.
