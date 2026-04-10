# Feature Specification: bypassQa=true Test with Data Sourcing Integration

**Feature Branch**: `007-nonsource-bypassqa-ds-test`
**Created**: 2026-04-10
**Status**: Draft
**Input**: User description: "Add bypassQa=true test with DS and no QA service interaction"

## User Scenarios & Testing *(mandatory)*

### User Story 1 — bypassQa=true path skips QA entirely but transitions DS to NonSourceable (Priority: P1)

An operator initialises a Data Sourcing request for a company, then posts a non-sourceability entry with `bypassQa=true`. Because QA is bypassed, the entry must be accepted immediately (no pending state, no QA decision step). The QA service must not hold any review row for this entry. The DS entity must end in the `NonSourceable` state — the same terminal state that the accepted QA path reaches.

**Why this priority**: This is the only user story in this feature; it completes the test coverage matrix for `NonSourceabilityTest` and proves that the QA-bypass and QA-lifecycle paths converge to the same DS terminal state.

**Independent Test**: Run the new `@Test` method against a running stack. A passing run proves all four acceptance scenarios below hold simultaneously.

**Acceptance Scenarios**:

1. **Given** a DS request has been initialised for a company and reporting period, **When** a non-sourceability entry is posted with `bypassQa=true`, **Then** the returned entry has `qaStatus=Accepted` and `currentlyActive=true` immediately, without any further action.
2. **Given** the entry has been posted with `bypassQa=true`, **When** the QA service is queried for review rows matching the company/dataType/reportingPeriod triple, **Then** no review rows are found.
3. **Given** the entry has been posted with `bypassQa=true`, **When** the backend is queried for the non-sourceability entry, **Then** the entry has `qaStatus=Accepted` and `currentlyActive=true`.
4. **Given** the entry has been posted with `bypassQa=true` and the DS entity was initialised beforehand, **When** the DS entity is retrieved, **Then** its state is `NonSourceable`.

### Edge Cases

- The existing `POST metadata nonSourceable followed by GET` test already covers `bypassQa=true` without DS. The new test must not break or duplicate that test; it differs by initialising DS first and asserting the DS state.
- No QA review row must exist; the assertion must verify absence (empty list), not merely that no row is `Pending`.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: The test MUST initialise a DS entity via `initializeDataSourcing` before posting non-sourceability.
- **FR-002**: The test MUST post non-sourceability with `bypassQa=true` and assert the response has `qaStatus=Accepted` and `currentlyActive=true`.
- **FR-003**: The test MUST assert that querying the QA service for review rows returns an empty list (no QA interaction occurred).
- **FR-004**: The test MUST assert (with `awaitUntilAsserted`) that the backend entry has `qaStatus=Accepted` and `currentlyActive=true`.
- **FR-005**: The test MUST assert (with `awaitUntilAsserted`) that the DS entity state is `NonSourceable`.
- **FR-006**: All API calls MUST be made via `asAdmin`.
- **FR-007**: The test MUST reuse `Ctx` (with `dataSourcingId`) and follow helper naming and style conventions already established in `NonSourceabilityTest`.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: The new `@Test` method passes against a running stack without flakiness.
- **SC-002**: All four acceptance scenarios are verified by a single test run.
- **SC-003**: No existing test in `NonSourceabilityTest` is broken; the full class continues to pass.

## Assumptions

- `bypassQa=true` causes the backend to set `qaStatus=Accepted` synchronously; the `awaitUntilAsserted` guard on the backend and DS state assertions is kept as a safety net for propagation delays.
- The DS state transition to `NonSourceable` is triggered by the same backend event path as after a QA acceptance decision.
- `postNonSourceableWithBypassQa` is a new private helper that mirrors `postNonSourceableAndAssertPending` but with `bypassQa=true`, asserting immediate `Accepted`/`currentlyActive=true`. No `nonSourceabilityId` return is needed since the QA service is not called.
- `assertNoQaReviewRowExists` is a new private helper asserting the QA service returns an empty list for the given triple.