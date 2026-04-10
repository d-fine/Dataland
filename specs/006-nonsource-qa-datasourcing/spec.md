# Feature Specification: NonSourceability QA Tests — Data Sourcing Integration & Rejected Path

**Feature Branch**: `006-nonsource-qa-datasourcing`  
**Created**: 2026-04-10  
**Status**: Draft  
**Input**: User description: "Extend NonSourceabilityTest: initialize Data Sourcing Service before QA lifecycle, assert Data Sourcing state is NonSourceableVerification after posting non-sourceability and NonSourceable after QA acceptance. Add new rejected QA path test: same lifecycle but QA decision Rejected, assert QA review is Rejected, backend entry is inactive, and Data Sourcing state is unchanged."

## User Scenarios & Testing *(mandatory)*

### User Story 1 — Accepted QA path includes Data Sourcing Service lifecycle assertions (Priority: P1)

A developer running the existing accepted-path E2E test wants confidence that the Data Sourcing Service transitions through the correct states as a non-sourceability entry moves through the QA lifecycle. Before the QA lifecycle starts, a Data Sourcing request is created and set to `Processing`. After the non-sourceability entry is posted and visible to the QA service, the Data Sourcing object should be in state `NonSourceableVerification`. After the QA decision is accepted and the backend entry becomes active, the Data Sourcing object should be in state `NonSourceable`.

**Why this priority**: The Data Sourcing Service is the upstream trigger for non-sourceability requests in real usage. Verifying its state transitions validates the full end-to-end state machine, not just backend/QA service in isolation.

**Independent Test**: Can be fully tested by extending the existing accepted-path test method in `NonSourceabilityTest.kt` with three additional steps (initialize, assert `NonSourceableVerification`, assert `NonSourceable`) and confirming the test passes against a running stack.

**Acceptance Scenarios**:

1. **Given** a fresh company and a Data Sourcing request in state `Processing`, **When** a non-sourceability entry is posted with `bypassQa=false` and the QA review row appears, **Then** `GET /data-sourcing/{dataSourcingId}` returns state `NonSourceableVerification`.
2. **Given** the QA review is in state `Pending`, **When** the QA decision `Accepted` is posted and the backend entry becomes `Accepted` and `currentlyActive=true`, **Then** `GET /data-sourcing/{dataSourcingId}` returns state `NonSourceable`.

---

### User Story 2 — Rejected QA path test (Priority: P2)

A developer wants a test that runs the same setup but exercises the rejection path: the QA reviewer rejects the non-sourceability entry, the QA review row reflects `Rejected`, the backend entry remains inactive (`currentlyActive=false`), and the Data Sourcing state does not advance beyond `NonSourceableVerification` (it remains unchanged, i.e., not `NonSourceable`).

**Why this priority**: The rejected path is the failure branch of the same state machine. Without it, the test suite only covers the happy path, leaving the rollback/inactive behaviour unverified.

**Independent Test**: Can be fully tested by adding a new `@Test` method in `NonSourceabilityTest.kt` that follows the same setup as US1 but posts `Rejected` as the QA decision, then verifies QA review is `Rejected`, backend entry is inactive, and Data Sourcing state has not changed to `NonSourceable`.

**Acceptance Scenarios**:

1. **Given** a fresh company, a Data Sourcing request in `Processing`, and a non-sourceability entry posted with `bypassQa=false`, **When** the QA review row appears, **Then** the QA review has `qaStatus=Pending`.
2. **Given** the QA review is `Pending`, **When** the QA decision `Rejected` is posted, **Then** the POST response reflects `qaStatus=Rejected`.
3. **Given** the QA decision is `Rejected`, **When** `GET /non-sourceability-qa` reviews are fetched, **Then** the review row shows `qaStatus=Rejected`.
4. **Given** the QA decision is `Rejected`, **When** the backend non-sourceability entries are fetched (polled), **Then** the entry has `qaStatus=Rejected` and `currentlyActive=false`.
5. **Given** the QA decision is `Rejected`, **When** `GET /data-sourcing/{dataSourcingId}` is called, **Then** the state is identical to what it was before the QA decision — nothing in the Data Sourcing Service has changed.

---

### Edge Cases

- What happens when the Data Sourcing Service has not yet transitioned to `NonSourceableVerification` by the time the QA review row appears? The assertion MUST poll with `awaitUntilAsserted` — DS state transitions are event-driven and async.
- What happens if the Data Sourcing object is not found? The test should fail with a clear error, not a silent pass.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: The test MUST initialize the Data Sourcing Service by: (1) calling `POST /requests` to obtain `dataRequestId`, then (2) calling `PATCH /requests/{dataRequestId}/state` with state `Processing` to obtain `dataSourcingEntityId` (used as `dataSourcingId` for all subsequent DS assertions). Both calls MUST occur before posting the non-sourceability entry.
- **FR-002**: After the QA review row appears with `Pending` status, the test MUST assert that `GET /data-sourcing/{dataSourcingId}` returns state `NonSourceableVerification`.
- **FR-003**: After the backend entry is `Accepted` and `currentlyActive=true`, the test MUST assert that `GET /data-sourcing/{dataSourcingId}` returns state `NonSourceable`.
- **FR-004**: A new test method MUST exercise the rejected path: same setup as the accepted path but with `QaStatus.Rejected` as the QA decision.
- **FR-005**: The rejected-path test MUST assert that the QA review POST response is `Rejected`.
- **FR-006**: The rejected-path test MUST assert that `GET /non-sourceability-qa` reviews show `qaStatus=Rejected` after the decision.
- **FR-007**: The rejected-path test MUST assert (with polling) that the backend entry has `qaStatus=Rejected` and `currentlyActive=false`.
- **FR-008**: The rejected-path test MUST assert that the Data Sourcing state is identical before and after the `Rejected` decision (i.e., nothing changes in the Data Sourcing Service).
- **FR-009**: Data Sourcing state assertions that depend on async event propagation MUST use `awaitUntilAsserted` to tolerate timing variability.

### Key Entities

- **DataSourcingObject**: Identified by `dataSourcingId`; has a `state` field that transitions through `Processing` → `NonSourceableVerification` → `NonSourceable` (accepted path) or remains at `NonSourceableVerification` (rejected path).
- **DataRequest**: `POST /requests` returns `requestId` (used as `dataRequestId`). `PATCH /requests/{dataRequestId}/state` sets state to `Processing` and returns `dataSourcingEntityId` (used as `dataSourcingId`).

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: The accepted-path test passes end-to-end with three additional Data Sourcing state assertions and zero new test failures.
- **SC-002**: The rejected-path test passes end-to-end, covering five acceptance scenarios across QA service, backend, and Data Sourcing Service.
- **SC-003**: All Data Sourcing state assertions complete within the same 2-second `awaitUntilAsserted` timeout used by all other async assertions in the test suite (no per-assertion timeout overrides).

## Clarifications

### Session 2026-04-10

- Q: How does the test obtain `dataSourcingId` after `POST /requests`? → A: `POST /requests` returns `requestId` (used as `dataRequestId`); then `PATCH /requests/{dataRequestId}/state` (set to `Processing`) returns `dataSourcingEntityId`, which is used as `dataSourcingId`.
- Q: What is the Data Sourcing state after a `Rejected` QA decision? → A: Unchanged — nothing changes in the Data Sourcing Service after rejection.
- Q: Should DS state assertions use polling or immediate check? → A: Poll with `awaitUntilAsserted` — DS state transitions are async (event-driven).
- Q: Should DS state assertions use the same 2s timeout as other async assertions? → A: Yes — keep the 2s budget for consistency across all async assertions.

## Assumptions

- The Data Sourcing Service is part of the local stack started by `manageLocalStack.sh --start --simple`.
- `POST /requests` returns `requestId` (used as `dataRequestId`). `PATCH /requests/{dataRequestId}/state` (set to `Processing`) returns `dataSourcingEntityId`, which is then used as `dataSourcingId` for all subsequent Data Sourcing Service assertions.
- The Data Sourcing state `NonSourceableVerification` is set asynchronously after the non-sourceability entry is posted; polling may be required.
- The Data Sourcing state after a `Rejected` QA decision is completely unchanged — the state remains exactly as it was before the QA decision was posted.
- All API calls are made as `TechnicalUser.Admin` consistent with existing test patterns.
- No new dependencies are required; the Data Sourcing Service client is already available in `ApiAccessor`.
